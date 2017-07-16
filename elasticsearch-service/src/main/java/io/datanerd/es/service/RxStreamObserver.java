package io.datanerd.es.service;

import io.grpc.stub.StreamObserver;
import io.reactivex.Observer;

/**
 * Reactive stream observer can link gRPC and rxJava to work together.
 * This is very use full when do gRPC client streaming.
 *
 * @author ivan.li@practiceinsight.io
 */
public class RxStreamObserver<T> implements StreamObserver<T> {

  private final Observer<T> observer;

  public RxStreamObserver(Observer<T> observer) {
    this.observer = observer;
  }

  @Override
  public void onNext(T value) {
    observer.onNext(value);
  }

  @Override
  public void onError(Throwable t) {
    observer.onError(t);
  }

  @Override
  public void onCompleted() {
    observer.onComplete();
  }
}
