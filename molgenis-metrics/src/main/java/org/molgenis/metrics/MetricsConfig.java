package org.molgenis.metrics;

import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

  @Bean
  public MeterRegistry meterRegistry() {
    return new PrometheusMeterRegistry(DEFAULT);
  }

  @Autowired // setter injection because of https://github.com/molgenis/molgenis/issues/8037
  public void setBinders(List<MeterBinder> meterBinders) {
    if (meterBinders == null) {
      return;
    }
    MeterRegistry meterRegistry = meterRegistry();
    meterBinders.forEach(binder -> binder.bindTo(meterRegistry));
  }

  @Bean
  public MolgenisTimedAspect timedAspect(MeterRegistry meterRegistry) {
    return new MolgenisTimedAspect(meterRegistry);
  }
}
