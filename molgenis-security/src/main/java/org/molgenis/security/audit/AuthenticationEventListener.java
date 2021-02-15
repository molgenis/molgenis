package org.molgenis.security.audit;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.audit.AuditEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.authentication.session.SessionFixationProtectionEvent;
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent;
import org.springframework.stereotype.Component;

/**
 * Catches some of Spring's {@link AbstractAuthenticationEvent}s and publishes them as audit events.
 */
@Component
public class AuthenticationEventListener {

  public static final String AUTHENTICATION_FAILURE = "AUTHENTICATION_FAILURE";
  public static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";
  public static final String AUTHENTICATION_SWITCH = "AUTHENTICATION_SWITCH";
  public static final String LOGOUT_SUCCESS = "LOGOUT_SUCCESS";
  public static final String SESSION_ID_CHANGE = "SESSION_ID_CHANGE";

  private final AuditEventPublisher auditEventPublisher;

  public AuthenticationEventListener(AuditEventPublisher auditEventPublisher) {
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @EventListener
  public void onAuthenticationFailureEvent(AbstractAuthenticationFailureEvent event) {
    Map<String, Object> data = new HashMap<>();
    data.put("type", event.getException().getClass().getName());
    data.put("message", event.getException().getMessage());
    if (event.getAuthentication().getDetails() != null) {
      data.put("details", event.getAuthentication().getDetails());
    }
    auditEventPublisher.publish(event.getAuthentication().getName(), AUTHENTICATION_FAILURE, data);
  }

  @EventListener
  public void onAuthenticationSuccessEvent(AuthenticationSuccessEvent event) {
    Map<String, Object> data = new HashMap<>();
    if (event.getAuthentication().getDetails() != null) {
      data.put("details", event.getAuthentication().getDetails());
    }
    auditEventPublisher.publish(event.getAuthentication().getName(), AUTHENTICATION_SUCCESS, data);
  }

  @EventListener
  public void onAuthenticationSwitchUserEvent(AuthenticationSwitchUserEvent event) {
    Map<String, Object> data = new HashMap<>();
    if (event.getAuthentication().getDetails() != null) {
      data.put("details", event.getAuthentication().getDetails());
    }
    if (event.getTargetUser() != null) {
      data.put("target", event.getTargetUser().getUsername());
    }
    auditEventPublisher.publish(event.getAuthentication().getName(), AUTHENTICATION_SWITCH, data);
  }

  @EventListener
  public void onLogoutSuccessEvent(LogoutSuccessEvent event) {
    Map<String, Object> data = new HashMap<>();
    if (event.getAuthentication().getDetails() != null) {
      data.put("details", event.getAuthentication().getDetails());
    }
    auditEventPublisher.publish(event.getAuthentication().getName(), LOGOUT_SUCCESS, data);
  }

  @EventListener
  public void onSessionFixationProtectionEvent(SessionFixationProtectionEvent event) {
    Map<String, Object> data = new HashMap<>();
    data.put("old_id", event.getOldSessionId());
    data.put("new_id", event.getNewSessionId());
    if (event.getAuthentication().getDetails() != null) {
      data.put("details", event.getAuthentication().getDetails());
    }
    auditEventPublisher.publish(event.getAuthentication().getName(), SESSION_ID_CHANGE, data);
  }
}
