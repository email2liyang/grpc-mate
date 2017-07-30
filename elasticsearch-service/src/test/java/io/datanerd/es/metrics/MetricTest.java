package io.datanerd.es.metrics;

import com.google.common.collect.ImmutableList;

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
        .setLabels(ImmutableList.of("labela", "labelb"))
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

  @Test
  public void labels() throws Exception {
    assertThat(metric.labels()).isNotEmpty();
    assertThat(metric.labels().get()).contains("labela", "labelb");
  }
}