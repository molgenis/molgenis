package org.molgenis.audit;

import com.google.auto.value.AutoValue;
import java.time.Instant;
import java.util.Map;

@AutoValue
public abstract class AuditEvent {

  public abstract Instant getTimestamp();

  public abstract String getPrincipal();

  public abstract AuditEventType getType();

  public abstract Map<String, Object> getData();

  public static AuditEvent create(
      Instant timestamp, String principal, AuditEventType type, Map<String, Object> data) {
    return new AutoValue_AuditEvent(timestamp, principal, type, data);
  }
}
