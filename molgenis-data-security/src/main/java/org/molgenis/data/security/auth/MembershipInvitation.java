package org.molgenis.data.security.auth;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MONTHS;
import static org.molgenis.data.security.auth.MembershipInvitationMetadata.*;
import static org.molgenis.data.security.auth.MembershipInvitationMetadata.Status.EXPIRED;
import static org.molgenis.data.security.auth.MembershipInvitationMetadata.Status.PENDING;

import java.time.Instant;
import java.util.Optional;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class MembershipInvitation extends StaticEntity {
  public MembershipInvitation(Entity entity) {
    super(entity);
  }

  public MembershipInvitation(EntityType entityType) {
    super(entityType);
  }

  public MembershipInvitation(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setToken(String token) {
    set(TOKEN, token);
  }

  public String getToken() {
    return getString(TOKEN);
  }

  public void setEmail(String email) {
    set(EMAIL, email);
  }

  public String getEmail() {
    return getString(EMAIL);
  }

  public void setFrom(Instant from) {
    set(FROM, from);
  }

  public Instant getFrom() {
    return getInstant(FROM);
  }

  public void setTo(Instant to) {
    set(TO, to);
  }

  public Optional<Instant> getTo() {
    return Optional.ofNullable(getInstant(TO));
  }

  public Role getRole() {
    return getEntity(ROLE, Role.class);
  }

  public void setRole(Role role) {
    set(ROLE, role);
  }

  public void setInvitedBy(User user) {
    set(INVITED_BY, user);
  }

  public User getInvitedBy() {
    return getEntity(INVITED_BY, User.class);
  }

  public Instant getIssued() {
    return getInstant(ISSUED);
  }

  public void setLastUpdateNow() {
    set(LAST_UPDATE, now());
  }

  public Instant getLastUpdate() {
    return getInstant(LAST_UPDATE);
  }

  public void setStatus(Status status) {
    set(STATUS, status.toString());
  }

  public Status getStatus() {
    Status result = Status.valueOf(getString(STATUS));
    if (result == PENDING && getLastUpdate().plus(1, MONTHS).isBefore(now())) {
      return EXPIRED;
    }
    return result;
  }

  public void setInvitationText(String invitationText) {
    set(INVITATION_TEXT, invitationText);
  }

  public String getInvitationText() {
    return getString(INVITATION_TEXT);
  }

  public void setDeclineReason(String declineReason) {
    set(DECLINE_REASON, declineReason);
  }

  public String getDeclineReason() {
    return getString(DECLINE_REASON);
  }
}
