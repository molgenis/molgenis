package org.molgenis.security.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.audit.AuditEventType.AUTHENTICATION_FAILURE;
import static org.molgenis.audit.AuditEventType.AUTHORIZATION_FAILURE;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.access.event.AuthenticationCredentialsNotFoundEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;

class AuthorizationEventListenerTest extends AbstractMockitoTest {

  @Mock private AuditEventPublisher auditEventPublisher;

  private AuthorizationEventListener authorizationEventListener;

  @BeforeEach
  void beforeEach() {
    authorizationEventListener = new AuthorizationEventListener(auditEventPublisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void onAuthenticationCredentialsNotFoundEvent() {
    AuthenticationCredentialsNotFoundEvent event = mock(
        AuthenticationCredentialsNotFoundEvent.class, RETURNS_DEEP_STUBS);

    authorizationEventListener.onAuthenticationCredentialsNotFoundEvent(event);

    verify(auditEventPublisher).publish(eq("<unknown>"), eq(AUTHENTICATION_FAILURE),
        any(Map.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void onAuthorizationFailureEvent() {
    AuthorizationFailureEvent event = mock(
        AuthorizationFailureEvent.class, RETURNS_DEEP_STUBS);
    when(event.getAuthentication().getName()).thenReturn("henk");

    authorizationEventListener.onAuthorizationFailureEvent(event);

    verify(auditEventPublisher).publish(eq("henk"), eq(AUTHORIZATION_FAILURE),
        any(Map.class));
  }
}