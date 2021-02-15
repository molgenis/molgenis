package org.molgenis.security.audit;

import static org.mockito.Mockito.verify;
import static org.molgenis.security.audit.AuthenticationEventListener.AUTHENTICATION_FAILURE;
import static org.molgenis.security.audit.AuthenticationEventListener.AUTHENTICATION_SUCCESS;
import static org.molgenis.security.audit.AuthenticationEventListener.AUTHENTICATION_SWITCH;
import static org.molgenis.security.audit.AuthenticationEventListener.LOGOUT_SUCCESS;
import static org.molgenis.security.audit.AuthenticationEventListener.SESSION_ID_CHANGE;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.session.SessionFixationProtectionEvent;
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent;

class AuthenticationEventListenerTest extends AbstractMockitoTest {

  @Mock private AuditEventPublisher auditEventPublisher;

  private AuthenticationEventListener authenticationEventListener;

  @BeforeEach
  void beforeEach() {
    authenticationEventListener = new AuthenticationEventListener(auditEventPublisher);
  }

  @Test
  void testOnAuthenticationFailureEvent() {
    Authentication authentication = new UsernamePasswordAuthenticationToken("henk", "secret");
    AuthenticationException exception =
        new BadCredentialsException("Incorrect username or password");

    AbstractAuthenticationFailureEvent event =
        new AuthenticationFailureBadCredentialsEvent(authentication, exception);

    authenticationEventListener.onAuthenticationFailureEvent(event);

    verify(auditEventPublisher)
        .publish(
            "henk",
            AUTHENTICATION_FAILURE,
            Map.of(
                "type",
                "org.springframework.security.authentication.BadCredentialsException",
                "message",
                "Incorrect username or password"));
  }

  @Test
  void testOnAuthenticationSuccessEvent() {
    Authentication authentication = new UsernamePasswordAuthenticationToken("henk", "secret");
    AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

    authenticationEventListener.onAuthenticationSuccessEvent(event);

    verify(auditEventPublisher).publish("henk", AUTHENTICATION_SUCCESS, Map.of());
  }

  @Test
  void testOnAuthenticationSwitchUserEvent() {
    Authentication authentication = new UsernamePasswordAuthenticationToken("henk", "secret");
    UserDetails details =
        new User("piet", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    AuthenticationSwitchUserEvent event =
        new AuthenticationSwitchUserEvent(authentication, details);

    authenticationEventListener.onAuthenticationSwitchUserEvent(event);

    verify(auditEventPublisher).publish("henk", AUTHENTICATION_SWITCH, Map.of("target", "piet"));
  }

  @Test
  void testLogoutSuccessEvent() {
    Authentication authentication = new UsernamePasswordAuthenticationToken("henk", "secret");
    LogoutSuccessEvent event = new LogoutSuccessEvent(authentication);

    authenticationEventListener.onLogoutSuccessEvent(event);

    verify(auditEventPublisher).publish("henk", LOGOUT_SUCCESS, Map.of());
  }

  @Test
  void testOnSessionFixationProtectionEvent() {
    Authentication authentication = new UsernamePasswordAuthenticationToken("henk", "secret");
    SessionFixationProtectionEvent event =
        new SessionFixationProtectionEvent(authentication, "CAFE", "EFAC");

    authenticationEventListener.onSessionFixationProtectionEvent(event);

    verify(auditEventPublisher)
        .publish("henk", SESSION_ID_CHANGE, Map.of("new_id", "EFAC", "old_id", "CAFE"));
  }
}
