package org.molgenis.security.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.audit.AuthenticationEventListener.AUTHENTICATION_FAILURE;
import static org.molgenis.security.audit.AuthenticationEventListener.AUTHENTICATION_SUCCESS;
import static org.molgenis.security.audit.AuthenticationEventListener.AUTHENTICATION_SWITCH;
import static org.molgenis.security.audit.AuthenticationEventListener.LOGOUT_SUCCESS;
import static org.molgenis.security.audit.AuthenticationEventListener.SESSION_ID_CHANGE;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.authentication.session.SessionFixationProtectionEvent;
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent;

class AuthenticationEventListenerTest extends AbstractMockitoTest {

  @Mock private AuditEventPublisher auditEventPublisher;

  private AuthenticationEventListener authenticationEventListener;

  @BeforeEach
  void beforeEach() {
    authenticationEventListener = new AuthenticationEventListener(auditEventPublisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testOnAuthenticationFailureEvent() {
    AbstractAuthenticationFailureEvent event =
        mock(AbstractAuthenticationFailureEvent.class, RETURNS_DEEP_STUBS);
    when(event.getAuthentication().getName()).thenReturn("henk");

    authenticationEventListener.onAuthenticationFailureEvent(event);

    verify(auditEventPublisher).publish(eq("henk"), eq(AUTHENTICATION_FAILURE), any(Map.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testOnAuthenticationSuccessEvent() {
    AuthenticationSuccessEvent event = mock(AuthenticationSuccessEvent.class, RETURNS_DEEP_STUBS);
    when(event.getAuthentication().getName()).thenReturn("henk");

    authenticationEventListener.onAuthenticationSuccessEvent(event);

    verify(auditEventPublisher).publish(eq("henk"), eq(AUTHENTICATION_SUCCESS), any(Map.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testOnAuthenticationSwitchUserEvent() {
    AuthenticationSwitchUserEvent event =
        mock(AuthenticationSwitchUserEvent.class, RETURNS_DEEP_STUBS);
    when(event.getAuthentication().getName()).thenReturn("henk");

    authenticationEventListener.onAuthenticationSwitchUserEvent(event);

    verify(auditEventPublisher).publish(eq("henk"), eq(AUTHENTICATION_SWITCH), any(Map.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testLogoutSuccessEvent() {
    LogoutSuccessEvent event = mock(LogoutSuccessEvent.class, RETURNS_DEEP_STUBS);
    when(event.getAuthentication().getName()).thenReturn("henk");

    authenticationEventListener.onLogoutSuccessEvent(event);

    verify(auditEventPublisher).publish(eq("henk"), eq(LOGOUT_SUCCESS), any(Map.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testOnSessionFixationProtectionEvent() {
    SessionFixationProtectionEvent event =
        mock(SessionFixationProtectionEvent.class, RETURNS_DEEP_STUBS);
    when(event.getAuthentication().getName()).thenReturn("henk");

    authenticationEventListener.onSessionFixationProtectionEvent(event);

    verify(auditEventPublisher).publish(eq("henk"), eq(SESSION_ID_CHANGE), any(Map.class));
  }
}
