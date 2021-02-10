package org.molgenis.security.audit;

import static java.util.Objects.requireNonNull;
import static org.molgenis.audit.AuditEventType.AUTHENTICATION_FAILURE;
import static org.molgenis.audit.AuditEventType.AUTHORIZATION_FAILURE;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.audit.AuditEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthenticationCredentialsNotFoundEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationEventListener {

  private final AuditEventPublisher auditEventPublisher;

  public AuthorizationEventListener(AuditEventPublisher auditEventPublisher) {
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @EventListener
  public void onAuthenticationCredentialsNotFoundEvent(
      AuthenticationCredentialsNotFoundEvent event) {
    Map<String, Object> data = new HashMap<>();
    data.put("type", event.getCredentialsNotFoundException().getClass().getName());
    data.put("message", event.getCredentialsNotFoundException().getMessage());
    auditEventPublisher.publish("<unknown>", AUTHENTICATION_FAILURE, data);
  }

  @EventListener
  public void onAuthorizationFailureEvent(AuthorizationFailureEvent event) {
    Map<String, Object> data = new HashMap<>();
    data.put("type", event.getAccessDeniedException().getClass().getName());
    data.put("message", event.getAccessDeniedException().getMessage());
    if (event.getAuthentication().getDetails() != null) {
      data.put("details", event.getAuthentication().getDetails());
    }
    auditEventPublisher.publish(event.getAuthentication().getName(), AUTHORIZATION_FAILURE, data);
  }
}
