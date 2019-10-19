package io.datanerd.es.metrics;

import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Optional;

@AutoValue
public abstract class Metric {

  abstract Class service();

  abstract Optional<String> action();

  abstract Optional<List<String>> labels();

  public static Builder builder() {
    return new AutoValue_Metric.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setService(Class clazz);

    public abstract Builder setAction(String action);

    public abstract Builder setLabels(List<String> lables);

    public abstract Metric build();
  }
}
