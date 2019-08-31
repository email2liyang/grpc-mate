package io.datanerd.es.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.protobuf.util.JsonFormat;

import org.apache.commons.configuration2.Configuration;
import org.elasticsearch.client.transport.TransportClient;

/**
 * Main Guice Config Module
 *
 * @author email2liyang@gmail.com
 */
public class ElasticSearchModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Configuration.class).toProvider(ConfigurationProvider.class).in(Singleton.class);
    bind(TransportClient.class).toProvider(TransportClientProvider.class).in(Singleton.class);
    bind(JsonFormat.Printer.class).toInstance(JsonFormat.printer());
    bind(JsonFormat.Parser.class).toInstance(JsonFormat.parser());
  }
}
