package org.molgenis.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class AuditApplicationEventTest {

  @Test
  void testGetter() {
    AuditEvent auditEvent = mock(AuditEvent.class);
    AuditApplicationEvent event = new AuditApplicationEvent(auditEvent);

    assertEquals(auditEvent, event.getAuditEvent());
  }
}
