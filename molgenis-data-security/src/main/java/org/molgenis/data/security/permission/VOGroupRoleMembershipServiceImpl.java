package org.molgenis.data.security.permission;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.FROM;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.ID;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.ROLE;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.TO;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.VOGROUP_ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.VO_GROUP;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.VOGroup;
import org.molgenis.data.security.auth.VOGroupMetadata;
import org.molgenis.data.security.auth.VOGroupRoleMembership;
import org.molgenis.data.security.auth.VOGroupRoleMembershipFactory;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.stereotype.Service;

@Service
public class VOGroupRoleMembershipServiceImpl implements VOGroupRoleMembershipService {

  private final DataService dataService;
  private final VOGroupRoleMembershipFactory voGroupRoleMembershipFactory;
  private final Fetch fetch;

  public VOGroupRoleMembershipServiceImpl(
      DataService dataService, VOGroupRoleMembershipFactory voGroupRoleMembershipFactory) {
    this.dataService = requireNonNull(dataService);
    this.voGroupRoleMembershipFactory = requireNonNull(voGroupRoleMembershipFactory);
    var roleFetch = new Fetch().field(RoleMetadata.NAME).field(RoleMetadata.LABEL);
    var voGroupFetch = new Fetch().field(VOGroupMetadata.NAME).field(VOGroupMetadata.ID);
    fetch =
        new Fetch()
            .field(ROLE, roleFetch)
            .field(VO_GROUP, voGroupFetch)
            .field(FROM)
            .field(TO)
            .field(ID);
  }

  @RunAsSystem
  @Override
  public Collection<VOGroupRoleMembership> getCurrentMemberships(Collection<VOGroup> groups) {
    if (groups.isEmpty()) {
      return emptyList();
    }
    return dataService
        .query(VOGROUP_ROLE_MEMBERSHIP, VOGroupRoleMembership.class)
        .fetch(fetch)
        .in(VO_GROUP, groups)
        .findAll()
        .filter(VOGroupRoleMembership::isCurrent)
        .collect(Collectors.toList());
  }

  @RunAsSystem
  @Override
  public Collection<VOGroupRoleMembership> getMemberships(Collection<Role> roles) {
    if (roles.isEmpty()) {
      return emptyList();
    }
    return dataService
        .query(VOGROUP_ROLE_MEMBERSHIP, VOGroupRoleMembership.class)
        .fetch(fetch)
        .in(ROLE, roles)
        .findAll()
        .filter(VOGroupRoleMembership::isCurrent)
        .collect(Collectors.toList());
  }

  @RunAsSystem
  @Override
  public void add(VOGroup voGroup, Role role) {
    var membership = voGroupRoleMembershipFactory.create();
    membership.setVOGroup(voGroup);
    membership.setFrom(Instant.now());
    membership.setRole(role);

    dataService.add(VOGROUP_ROLE_MEMBERSHIP, membership);
  }

  @RunAsSystem
  @Override
  public void removeMembership(String id) {
    final var membership =
        dataService.findOneById(VOGROUP_ROLE_MEMBERSHIP, id, fetch, VOGroupRoleMembership.class);

    if (membership == null) {
      throw new UnknownEntityException(VOGROUP_ROLE_MEMBERSHIP, id);
    }
    membership.setTo(Instant.now());
    dataService.update(VOGROUP_ROLE_MEMBERSHIP, membership);
  }

  @RunAsSystem
  @Override
  public void updateMembership(String id, Role newRole) {
    final var membership =
        dataService.findOneById(VOGROUP_ROLE_MEMBERSHIP, id, fetch, VOGroupRoleMembership.class);
    if (membership == null) {
      throw new UnknownEntityException(VOGROUP_ROLE_MEMBERSHIP, id);
    }
    membership.setTo(Instant.now());
    dataService.update(VOGROUP_ROLE_MEMBERSHIP, membership);
    add(membership.getVOGroup(), newRole);
  }
}
