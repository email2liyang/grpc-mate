package io.datanerd.es.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import com.github.javafaker.Faker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.datanerd.es.dao.ProductDao;
import io.datanerd.es.guice.ElasticSearchModule;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.common.ProductStatus;
import io.datanerd.generated.es.ProductUpdateServiceGrpc;
import io.datanerd.generated.es.UploadProductResponse;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcServerRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ProductUpdateServiceTest {

  @Rule
  public GrpcServerRule grpcServerRule = new GrpcServerRule();

  private Faker faker;
  private ProductDao productDao;
  private Injector injector;
  private ProductUpdateService productUpdateService;
  private ProductUpdateServiceGrpc.ProductUpdateServiceStub stub;

  @Before
  public void setUp() throws Exception {
    faker = new Faker();
    productDao = mock(ProductDao.class);
    injector = Guice.createInjector(
        Modules.override(new ElasticSearchModule())
            .with(binder -> {
              binder.bind(ProductDao.class).toProvider(() -> productDao);
            })
    );

    productUpdateService = injector.getInstance(ProductUpdateService.class);
    grpcServerRule.getServiceRegistry().addService(productUpdateService);

    stub = ProductUpdateServiceGrpc.newStub(grpcServerRule.getChannel());
  }

  @After
  public void tearDown() throws Exception {

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
        .setProductPrice(faker.number().randomDouble(2, 10, 100))
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
    verify(productDao, times(3)).upsertProduct(any());
  }

}