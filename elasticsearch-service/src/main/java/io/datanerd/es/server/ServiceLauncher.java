package io.datanerd.es.server;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datanerd.es.guice.ElasticSearchModule;

public class ServiceLauncher {

  private static final Logger log = LoggerFactory.getLogger(ServiceLauncher.class);

  public static void main(final String[] args) {
    try {
      final Injector injector =
          Guice.createInjector(new ElasticSearchModule());
      injector
          .getInstance(GrpcServer.class)
          .start()
          // blocks until VM shutdown begins
          .awaitTermination();

    } catch (Exception ex) {
      log.error("failed to start ip-elasticsearch service ", ex);
      throw new IllegalStateException("failed to start elasticsearch service ", ex);
    }
  }
}
