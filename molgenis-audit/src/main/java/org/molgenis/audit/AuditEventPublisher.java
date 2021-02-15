package org.molgenis.audit;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;
  private final Clock clock = Clock.systemUTC();

  public AuditEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = requireNonNull(applicationEventPublisher);
  }

  /**
   * Publish an audit event.
   *
   * @param principal the principal's name
   * @param type the audit event type
   * @param data additional information pertaining to the event
   */
  public void publish(@Nullable String principal, String type, Map<String, Object> data) {
    if (principal == null) {
      principal = "<unknown>";
    }

    applicationEventPublisher.publishEvent(
        new AuditApplicationEvent(AuditEvent.create(clock.instant(), principal, type, data)));
  }
}
