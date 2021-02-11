package org.molgenis.audit;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditEventLoggerTest {

  AuditEventLogger auditEventLogger;

  @BeforeEach
  void beforeEach() {
    auditEventLogger = new AuditEventLogger();
  }

  @Test
  void testLogAuditEvent() {
    AuditEvent auditEvent =
        AuditEvent.create(Instant.EPOCH, "henk", AuditEventType.AUTHENTICATION_FAILURE, emptyMap());
    AuditApplicationEvent auditApplicationEvent = new AuditApplicationEvent(auditEvent);

    auditEventLogger.onAuditApplicationEvent(auditApplicationEvent);

    assertEquals(1, TestAppender.events.size());
    // TODO check formatted message
  }
}
