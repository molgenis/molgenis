package org.molgenis.data.security.auth;

import static com.google.common.collect.Range.atLeast;
import static com.google.common.collect.Range.closedOpen;
import static java.time.Instant.now;
import static org.molgenis.data.security.auth.VOGroupRoleMembership.Status.CURRENT;
import static org.molgenis.data.security.auth.VOGroupRoleMembership.Status.FUTURE;
import static org.molgenis.data.security.auth.VOGroupRoleMembership.Status.PAST;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.FROM;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.ID;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.ROLE;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.TO;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.VO_GROUP;

import com.google.common.collect.Range;
import java.time.Instant;
import java.util.Optional;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

@SuppressWarnings("unused")
public class VOGroupRoleMembership extends StaticEntity {
  public enum Status {
    FUTURE,
    CURRENT,
    PAST
  }

  public VOGroupRoleMembership(Entity entity) {
    super(entity);
  }

  public VOGroupRoleMembership(EntityType entityType) {
    super(entityType);
  }

  public VOGroupRoleMembership(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setVOGroup(VOGroup group) {
    set(VO_GROUP, group);
  }

  public VOGroup getVOGroup() {
    return getEntity(VO_GROUP, VOGroup.class);
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
