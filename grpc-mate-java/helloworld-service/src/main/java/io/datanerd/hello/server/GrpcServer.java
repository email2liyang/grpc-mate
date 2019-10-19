package io.datanerd.hello.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import io.datanerd.hello.service.GreeterService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;


@Singleton
class GrpcServer {

  private static Logger log = LoggerFactory.getLogger(GrpcServer.class); //NOPMD
  @Inject
  private GreeterService greeterService;

  /**
   * Start Netty Grpc Server.
   *
   * @return Server gRPC Server
   * @throws IOException - when something went wrong starting the grpc server
   */
  final Server start() throws IOException {
    final int port = 8080;

    log.info("Starting grpc server on port '{}'...", port);
    final Server server =
        NettyServerBuilder
            .forPort(port)
            .addService(greeterService)
            .build();

    server.start();
    log.info("grpc (port={}) server started successfully.", port);

    return server;
  }
}
