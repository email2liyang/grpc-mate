package io.datanerd.es.service;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.github.javafaker.Faker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.datanerd.es.server.ServiceInterceptor;
import io.datanerd.generated.es.EchoRequest;
import io.datanerd.generated.es.EchoResponse;
import io.datanerd.generated.es.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ServerInterceptors;
import io.grpc.testing.GrpcServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class EchoServiceTest {

  private Faker faker;
  private EchoServiceGrpc.EchoServiceBlockingStub stub;

  @Rule
  public GrpcServerRule grpcServerRule = new GrpcServerRule();

  @Before
  public void setUp() throws Exception {
    faker = new Faker();
    Injector injector = Guice.createInjector();
    EchoService echoService = injector.getInstance(EchoService.class);
    ServiceInterceptor serviceInterceptor = injector.getInstance(ServiceInterceptor.class);
    CallerInterceptor callerInterceptor = injector.getInstance(CallerInterceptor.class);

    grpcServerRule.getServiceRegistry().addService(ServerInterceptors.intercept(echoService, serviceInterceptor));
    Channel channel = ClientInterceptors.intercept(
        grpcServerRule.getChannel(),
        callerInterceptor);
    stub = EchoServiceGrpc.newBlockingStub(channel);
  }

  @After
  public void tearDown() throws Exception {
    faker = null;
  }

  @Test
  public void echo() throws Exception {
    EchoRequest echoRequest = EchoRequest.newBuilder().setPing(faker.hacker().verb()).build();

    EchoResponse echoResponse = stub.echo(echoRequest);

    assertThat(echoResponse.getPong()).isEqualTo(echoRequest.getPing());

  }

}