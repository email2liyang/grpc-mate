package io.datanerd.es.metrics;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Metric {

  abstract Class service();

  abstract String action();

  static Builder builder() {
    return new AutoValue_Metric.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder setService(Class clazz);

    abstract Builder setAction(String action);

    abstract Metric build();
  }
}
