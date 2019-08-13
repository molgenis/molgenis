package org.molgenis.metrics;

import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

  @Bean
  public PrometheusMeterRegistry meterRegistry() {
    return new PrometheusMeterRegistry(DEFAULT);
  }

  @Bean
  public MolgenisTimedAspect timedAspect(MeterRegistry meterRegistry) {
    return new MolgenisTimedAspect(meterRegistry);
  }
}
