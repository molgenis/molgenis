package org.molgenis.security.metrics;

import static java.util.Objects.requireNonNull;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.annotation.Nonnull;
import org.molgenis.security.permission.SecurityContextRegistryImpl;
import org.springframework.stereotype.Component;

@Component
public class SessionMetrics implements MeterBinder {

  private final SecurityContextRegistryImpl sessions;

  SessionMetrics(SecurityContextRegistryImpl sessions) {
    this.sessions = requireNonNull(sessions);
  }

  @Override
  public void bindTo(@Nonnull MeterRegistry meterRegistry) {
    Gauge.builder("spring.sessions", sessions, SessionMetrics::getSessions)
        .description("The number of sessions, including expired sessions")
        .register(meterRegistry);
  }

  private static long getSessions(SecurityContextRegistryImpl sessions) {
    return sessions.getSecurityContexts().count();
  }
}
