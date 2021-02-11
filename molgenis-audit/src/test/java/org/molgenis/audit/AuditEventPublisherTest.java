package org.molgenis.audit;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationEventPublisher;

class AuditEventPublisherTest extends AbstractMockitoTest {

  @Mock private ApplicationEventPublisher applicationEventPublisher;

  @Captor ArgumentCaptor<AuditApplicationEvent> eventCaptor;

  private AuditEventPublisher auditEventPublisher;

  @BeforeEach
  void beforeEach() {
    auditEventPublisher = new AuditEventPublisher();
    auditEventPublisher.setApplicationEventPublisher(applicationEventPublisher);
  }

  @Test
  void testPublish() {
    auditEventPublisher.publish("henk", AuditEventType.AUTHENTICATION_SUCCESS, emptyMap());

    verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());
    AuditEvent auditEvent = eventCaptor.getValue().getAuditEvent();
    assertEquals("henk", auditEvent.getPrincipal());
    assertEquals(AuditEventType.AUTHENTICATION_SUCCESS, auditEvent.getType());
    assertEquals(emptyMap(), auditEvent.getData());
  }
}
