package io.datanerd.es.service;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.inject.Guice;

import com.github.javafaker.Faker;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.datanerd.es.dao.ProductDao;
import io.datanerd.generated.common.DataChunk;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.common.ProductStatus;
import io.datanerd.generated.es.CalculateProductScoreResponse;
import io.datanerd.generated.es.DownloadProductImageRequest;
import io.datanerd.generated.es.DownloadProductsRequest;
import io.datanerd.generated.es.ProductReadServiceGrpc;
import io.datanerd.generated.es.SearchProductsRequest;
import io.datanerd.generated.es.SearchProductsResponse;
import io.grpc.Channel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.reactivex.subjects.PublishSubject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductReadServiceTest {

  private static Logger log = LoggerFactory.getLogger(ProductReadServiceTest.class); //NOPMD
  private Faker faker;
  private ProductDao productDao;
  private ProductImageSeeker productImageSeeker;
  private ProductReadService productReadService;
  private Server server;
  private ProductReadServiceGrpc.ProductReadServiceBlockingStub blockingStub;
  private ProductReadServiceGrpc.ProductReadServiceStub stub;

  @Before
  public void setUp() throws Exception {
    faker = new Faker();
    productDao = mock(ProductDao.class);
    productImageSeeker = mock(ProductImageSeeker.class);
    productReadService = Guice.createInjector(binder -> {
      binder.bind(ProductDao.class).toProvider(() -> productDao);
      binder.bind(ProductImageSeeker.class).toProvider(() -> productImageSeeker);
    }).getInstance(ProductReadService.class);

    String serverName = faker.numerify("prod-read-server-###");
    server = InProcessServerBuilder
        .forName(serverName)
        .addService(productReadService)
        .build()
        .start();
    Channel channel = InProcessChannelBuilder.forName(serverName).build();
    blockingStub = ProductReadServiceGrpc.newBlockingStub(channel);
    stub = ProductReadServiceGrpc.newStub(channel);
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

  @Test
  public void downloadProducts() throws Exception {
    doAnswer(invocation -> {
      PublishSubject<Product> publishSubject = (PublishSubject<Product>) invocation.getArguments()[1];
      publishSubject.onNext(Product.getDefaultInstance());
      publishSubject.onComplete();
      return null;
    }).when(productDao).downloadProducts(any(), any());

    List<Product> downloadedProducts = Lists.newArrayList();
    AtomicBoolean onCompletedCalled = new AtomicBoolean(false);
    StreamObserver<Product> downloadObserver = new StreamObserver<Product>() {
      @Override
      public void onNext(Product value) {
        downloadedProducts.add(value);
      }

      @Override
      public void onError(Throwable t) {
        fail("should not fail");
      }

      @Override
      public void onCompleted() {
        onCompletedCalled.compareAndSet(false, true);
      }
    };
    productReadService.downloadProducts(DownloadProductsRequest.getDefaultInstance(), downloadObserver);

    verify(productDao, times(1)).downloadProducts(any(), any());
    assertThat(downloadedProducts).containsOnly(Product.getDefaultInstance());
    assertThat(onCompletedCalled).isTrue();
  }

  @Test
  public void calculateProductScore() throws Exception {
    doAnswer(invocation -> {
      PublishSubject<CalculateProductScoreResponse> downloadStream =
          (PublishSubject<CalculateProductScoreResponse>) invocation.getArguments()[1];

      downloadStream.onNext(CalculateProductScoreResponse.getDefaultInstance());
      return null;
    }).when(productDao).calculateProductScore(any(), any());
    List<CalculateProductScoreResponse> responses = Lists.newArrayList();
    AtomicBoolean onErrorCalled = new AtomicBoolean(false);
    AtomicBoolean onCompleted = new AtomicBoolean(false);
    StreamObserver<Product>
        uploadStream =
        productReadService.calculateProductScore(new StreamObserver<CalculateProductScoreResponse>() {
          @Override
          public void onNext(CalculateProductScoreResponse value) {
            responses.add(value);
          }

          @Override
          public void onError(Throwable t) {
            onErrorCalled.compareAndSet(false, true);
          }

          @Override
          public void onCompleted() {
            onCompleted.compareAndSet(false, true);
          }
        });

    List<Product> products = IntStream.range(1, 5)
        .mapToObj(index -> Product.getDefaultInstance())
        .collect(Collectors.toList());

    products.forEach(product -> uploadStream.onNext(product));
    uploadStream.onCompleted();

    assertThat(responses.size()).isEqualTo(4);
    assertThat(onCompleted).isTrue();
    assertThat(onErrorCalled).isFalse();
  }

  @Test
  public void downloadProductImage() throws Exception {
    when(productImageSeeker.seekProductImage(anyLong()))
        .thenReturn(Resources.getResource("Large_Scaled_Forest_Lizard.jpg").openStream());

    AtomicBoolean completed = new AtomicBoolean(false);
    AtomicBoolean error = new AtomicBoolean(false);
    File imageFile = File.createTempFile("image", ".jpg");
    imageFile.deleteOnExit();
    Files.touch(imageFile);
    ByteSink byteSink = Files.asByteSink(imageFile, FileWriteMode.APPEND);
    StreamObserver<DataChunk> streamObserver = new StreamObserver<DataChunk>() {
      @Override
      public void onNext(DataChunk dataChunk) {
        try {
          byteSink.write(dataChunk.getData().toByteArray());
        } catch (IOException e) {
          log.error("error on write files", e);
          onError(e);
        }
      }

      @Override
      public void onError(Throwable t) {
        error.compareAndSet(false, true);
      }

      @Override
      public void onCompleted() {
        log.info("write image to {}", imageFile.getAbsoluteFile());
        completed.compareAndSet(false, true);
      }
    };
    stub.downloadProductImage(DownloadProductImageRequest.getDefaultInstance(), streamObserver);

    while (!completed.get() && !error.get()) {
      Thread.sleep(500);
    }

    assertThat(completed.get()).isTrue();
    assertThat(error.get()).isFalse();

    try(InputStream destImageStream = new FileInputStream(imageFile);
    InputStream origImageStream = Resources.getResource("Large_Scaled_Forest_Lizard.jpg").openStream()){
      assertThat(DigestUtils.md5Hex(destImageStream)).isEqualTo(
          DigestUtils.md5Hex(origImageStream)
      );
    }
  }
}