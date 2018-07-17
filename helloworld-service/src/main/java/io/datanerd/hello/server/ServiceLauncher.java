package io.datanerd.hello.server;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLauncher {

  private static Logger log = LoggerFactory.getLogger(ServiceLauncher.class); //NOPMD

  /**
   * Start the micro service.
   */
  public static void main(final String[] args) {
    try {
      final Injector injector = Guice.createInjector();
      //start grpc server
      injector
          .getInstance(GrpcServer.class)
          .start()
          // blocks until VM shutdown begins
          .awaitTermination();

    } catch (Exception ex) {
      log.error("failed to start hello world service ", ex);
      throw new IllegalStateException("failed to start hello world ", ex);
    }
  }
}
