package io.datanerd.es.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datanerd.es.dao.ProductDao;
import io.datanerd.generated.common.Product;
import io.datanerd.generated.es.ProductUpdateServiceGrpc;
import io.datanerd.generated.es.UploadProductResponse;
import io.grpc.stub.StreamObserver;
import io.reactivex.subjects.PublishSubject;

/**
 * Provide Product mutation related service.
 *
 * @author email2liyang@gmail.com
 */
@Singleton
public class ProductUpdateService extends ProductUpdateServiceGrpc.ProductUpdateServiceImplBase {

  private static Logger log = LoggerFactory.getLogger(ProductUpdateService.class); //NOPMD
  @Inject
  private ProductDao productDao;
  @Override
  public StreamObserver<Product> uploadProduct(StreamObserver<UploadProductResponse> responseObserver) {
    PublishSubject<Product> publishSubject = PublishSubject.create();
    publishSubject
        .doOnNext(product -> {
          log.info("saving product - {} ",product);
          productDao.upsertProduct(product);
        })
        .doOnError(t->responseObserver.onError(t))
        .doOnComplete(()->{
          responseObserver.onNext(UploadProductResponse.newBuilder().build());
          responseObserver.onCompleted();
        })
    .subscribe();
    return new RxStreamObserver<>(publishSubject);
  }
}
