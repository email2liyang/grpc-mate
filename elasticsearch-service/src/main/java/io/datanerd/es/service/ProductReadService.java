package io.datanerd.es.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datanerd.es.dao.ProductDao;
import io.datanerd.generated.es.ProductReadServiceGrpc;
import io.datanerd.generated.es.SearchProductsRequest;
import io.datanerd.generated.es.SearchProductsResponse;
import io.grpc.stub.StreamObserver;

@Singleton
public class ProductReadService extends ProductReadServiceGrpc.ProductReadServiceImplBase {

  private static Logger log = LoggerFactory.getLogger(ProductReadService.class); //NOPMD
  @Inject
  private ProductDao productDao;

  @Override
  public void searchProducts(SearchProductsRequest request, StreamObserver<SearchProductsResponse> responseObserver) {
    try{
      responseObserver.onNext(productDao.searchProducts(request));
      responseObserver.onCompleted();
    }catch (Exception e){
      log.error(" error on search product with request - {}",request,e);
      responseObserver.onError(e);
    }
  }
}
