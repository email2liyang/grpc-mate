package io.datanerd.hello.service;

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datanerd.generated.helloworld.GreeterGrpc;
import io.datanerd.generated.helloworld.HelloReply;
import io.datanerd.generated.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;

@Singleton
public class GreeterService extends GreeterGrpc.GreeterImplBase {

  private static Logger log = LoggerFactory.getLogger(GreeterService.class); //NOPMD

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    log.info("get request - {}", request.getName());
    HelloReply helloReply = HelloReply
        .newBuilder()
        .setMessage(String.format("hello %s", request.getName()))
        .build();
    log.info("reply with {}", helloReply);
    responseObserver.onNext(helloReply);
    responseObserver.onCompleted();
  }
}
