package org.molgenis.security.audit;

import static org.mockito.Mockito.verify;
import static org.molgenis.security.audit.AuthenticationEventListener.AUTHENTICATION_FAILURE;
import static org.molgenis.security.audit.AuthorizationEventListener.AUTHORIZATION_FAILURE;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.event.AuthenticationCredentialsNotFoundEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class AuthorizationEventListenerTest extends AbstractMockitoTest {

  @Mock private AuditEventPublisher auditEventPublisher;

  private AuthorizationEventListener authorizationEventListener;

  @BeforeEach
  void beforeEach() {
    authorizationEventListener = new AuthorizationEventListener(auditEventPublisher);
  }

  @Test
  void onAuthenticationCredentialsNotFoundEvent() {
    AuthenticationCredentialsNotFoundEvent event =
        new AuthenticationCredentialsNotFoundEvent(
            getClass(),
            List.of(new SecurityConfig("isAuthenticated()")),
            new AuthenticationCredentialsNotFoundException("Not authenticated."));

    authorizationEventListener.onAuthenticationCredentialsNotFoundEvent(event);

    verify(auditEventPublisher)
        .publish(
            null,
            AUTHENTICATION_FAILURE,
            Map.of(
                "type",
                "org.springframework.security.authentication.AuthenticationCredentialsNotFoundException",
                "message",
                "Not authenticated."));
  }

  @Test
  void onAuthorizationFailureEvent() {
    Authentication authentication = new UsernamePasswordAuthenticationToken("henk", "secret");
    AuthorizationFailureEvent event =
        new AuthorizationFailureEvent(
            getClass(),
            List.of(new SecurityConfig("hasRole('user')")),
            authentication,
            new AccessDeniedException("Access denied"));

    authorizationEventListener.onAuthorizationFailureEvent(event);

    verify(auditEventPublisher)
        .publish(
            "henk",
            AUTHORIZATION_FAILURE,
            Map.of(
                "type",
                "org.springframework.security.access.AccessDeniedException",
                "message",
                "Access denied",
                "object",
                "class org.molgenis.security.audit.AuthorizationEventListenerTest"));
  }
}
