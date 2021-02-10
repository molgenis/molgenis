package org.molgenis.audit;

import java.time.Clock;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPublisher implements ApplicationEventPublisherAware {

  private ApplicationEventPublisher applicationEventPublisher;
  private final Clock clock = Clock.systemUTC();

  @Override
  public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher publisher) {
    this.applicationEventPublisher = publisher;
  }

  public void publish(String principal, AuditEventType type, Map<String, Object> data) {
    applicationEventPublisher.publishEvent(
        new AuditApplicationEvent(AuditEvent.create(clock.instant(), principal, type, data)));
  }
}
