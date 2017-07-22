package io.datanerd.es.dao;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import com.github.javafaker.Faker;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.datanerd.es.guice.ElasticSearchModule;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.common.ProductStatus;
import io.datanerd.generated.es.DownloadProductsRequest;
import io.datanerd.generated.es.SearchProductsRequest;
import io.datanerd.generated.es.SearchProductsResponse;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static io.datanerd.es.TestConstant.ES_TEST_IMAGE;
import static io.datanerd.es.dao.ProductDao.INDEX;
import static io.datanerd.es.dao.ProductDao.TYPE;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_HOST;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_NAME;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_PORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@Ignore
public class ProductDaoTest {

  private static Logger log = LoggerFactory.getLogger(ProductDaoTest.class); //NOPMD
  private static Faker faker;
  private static ProductDao productDao;
  private static TransportClient esClient;
  private static Injector injector;

  @ClassRule
  public static final GenericContainer esContainer =
      new GenericContainer(ES_TEST_IMAGE)
          .withEnv("transport.host", "0.0.0.0")
          .withEnv("discovery.zen.minimum_master_nodes", "1")
          .withExposedPorts(9200, 9300);

  @BeforeClass
  public static void setUpClass() throws Exception {
    faker = new Faker();
    String ip = esContainer.getContainerIpAddress();
    Integer transportPort = esContainer.getMappedPort(9300);
    MapConfiguration memoryParams = new MapConfiguration(new HashMap<>());
    memoryParams.setProperty(CONFIG_ES_CLUSTER_HOST, ip);
    memoryParams.setProperty(CONFIG_ES_CLUSTER_PORT, transportPort);
    memoryParams.setProperty(CONFIG_ES_CLUSTER_NAME, "elasticsearch");
    injector = Guice.createInjector(
        Modules.override(new ElasticSearchModule())
            .with(binder -> {
              binder.bind(Configuration.class).toProvider(() -> memoryParams);
            })
    );
    productDao = injector.getInstance(ProductDao.class);
    esClient = injector.getInstance(TransportClient.class);
  }

  @Before
  public void setUp() throws Exception {
    productDao.initIndexIfNotExists();
  }

  @After
  public void tearDown() throws Exception {
    ClusterStatsResponse clusterStatsResponse = esClient.admin().cluster().prepareClusterStats().get();
    //be sure it's for unit test clean up
    if (clusterStatsResponse.getClusterName().value().equals("elasticsearch")
        && clusterStatsResponse.getNodes().size() == 1) {
      esClient.admin().indices().delete(new DeleteIndexRequest(INDEX)).actionGet();
    }
  }

  @Test
  public void initIndexIfNotExists() throws Exception {
    final IndicesExistsResponse existsResponse = esClient.admin().indices().prepareExists(INDEX).get();
    assertThat(existsResponse.isExists()).isTrue();
  }

  @Test
  public void upsertProduct() throws Exception {
    Product product = Product.newBuilder()
        .setProductId(faker.number().randomNumber())
        .setProductName(faker.company().name())
        .setProductPrice(faker.number().randomDouble(2, 10, 100))
        .setProductStatus(ProductStatus.InStock)
        .build();
    productDao.upsertProduct(product);
    esClient.admin().indices().flush(Requests.flushRequest(INDEX)).actionGet();

    GetResponse getResponse = esClient.prepareGet(INDEX, TYPE, String.valueOf(product.getProductId())).get();
    JsonFormat.Parser jsonParser = injector.getInstance(JsonFormat.Parser.class);
    Product.Builder builder = Product.newBuilder();
    jsonParser.merge(getResponse.getSourceAsString(), builder);
    assertThat(builder.build()).isEqualTo(product);
  }

  @Test
  public void searchProducts() throws Exception {
    Product product1 = Product.newBuilder()
        .setProductId(faker.number().randomNumber())
        .setProductName("apple guice")
        .setProductPrice(faker.number().randomDouble(2, 10, 100))
        .setProductStatus(ProductStatus.InStock)
        .build();
    Product product2 = Product.newBuilder()
        .setProductId(faker.number().randomNumber())
        .setProductName("cheese cake")
        .setProductPrice(faker.number().randomDouble(2, 10, 100))
        .setProductStatus(ProductStatus.InStock)
        .build();

    productDao.upsertProduct(product1);
    productDao.upsertProduct(product2);
    esClient.admin().indices().flush(Requests.flushRequest(INDEX)).actionGet();

    SearchProductsResponse response = productDao.searchProducts(
        SearchProductsRequest
            .newBuilder()
            .setKeyWord("apple")
            .setLimit(5)
            .build()
    );

    assertThat(response.getProductsList()).containsOnly(product1);
  }

  @Test
  public void downloadProducts() throws Exception {
    String category = faker.numerify("category-##");
    //insert sample data into es
    List<Product> sampleProducts = IntStream.range(1, 5).mapToObj(index -> {
      Product product = createProduct(category);
      try {
        productDao.upsertProduct(product);
      } catch (InvalidProtocolBufferException e) {
        log.error(" error on creating sample product for test downloadProducts", e);
      }
      return product;
    }).collect(Collectors.toList());

    esClient.admin().indices().flush(Requests.flushRequest(INDEX)).actionGet();
    PublishSubject<Product> productPublishSubject = PublishSubject.create();
    List<Product> downloadedProducts = Lists.newArrayList();
    Disposable disposable = productPublishSubject
        .doOnNext(product -> downloadedProducts.add(product))
        .doOnError(t -> fail("should not failed", t))
        .doOnComplete(() -> {
          Product[] downloadedProductArray = sampleProducts.toArray(new Product[]{});
          assertThat(downloadedProducts).containsOnly(downloadedProductArray);
        })
        .subscribe();
    productDao.downloadProducts(
        DownloadProductsRequest.newBuilder()
            .setCategory(category)
            .build(),
        productPublishSubject
    );
    disposable.dispose();
  }

  private Product createProduct(String category) {
    return Product.newBuilder()
        .setProductId(faker.number().randomNumber())
        .setProductName(faker.name().name())
        .setProductPrice(faker.number().randomDouble(2, 10, 100))
        .setCategory(category)
        .setProductStatus(ProductStatus.InStock)
        .build();
  }
}