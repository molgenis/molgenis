package org.molgenis.metrics;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.Objects;

public class HikariMetrics implements MeterBinder {

  private final HikariDataSource dataSource;

  public HikariMetrics(HikariDataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource);
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    dataSource.setMetricsTrackerFactory(new MicrometerMetricsTrackerFactory(registry));
  }
}
