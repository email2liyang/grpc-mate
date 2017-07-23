package io.datanerd.es.service;

import com.google.common.collect.Sets;

import org.junit.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static org.assertj.core.api.Assertions.assertThat;

public class RxStreamObserverTest {

  @Test
  public void clientStreaming_sync() throws Exception {
    PublishSubject<Integer> publishSubject = PublishSubject.create();
    Set<Integer> resultsHolder = Sets.newConcurrentHashSet();
    Set<Throwable> exceptionsHolder = Sets.newConcurrentHashSet();
    AtomicBoolean complete = new AtomicBoolean(false);
    Disposable disposable = publishSubject
        .doOnNext(num->resultsHolder.add(num*2))
        .doOnError(t->exceptionsHolder.add(t))
        .doOnComplete(()->complete.compareAndSet(false,true))
        .subscribe();
    assertThat(disposable.isDisposed()).isFalse();
    RxStreamObserver<Integer> rxStreamObserver = new RxStreamObserver<>(publishSubject);

    IntStream.range(1,5).forEach(num->rxStreamObserver.onNext(num));
    rxStreamObserver.onCompleted();
    assertThat(disposable.isDisposed()).isTrue();
    assertThat(resultsHolder).containsExactly(2,4,6,8);
    assertThat(exceptionsHolder).isEmpty();
    assertThat(complete).isTrue();
  }

  @Test
  public void clientStreaming_async() throws Exception {
    PublishSubject<Integer> publishSubject = PublishSubject.create();
    Set<Integer> resultsHolder = Sets.newConcurrentHashSet();
    Set<Throwable> exceptionsHolder = Sets.newConcurrentHashSet();
    AtomicBoolean complete = new AtomicBoolean(false);
    Disposable disposable = publishSubject
        .observeOn(Schedulers.io())
        .doOnNext(num->resultsHolder.add(num*2))
        .doOnError(t->exceptionsHolder.add(t))
        .doOnComplete(()->complete.compareAndSet(false,true))
        .subscribe();

    assertThat(disposable.isDisposed()).isFalse();
    RxStreamObserver<Integer> rxStreamObserver = new RxStreamObserver<>(publishSubject);

    IntStream.range(1,5).forEach(num->rxStreamObserver.onNext(num));
    rxStreamObserver.onCompleted();
    while(resultsHolder.size() < 4){
      Thread.sleep(500);
    }

    assertThat(disposable.isDisposed()).isTrue();
    assertThat(resultsHolder).containsExactly(2,4,6,8);
    assertThat(exceptionsHolder).isEmpty();
    assertThat(complete).isTrue();
  }

  @Test
  public void clientStreaming_error_break_flow() throws Exception {
    PublishSubject<Integer> publishSubject = PublishSubject.create();
    Set<Integer> resultsHolder = Sets.newConcurrentHashSet();
    Set<Throwable> exceptionsHolder = Sets.newConcurrentHashSet();
    AtomicBoolean complete = new AtomicBoolean(false);
    Disposable disposable = publishSubject
        .doOnNext(num->resultsHolder.add(num))
        .doOnError(t->exceptionsHolder.add(t))
        .doOnComplete(()->complete.compareAndSet(false,true))
        .subscribe();
    assertThat(disposable.isDisposed()).isFalse();
    RxStreamObserver<Integer> rxStreamObserver = new RxStreamObserver<>(publishSubject);

    rxStreamObserver.onError(new IllegalStateException());

    assertThat(disposable.isDisposed()).isTrue();
    assertThat(resultsHolder).isEmpty();
    assertThat(exceptionsHolder.size()).isEqualTo(1);
    assertThat(exceptionsHolder.iterator().next()).isInstanceOf(IllegalStateException.class);
    assertThat(complete).isFalse();
  }
}