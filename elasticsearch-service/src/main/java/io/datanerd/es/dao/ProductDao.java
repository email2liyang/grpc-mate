package io.datanerd.es.dao;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

import io.datanerd.generated.common.Product;
import io.datanerd.generated.es.CalculateProductScoreResponse;
import io.datanerd.generated.es.DownloadProductsRequest;
import io.datanerd.generated.es.SearchProductsRequest;
import io.datanerd.generated.es.SearchProductsResponse;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class ProductDao {

  private static Logger log = LoggerFactory.getLogger(ProductDao.class); //NOPMD
  public static final String INDEX = "products";
  public static final String TYPE = "Product";
  public static final TimeValue DEFAULT_SCROLL_TIME_VALUE = new TimeValue(60000);
  static final int SCROLL_SIZE = 50;

  @Inject
  private TransportClient esClient;

  @Inject
  private JsonFormat.Printer jsonPrinter;

  @Inject
  private JsonFormat.Parser jsonParser;

  /**
   * Upsert the given product into ES index.
   *
   * @param product given product object
   */
  public void upsertProduct(Product product) throws InvalidProtocolBufferException {
    log.debug("save product into ES");
    final UpdateRequestBuilder updateRequestBuilder =
        esClient
            .prepareUpdate(INDEX, TYPE, String.valueOf(product.getProductId()))
            .setDoc(
                jsonPrinter
                    .includingDefaultValueFields()
                    .omittingInsignificantWhitespace()
                    .print(product)
            )
            .setDocAsUpsert(true);
    updateRequestBuilder.get();
  }

  /**
   * Init index setting and mapping.
   *
   * @return true if a new index was created, false otherwise
   */
  public boolean initIndexIfNotExists() throws IOException {
    final IndicesExistsResponse existsResponse = esClient.admin().indices().prepareExists(INDEX).get();
    if (existsResponse.isExists()) {
      return false;
    }
    final String settings = Resources.toString(
        getClass().getResource("/elasticsearch/product_settings.json"),
        Charset.defaultCharset()
    );
    CreateIndexRequestBuilder createIndexRequestBuilder =
        esClient
            .admin()
            .indices()
            .prepareCreate(INDEX)
            .setSettings(settings);
    final String mapping = Resources.toString(
        getClass().getResource("/elasticsearch/product_mappings.json"),
        Charset.defaultCharset()
    );
    createIndexRequestBuilder = createIndexRequestBuilder.addMapping(TYPE, mapping);
    return createIndexRequestBuilder.get().isShardsAcked();
  }

  /**
   * Search Product by keywords.
   *
   * @param request search request which contains keyword and limit
   * @return SearchProductsResponse
   */
  public SearchProductsResponse searchProducts(SearchProductsRequest request) throws InvalidProtocolBufferException {
    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .must(QueryBuilders.matchQuery("productName", request.getKeyWord()));
    SearchResponse response = esClient.prepareSearch(INDEX)
        .setTypes(TYPE)
        .setQuery(queryBuilder)
        .setSize(request.getLimit())
        .execute()
        .actionGet();
    SearchHits hits = response.getHits();
    SearchProductsResponse.Builder responseBuilder = SearchProductsResponse.newBuilder();
    for (SearchHit hit : hits) {
      Product.Builder builder = Product.newBuilder();
      jsonParser.merge(hit.getSourceAsString(), builder);
      responseBuilder.addProducts(builder.build());
    }
    return responseBuilder.build();
  }

  /**
   * Download product from given category.
   *
   * @param request               which contains query category
   * @param productPublishSubject the subject which downloaded product should publish to
   */
  public void downloadProducts(DownloadProductsRequest request, PublishSubject<Product> productPublishSubject) {

    QueryBuilder queryBuilder = QueryBuilders.termQuery("category", request.getCategory());
    SearchResponse scrollResponse =
        esClient
            .prepareSearch(INDEX)
            .setScroll(DEFAULT_SCROLL_TIME_VALUE)
            .setTypes(TYPE)
            .setQuery(queryBuilder)
            .setSize(SCROLL_SIZE)
            .get();
    do {
      scrollResponse.getHits().forEach(hit -> {
        try {
          Product.Builder builder = Product.newBuilder();
          jsonParser.merge(hit.sourceAsString(), builder);
          productPublishSubject.onNext(builder.build());
        } catch (IOException ioe) {
          // Don't fail the whole stream
          log.error("Unable to read product record", ioe);
          productPublishSubject.onError(ioe);
          throw new IllegalStateException(ioe);
        }
      });
      // Fetch next batch of cite group records
      scrollResponse =
          esClient
              .prepareSearchScroll(scrollResponse.getScrollId())
              .setScroll(DEFAULT_SCROLL_TIME_VALUE)
              .execute()
              .actionGet();
    } while (scrollResponse.getHits().getHits().length != 0);

    productPublishSubject.onComplete();
  }

  /**
   * Calculate product score based on product price for demo only.
   *
   * @param product        given product to be calculated
   * @param downloadStream the download stream for score
   */
  public void calculateProductScore(Product product, PublishSubject<CalculateProductScoreResponse> downloadStream) {
    downloadStream.onNext(
        CalculateProductScoreResponse
            .newBuilder()
            .setProduct(product)
            .setScore(new Double(product.getProductPrice()).longValue())
            .build()
    );
  }
}
