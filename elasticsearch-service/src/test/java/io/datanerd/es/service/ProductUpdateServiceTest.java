package io.datanerd.es.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import com.github.javafaker.Faker;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.datanerd.es.dao.ProductDao;
import io.datanerd.es.guice.ElasticSearchModule;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.common.ProductStatus;
import io.datanerd.generated.es.ProductUpdateServiceGrpc;
import io.datanerd.generated.es.UploadProductResponse;
import io.grpc.Channel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ProductUpdateServiceTest {

  private Faker faker;
  private ProductDao productDao;
  private Injector injector;
  private ProductUpdateService productUpdateService;
  private Server server;
  private ProductUpdateServiceGrpc.ProductUpdateServiceStub stub;

  @Before
  public void setUp() throws Exception {
    faker = new Faker();
    productDao = mock(ProductDao.class);
    injector = Guice.createInjector(
        Modules.override(new ElasticSearchModule())
            .with(binder -> {
              binder.bind(ProductDao.class).toInstance(productDao);
            })
    );

    productUpdateService = injector.getInstance(ProductUpdateService.class);
    String serverName = faker.numerify("prod-update-server-###");
    server = InProcessServerBuilder
        .forName(serverName)
        .addService(productUpdateService)
        .build()
        .start();
    Channel channel = InProcessChannelBuilder.forName(serverName).build();
    stub = ProductUpdateServiceGrpc.newStub(channel);
  }

  @After
  public void tearDown() throws Exception {
    server.shutdownNow();
  }

  @Test
  public void uploadProduct() throws Exception {
    Set<UploadProductResponse> responseHolder = Sets.newHashSet();
    Set<Throwable> exceptionHolder = Sets.newHashSet();
    AtomicBoolean completed = new AtomicBoolean(false);
    //client stream is as complex as Bidirectional streaming
    StreamObserver<Product> uploadStream = stub.uploadProduct(new StreamObserver<UploadProductResponse>() {
      @Override
      public void onNext(UploadProductResponse value) {
        responseHolder.add(value);
      }

      @Override
      public void onError(Throwable t) {
        exceptionHolder.add(t);
      }

      @Override
      public void onCompleted() {
        completed.compareAndSet(false, true);
      }
    });
    Product product = Product.newBuilder()
        .setProductId(faker.number().randomNumber())
        .setProductName(faker.company().name())
        .setProductPrice(faker.number().randomDouble(2,10,100))
        .setProductStatus(ProductStatus.InStock)
        .build();
    ImmutableList
        .of(product, product, product)
        .stream()
        .forEach(prod -> uploadStream.onNext(prod));
    uploadStream.onCompleted();

    while (!completed.get()) {
      Thread.sleep(200);
    }

    assertThat(responseHolder.size()).isEqualTo(1);
    assertThat(exceptionHolder).isEmpty();
    assertThat(completed).isTrue();
    verify(productDao,times(3)).upsertProduct(any());
  }

}