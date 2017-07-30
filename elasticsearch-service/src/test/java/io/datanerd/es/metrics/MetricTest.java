package io.datanerd.es.metrics;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
    assertThat(metric.action().get()).isEqualTo("test");
  }

  @Test
  public void labels() throws Exception {
    assertThat(metric.labels()).isNotEmpty();
    assertThat(metric.labels().get()).contains("labela", "labelb");
  }

  @Test
  public void optional_field() throws Exception {
    metric = Metric
        .builder()
        .setService(MetricTest.class)
        .setAction("test")
        .build();
    assertThat(metric.labels()).isEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void mandatory_field() throws Exception {
    metric = Metric
        .builder()
        .build();
    fail("should failed as mandatory field is missing");
  }
}