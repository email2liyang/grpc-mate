package io.datanerd.es.service;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;

import io.datanerd.es.dao.ProductDao;
import io.datanerd.es.metrics.CounterFactory;
import io.datanerd.es.metrics.Metric;
import io.datanerd.generated.common.DataChunk;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.es.CalculateProductScoreResponse;
import io.datanerd.generated.es.DownloadProductImageRequest;
import io.datanerd.generated.es.DownloadProductsRequest;
import io.datanerd.generated.es.ProductReadServiceGrpc;
import io.datanerd.generated.es.SearchProductsRequest;
import io.datanerd.generated.es.SearchProductsResponse;
import io.grpc.stub.StreamObserver;
import io.prometheus.client.Counter;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class ProductReadService extends ProductReadServiceGrpc.ProductReadServiceImplBase {

  private static Logger log = LoggerFactory.getLogger(ProductReadService.class); //NOPMD
  @Inject
  private ProductDao productDao;
  @Inject
  private ProductImageSeeker productImageSeeker;

  private final Counter counter;

  /**
   * Constructor invoked by Guice.
   */
  @Inject
  public ProductReadService() {
    counter = CounterFactory.create(
        Metric.builder()
            .setService(ProductReadService.class)
            .setLabels(ImmutableList.of("method", "outcome"))
            .build()
    );
  }

  @Override
  public void searchProducts(SearchProductsRequest request, StreamObserver<SearchProductsResponse> responseObserver) {
    try {
      responseObserver.onNext(productDao.searchProducts(request));
      responseObserver.onCompleted();
      counter.labels("searchProducts", "success");
    } catch (Exception e) {
      log.error(" error on search product with request - {}", request, e);
      responseObserver.onError(e);
      counter.labels("searchProducts", "failed");
    }
  }

  @Override
  public void downloadProducts(DownloadProductsRequest request, StreamObserver<Product> responseObserver) {
    PublishSubject<Product> productPublishSubject = PublishSubject.create();
    productPublishSubject
        .doOnNext(product -> {
          responseObserver.onNext(product);
          counter.labels("downloadProducts", "success");
        })
        .doOnComplete(() -> responseObserver.onCompleted())
        .doOnError(t -> {
          responseObserver.onError(t);
          counter.labels("downloadProducts", "failed");
        })
        .subscribe();
    productDao.downloadProducts(request, productPublishSubject);
  }

  @Override
  public StreamObserver<Product> calculateProductScore(StreamObserver<CalculateProductScoreResponse> responseObserver) {
    //define download stream behaviour
    PublishSubject<CalculateProductScoreResponse> downloadStream = PublishSubject.create();
    downloadStream
        .doOnNext(response -> {
          responseObserver.onNext(response);
          counter.labels("calculateProductScore_download", "success");
        })
        .doOnError(t -> {
          log.error("error on calculate product score response", t);
          responseObserver.onError(t);
          counter.labels("calculateProductScore_download", "failed");
        })
        .doOnComplete(() -> {
          log.info("calculate product score response done");
          responseObserver.onCompleted();
        })
        .subscribe();
    //define upload stream behaviour
    PublishSubject<Product> uploadStream = PublishSubject.create();
    uploadStream
        .doOnNext(product -> {
          log.debug(" calculate product score - {}", product);
          productDao.calculateProductScore(product, downloadStream);
          counter.labels("calculateProductScore_upload", "success");
        })
        .doOnError(t -> {
          log.info("client upload got error", t);
          downloadStream.onError(t);
          counter.labels("calculateProductScore_upload", "failed");
        })
        .doOnComplete(() -> {
          log.info("client upload complete");
          downloadStream.onComplete();
        })
        .subscribe();

    return new RxStreamObserver<>(uploadStream);

  }

  @Override
  public void downloadProductImage(DownloadProductImageRequest request, StreamObserver<DataChunk> responseObserver) {
    BufferedInputStream imageStream = new BufferedInputStream(
        productImageSeeker.seekProductImage(request.getProductId())
    );

    try {
      int bufferSize = 256 * 1024;// 256k
      byte[] buffer = new byte[bufferSize];
      int offset = 0;
      int length;
      while ((length = imageStream.read(buffer, offset, bufferSize)) != -1) {
        responseObserver.onNext(
            DataChunk.newBuilder().setData(ByteString.copyFrom(buffer, 0, length)).build()
        );
        offset += length;
      }
      responseObserver.onCompleted();
      counter.labels("downloadProductImage", "success");
    } catch (Exception e) {
      counter.labels("downloadProductImage", "failed");
      log.error("error on read product image", e);
      responseObserver.onError(e);
    }

  }
}
