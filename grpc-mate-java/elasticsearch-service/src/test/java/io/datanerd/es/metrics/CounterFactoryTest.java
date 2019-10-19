package io.datanerd.es.metrics;

import org.junit.Test;

import io.prometheus.client.Counter;

import static org.assertj.core.api.Assertions.assertThat;

public class CounterFactoryTest {

  @Test
  public void create() throws Exception {
    Counter counter1 = CounterFactory.create(
        Metric.builder()
            .setService(CounterFactoryTest.class)
            .setAction("test")
            .build()
    );
    assertThat(counter1).isNotNull();

    //test cached counter
    Counter counter2 = CounterFactory.create(
        Metric.builder()
            .setService(CounterFactoryTest.class)
            .setAction("test")
            .build()
    );
    assertThat(counter1).isSameAs(counter2);
  }

}