package io.datanerd.es.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datanerd.es.dao.ProductDao;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.es.CalculateProductScoreResponse;
import io.datanerd.generated.es.DownloadProductsRequest;
import io.datanerd.generated.es.ProductReadServiceGrpc;
import io.datanerd.generated.es.SearchProductsRequest;
import io.datanerd.generated.es.SearchProductsResponse;
import io.grpc.stub.StreamObserver;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class ProductReadService extends ProductReadServiceGrpc.ProductReadServiceImplBase {

  private static Logger log = LoggerFactory.getLogger(ProductReadService.class); //NOPMD
  @Inject
  private ProductDao productDao;

  @Override
  public void searchProducts(SearchProductsRequest request, StreamObserver<SearchProductsResponse> responseObserver) {
    try {
      responseObserver.onNext(productDao.searchProducts(request));
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error(" error on search product with request - {}", request, e);
      responseObserver.onError(e);
    }
  }

  @Override
  public void downloadProducts(DownloadProductsRequest request, StreamObserver<Product> responseObserver) {
    PublishSubject<Product> productPublishSubject = PublishSubject.create();
    productPublishSubject
        .doOnNext(product -> responseObserver.onNext(product))
        .doOnComplete(() -> responseObserver.onCompleted())
        .doOnError(t -> responseObserver.onError(t))
        .subscribe();
    productDao.downloadProducts(request, productPublishSubject);
  }

  @Override
  public StreamObserver<Product> calculateProductScore(StreamObserver<CalculateProductScoreResponse> responseObserver) {
    //define download stream behaviour
    PublishSubject<CalculateProductScoreResponse> downloadStream = PublishSubject.create();
    downloadStream
        .doOnNext(response -> responseObserver.onNext(response))
        .doOnError(t -> {
          log.error("error on calculate product score response", t);
          responseObserver.onError(t);
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
        })
        .doOnError(t -> {
          log.info("client upload got error", t);
          downloadStream.onError(t);
        })
        .doOnComplete(() -> {
          log.info("client upload complete");
          downloadStream.onComplete();
        })
        .subscribe();

    return new RxStreamObserver<>(uploadStream);

  }
}
