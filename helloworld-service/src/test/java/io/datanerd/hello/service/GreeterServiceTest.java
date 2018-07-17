/*
 * Copyright (c) 2018 Practice Insight Pty Ltd.
 */

package io.datanerd.hello.service;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.github.javafaker.Faker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.datanerd.generated.helloworld.GreeterGrpc;
import io.datanerd.generated.helloworld.HelloReply;
import io.datanerd.generated.helloworld.HelloRequest;
import io.grpc.testing.GrpcServerRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ivan
 */
public class GreeterServiceTest {

  private Faker faker;
  private GreeterGrpc.GreeterBlockingStub stub;

  @Rule
  public GrpcServerRule grpcServerRule = new GrpcServerRule();

  @Before
  public void setUp() throws Exception {
    faker = new Faker();
    Injector injector = Guice.createInjector();
    GreeterService greeterService = injector.getInstance(GreeterService.class);

    grpcServerRule.getServiceRegistry().addService(greeterService);
    stub = GreeterGrpc.newBlockingStub(grpcServerRule.getChannel());
  }

  @After
  public void tearDown() throws Exception {
    faker = null;
  }

  @Test
  public void sayHello() {
    String name = faker.superhero().name();
    HelloRequest request = HelloRequest.newBuilder().setName(name).build();
    HelloReply helloReply = stub.sayHello(request);
    assertThat(helloReply.getMessage()).isEqualTo("hello " + name);
  }
}