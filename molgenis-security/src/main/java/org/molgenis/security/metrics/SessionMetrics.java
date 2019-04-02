package org.molgenis.security.metrics;

import static java.util.Objects.requireNonNull;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import javax.annotation.PostConstruct;
import org.molgenis.security.permission.SecurityContextRegistryImpl;
import org.springframework.stereotype.Component;

@Component
public class SessionMetrics {

  private final SecurityContextRegistryImpl sessions;
  private MeterRegistry meterRegistry;

  SessionMetrics(SecurityContextRegistryImpl sessions, MeterRegistry meterRegistry) {
    this.sessions = requireNonNull(sessions);
    this.meterRegistry = requireNonNull(meterRegistry);
  }

  @PostConstruct
  public void bindToRegistry() {
    Gauge.builder("spring.sessions", sessions, SessionMetrics::getSessions)
        .description("The number of sessions, including expired sessions")
        .register(meterRegistry);
  }

  private static long getSessions(SecurityContextRegistryImpl sessions) {
    return sessions.getSecurityContexts().count();
  }
}
