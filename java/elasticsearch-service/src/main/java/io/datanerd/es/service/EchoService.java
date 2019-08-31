package io.datanerd.es.service;

import com.google.inject.Singleton;

import io.datanerd.generated.es.EchoRequest;
import io.datanerd.generated.es.EchoResponse;
import io.datanerd.generated.es.EchoServiceGrpc;
import io.grpc.stub.StreamObserver;

@Singleton
public class EchoService extends EchoServiceGrpc.EchoServiceImplBase {

  @Override
  public void echo(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
    responseObserver.onNext(EchoResponse.newBuilder().setPong(request.getPing()).build());
    responseObserver.onCompleted();
  }
}
