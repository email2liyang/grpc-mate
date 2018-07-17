package io.datanerd.hello.service;

import com.google.inject.Singleton;

import io.datanerd.generated.helloworld.GreeterGrpc;
import io.datanerd.generated.helloworld.HelloReply;
import io.datanerd.generated.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;

@Singleton
public class GreeterService extends GreeterGrpc.GreeterImplBase {

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    responseObserver.onNext(
        HelloReply
            .newBuilder()
            .setMessage(String.format("hello %s", request.getName()))
            .build());
    responseObserver.onCompleted();
  }
}
