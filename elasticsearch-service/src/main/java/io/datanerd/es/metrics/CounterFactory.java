package io.datanerd.es.metrics;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.prometheus.client.Counter;

/**
 * Create Prometheus Counter based on given Metric,
 * the CounterFactory will make sure the created Counter has unified path
 * and cache the counter as needed.
 */
public class CounterFactory {

  private static final String BASE_MODULE = "elasticserch";
  private static final LoadingCache<Metric, Counter> cachedCounter = CacheBuilder.newBuilder()
      .build(
          new CacheLoader<Metric, Counter>() {
            @Override
            public Counter load(Metric metric) throws Exception {
              final String metricName = getMetricName(metric);

              Counter.Builder countBuilder = Counter.build()
                  .name(metricName)
                  .help(metricName);

              if (metric.labels().isPresent()) {
                countBuilder.labelNames(metric.labels().get().toArray(new String[metric.labels().get().size()]));
              }

              return countBuilder.create().register();
            }
          }
      );

  private static String getMetricName(Metric metric) {
    return metric.action()
        .map(action -> String.format("%s_%s_%s", BASE_MODULE, metric.service().getSimpleName().toLowerCase(), action))
        .orElse(String.format("%s_%s", BASE_MODULE, metric.service().getSimpleName().toLowerCase()));
  }

  public static Counter create(final Metric metric) {
    return cachedCounter.getUnchecked(metric);
  }
}
