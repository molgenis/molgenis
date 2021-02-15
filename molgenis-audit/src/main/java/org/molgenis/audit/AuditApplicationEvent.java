package org.molgenis.audit;

import static java.util.Objects.requireNonNull;

import org.springframework.context.ApplicationEvent;

/** Wraps an {@link AuditEvent} so that it can be used by Spring's event system. */
public class AuditApplicationEvent extends ApplicationEvent {

  private final AuditEvent auditEvent;

  public AuditApplicationEvent(AuditEvent auditEvent) {
    super(auditEvent);
    this.auditEvent = requireNonNull(auditEvent);
  }

  public AuditEvent getAuditEvent() {
    return auditEvent;
  }
}
