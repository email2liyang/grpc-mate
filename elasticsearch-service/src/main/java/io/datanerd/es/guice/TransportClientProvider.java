package io.datanerd.es.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.apache.commons.configuration2.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_HOST;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_NAME;
import static io.datanerd.es.guice.Constants.CONFIG_ES_CLUSTER_PORT;

public class TransportClientProvider implements Provider<TransportClient> {

  private static Logger log = LoggerFactory.getLogger(TransportClientProvider.class); //NOPMD

  @Inject
  private Configuration configuration;

  /**
   * TransportClient provider.
   * @return TransportClient
   */
  public TransportClient get() {
    final String hostCsv = configuration.getString(CONFIG_ES_CLUSTER_HOST);
    final List<String> hosts = Splitter.on(",").splitToList(hostCsv);
    Preconditions.checkState(!hosts.isEmpty());

    final TransportClient transportClient = new PreBuiltTransportClient(esSettings());
    final Integer esTransportPort = configuration.getInteger(CONFIG_ES_CLUSTER_PORT, 9300);
    log.info("connect to elastic search {} on port {} ", hostCsv, esTransportPort);

    hosts.forEach(
        host -> transportClient.addTransportAddress(
            new InetSocketTransportAddress(new InetSocketAddress(host, esTransportPort))
        )
    );

    return transportClient;
  }

  private Settings esSettings() {
    return Settings.builder()
        .put("client.transport.ping_timeout", "60s")
        .put("client.transport.nodes_sampler_interval", "120s")
        .put("cluster.name",
             configuration.getString(CONFIG_ES_CLUSTER_NAME, "elasticsearch"))
        .build();
  }
}
