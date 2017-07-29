package io.datanerd.es.metrics;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricTest {

  private Metric metric;

  @Before
  public void setUp() throws Exception {
    metric = Metric
        .builder()
        .setService(MetricTest.class)
        .setAction("test")
        .build();
  }

  @Test
  public void service() throws Exception {
    assertThat(metric.service()).isAssignableFrom(MetricTest.class);
  }

  @Test
  public void action() throws Exception {
    assertThat(metric.action()).isEqualTo("test");
  }
}