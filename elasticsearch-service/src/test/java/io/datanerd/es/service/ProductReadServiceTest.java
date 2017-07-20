package io.datanerd.es.service;

import com.google.inject.Guice;

import com.github.javafaker.Faker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.datanerd.es.dao.ProductDao;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.common.ProductStatus;
import io.datanerd.generated.es.ProductReadServiceGrpc;
import io.datanerd.generated.es.SearchProductsRequest;
import io.datanerd.generated.es.SearchProductsResponse;
import io.grpc.Channel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductReadServiceTest {

  private Faker faker;
  private ProductDao productDao;
  private ProductReadService productReadService;
  private Server server;
  private ProductReadServiceGrpc.ProductReadServiceBlockingStub blockingStub;

  @Before
  public void setUp() throws Exception {
    faker = new Faker();
    productDao = mock(ProductDao.class);
    productReadService = Guice.createInjector(binder -> {
      binder.bind(ProductDao.class).toProvider(() -> productDao);
    }).getInstance(ProductReadService.class);

    String serverName = faker.numerify("prod-read-server-###");
    server = InProcessServerBuilder
        .forName(serverName)
        .addService(productReadService)
        .build()
        .start();
    Channel channel = InProcessChannelBuilder.forName(serverName).build();
    blockingStub = ProductReadServiceGrpc.newBlockingStub(channel);
  }

  @After
  public void tearDown() throws Exception {
    faker = null;
    productDao = null;
    productReadService = null;
    server.shutdownNow();
  }

  @Test
  public void searchProducts() throws Exception {
    SearchProductsResponse searchProductsResponse = SearchProductsResponse.newBuilder()
        .addProducts(
            Product.newBuilder()
                .setProductId(faker.number().randomNumber())
                .setProductName(faker.name().fullName())
                .setCategory(faker.numerify("category-###"))
                .setProductStatus(ProductStatus.InStock)
        ).build();
    when(productDao.searchProducts(any())).thenReturn(searchProductsResponse);

    SearchProductsResponse result = blockingStub.searchProducts(SearchProductsRequest.getDefaultInstance());
    assertThat(result).isEqualTo(searchProductsResponse);
  }

  @Test(expected = StatusRuntimeException.class)
  public void searchProducts_with_exception() throws Exception {
    when(productDao.searchProducts(any())).thenThrow(new IllegalStateException());
    blockingStub.searchProducts(SearchProductsRequest.getDefaultInstance());
  }

}