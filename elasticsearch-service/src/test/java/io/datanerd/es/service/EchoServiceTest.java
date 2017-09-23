/*
 * Copyright (c) 2017 Practice Insight Pty Ltd.
 */

package io.datanerd.es.service;

import com.google.inject.Guice;

import com.github.javafaker.Faker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.datanerd.generated.es.EchoRequest;
import io.datanerd.generated.es.EchoResponse;
import io.datanerd.generated.es.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ivan.li@practiceinsight.io
 */
public class EchoServiceTest {

  private Faker faker;
  private Server server;
  private EchoServiceGrpc.EchoServiceBlockingStub stub;

  @Before
  public void setUp() throws Exception {
    faker = new Faker();

    EchoService echoService = Guice.createInjector().getInstance(EchoService.class);

    String serverName = faker.numerify("prod-read-server-###");
    server = InProcessServerBuilder
        .forName(serverName)
        .addService(echoService)
        .build()
        .start();
    Channel channel = InProcessChannelBuilder.forName(serverName).build();
    stub = EchoServiceGrpc.newBlockingStub(channel);
  }

  @After
  public void tearDown() throws Exception {
    faker = null;
    server.shutdownNow();
  }

  @Test
  public void echo() throws Exception {
    EchoRequest echoRequest = EchoRequest.newBuilder().setPing(faker.hacker().verb()).build();

    EchoResponse echoResponse = stub.echo(echoRequest);

    assertThat(echoResponse.getPong()).isEqualTo(echoRequest.getPing());

  }

}