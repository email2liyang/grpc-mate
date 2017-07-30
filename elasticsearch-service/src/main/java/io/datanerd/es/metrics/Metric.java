package io.datanerd.es.metrics;

import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Optional;

@AutoValue
public abstract class Metric {

  abstract Class service();

  abstract String action();

  abstract Optional<List<String>> labels();

  static Builder builder() {
    return new AutoValue_Metric.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder setService(Class clazz);

    abstract Builder setAction(String action);

    abstract Builder setLabels(List<String> lables);

    abstract Metric build();
  }
}
