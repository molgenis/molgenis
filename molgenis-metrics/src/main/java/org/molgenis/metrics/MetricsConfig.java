package org.molgenis.metrics;

import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

  private final Collection<MeterBinder> binders;

  @Autowired
  public MetricsConfig(List<MeterBinder> binders) {
    this.binders = (binders != null ? binders : Collections.emptyList());
  }

  @Bean
  public MeterRegistry meterRegistry() {
    MeterRegistry result = new PrometheusMeterRegistry(DEFAULT);
    binders.forEach(binder -> binder.bindTo(result));
    return result;
  }

  @Bean
  public MolgenisTimedAspect timedAspect(MeterRegistry meterRegistry) {
    return new MolgenisTimedAspect(meterRegistry);
  }
}
