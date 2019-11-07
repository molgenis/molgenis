package org.molgenis.data.security.auth;

import static com.google.common.collect.Range.atLeast;
import static com.google.common.collect.Range.closedOpen;
import static java.time.Instant.now;
import static org.molgenis.data.security.auth.RoleMembership.Status.CURRENT;
import static org.molgenis.data.security.auth.RoleMembership.Status.FUTURE;
import static org.molgenis.data.security.auth.RoleMembership.Status.PAST;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.FROM;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ID;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.TO;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.USER;

import com.google.common.collect.Range;
import java.time.Instant;
import java.util.Optional;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

@SuppressWarnings("unused")
public class RoleMembership extends StaticEntity {
  public enum Status {
    FUTURE,
    CURRENT,
    PAST
  }

  public RoleMembership(Entity entity) {
    super(entity);
  }

  public RoleMembership(EntityType entityType) {
    super(entityType);
  }

  public RoleMembership(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setUser(User user) {
    set(USER, user);
  }

  public User getUser() {
    return getEntity(USER, User.class);
  }

  public void setRole(Role role) {
    set(ROLE, role);
  }

  public Role getRole() {
    return getEntity(ROLE, Role.class);
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

  public Range<Instant> getValidity() {
    return getTo().map(to -> closedOpen(getFrom(), to)).orElse(atLeast(getFrom()));
  }

  public Status getStatus() {
    if (getFrom().isAfter(now())) return FUTURE;
    return getValidity().contains(now()) ? CURRENT : PAST;
  }

  public boolean isCurrent() {
    return getStatus() == CURRENT;
  }
}
