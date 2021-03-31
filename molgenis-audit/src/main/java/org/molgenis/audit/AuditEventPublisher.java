package org.molgenis.audit;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    var mutableData = new HashMap<>(data);
    getTransactionId().ifPresent(id -> mutableData.put("transactionId", id));

    applicationEventPublisher.publishEvent(
        new AuditApplicationEvent(
            AuditEvent.create(clock.instant(), principal, type, mutableData)));
  }

  private static Optional<String> getTransactionId() {
    return Optional.ofNullable(
        (String) TransactionSynchronizationManager.getResource("transactionId"));
  }
}
