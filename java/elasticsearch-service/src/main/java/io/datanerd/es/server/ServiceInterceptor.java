package io.datanerd.es.server;

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@Singleton
public class ServiceInterceptor implements ServerInterceptor {
  //ref https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/header/HeaderServerInterceptor.java
  private static Logger log = LoggerFactory.getLogger(ServiceInterceptor.class); //NOPMD
  static final Metadata.Key<String> CUSTOM_HEADER_KEY =
      Metadata.Key.of("custom_server_header_key", Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                               ServerCallHandler<ReqT, RespT> next) {

    log.info("receive headers - {}", headers);
    String methodName = call.getMethodDescriptor().getFullMethodName();

    return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
      @Override
      public void sendHeaders(Metadata headers) {
        headers.put(CUSTOM_HEADER_KEY, methodName);
        super.sendHeaders(headers);
      }
    }, headers);
  }
}
