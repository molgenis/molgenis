package org.molgenis.audit;

import static java.util.Objects.requireNonNull;

import org.springframework.context.ApplicationEvent;

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
