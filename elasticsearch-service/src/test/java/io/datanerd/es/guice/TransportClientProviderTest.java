package io.datanerd.es.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;

import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_HOST;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_NAME;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_PORT;
import static org.assertj.core.api.Assertions.assertThat;

public class TransportClientProviderTest {

  private static Logger log = LoggerFactory.getLogger(TransportClientProviderTest.class); //NOPMD
  private TransportClientProvider transportClientProvider;
  @ClassRule
  public static GenericContainer esContainer = new GenericContainer("email2liyang/elasticsearch-unit-image:5.4.3")
      .withExposedPorts(9200,9300);

  @Before
  public void setUp() throws Exception {
    String ip = esContainer.getContainerIpAddress();
    Integer transportPort = esContainer.getMappedPort(9300);
    MapConfiguration memoryParams = new MapConfiguration(new HashMap<>());
    memoryParams.setProperty(CONFIG_ES_CLUSTER_HOST,ip);
    memoryParams.setProperty(CONFIG_ES_CLUSTER_PORT,transportPort);
    memoryParams.setProperty(CONFIG_ES_CLUSTER_NAME,"elasticsearch");
    Injector injector = Guice.createInjector(
        Modules.override(new ElasticSearchModule()).with(
            binder -> {
              binder.bind(Configuration.class).toInstance(memoryParams);
            }
        )
    );
    transportClientProvider = injector.getInstance(TransportClientProvider.class);
  }

  @Test
  public void get() throws Exception {
    TransportClient transportClient = transportClientProvider.get();
    assertThat(transportClient).isNotNull();
  }

}