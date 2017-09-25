package io.datanerd.es.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class CallerInterceptor implements ClientInterceptor {

  private static Logger log = LoggerFactory.getLogger(CallerInterceptor.class); //NOPMD
  static final Metadata.Key<String> CUSTOM_HEADER_KEY =
      Metadata.Key.of("custom_client_header_key", Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
                                                             Channel next) {
    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        headers.put(CUSTOM_HEADER_KEY, "hello grpc");
        super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener(responseListener) {
          @Override
          public void onHeaders(Metadata headers) {
            log.info("receive header from server - {}", headers);
            super.onHeaders(headers);
          }
        }, headers);
      }
    };


  }
}
