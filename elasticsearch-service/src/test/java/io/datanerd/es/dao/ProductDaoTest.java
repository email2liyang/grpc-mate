package io.datanerd.es.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
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
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;

import io.datanerd.es.guice.ElasticSearchModule;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.common.ProductStatus;
import io.datanerd.generated.es.SearchProductsRequest;
import io.datanerd.generated.es.SearchProductsResponse;

import static io.datanerd.es.TestConstant.ES_TEST_IMAGE;
import static io.datanerd.es.dao.ProductDao.INDEX;
import static io.datanerd.es.dao.ProductDao.TYPE;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_HOST;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_NAME;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_PORT;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductDaoTest {

  private static Faker faker;
  private static ProductDao productDao;
  private static TransportClient esClient;
  private static Injector injector;

  @ClassRule
  public static final GenericContainer esContainer =
      new GenericContainer(ES_TEST_IMAGE)
          .withEnv("transport.host","0.0.0.0")
          .withEnv("discovery.zen.minimum_master_nodes","1")
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
}