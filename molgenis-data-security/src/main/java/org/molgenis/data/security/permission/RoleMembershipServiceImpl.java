package org.molgenis.data.security.permission;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.UserMetaData.USERNAME;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.stereotype.Component;

@Component
public class RoleMembershipServiceImpl implements RoleMembershipService {
  private final UserService userService;
  private final RoleMembershipFactory roleMembershipFactory;
  private final DataService dataService;
  private final UserMetaData userMetaData;
  private final RoleMetadata roleMetadata;

  RoleMembershipServiceImpl(
      UserService userService,
      RoleMembershipFactory roleMembershipFactory,
      DataService dataService,
      UserMetaData userMetaData,
      RoleMetadata roleMetadata) {
    this.userService = requireNonNull(userService);
    this.roleMembershipFactory = requireNonNull(roleMembershipFactory);
    this.dataService = requireNonNull(dataService);
    this.userMetaData = requireNonNull(userMetaData);
    this.roleMetadata = requireNonNull(roleMetadata);
  }

  @RunAsSystem
  @Override
  public void addUserToRole(String username, String roleName) {
    User user = userService.getUser(username);
    if (user == null) {
      throw new UnknownEntityException(userMetaData, userMetaData.getAttribute(USERNAME), username);
    }

    Role role = dataService.query(RoleMetadata.ROLE, Role.class).eq(NAME, roleName).findOne();
    if (role == null) {
      throw new UnknownEntityException(roleMetadata, roleMetadata.getAttribute(NAME), roleName);
    }

    this.addUserToRole(user, role);
  }

  @RunAsSystem
  @Override
  public void addUserToRole(final User user, final Role role) {
    RoleMembership roleMembership = roleMembershipFactory.create();
    roleMembership.setUser(user);
    roleMembership.setFrom(Instant.now());
    roleMembership.setRole(role);

    dataService.add(ROLE_MEMBERSHIP, roleMembership);
  }

  @RunAsSystem
  @Override
  public void removeMembership(final RoleMembership roleMembership) {
    dataService.delete(ROLE_MEMBERSHIP, roleMembership);
  }

  @RunAsSystem
  @Override
  public void updateMembership(RoleMembership roleMembership, Role newRole) {
    final RoleMembership membership =
        dataService.findOneById(
            RoleMembershipMetadata.ROLE_MEMBERSHIP, roleMembership.getId(), RoleMembership.class);
    if (membership == null) {
      throw new UnknownEntityException(roleMetadata, roleMembership.getId());
    }
    membership.setRole(newRole);
    dataService.update(RoleMembershipMetadata.ROLE_MEMBERSHIP, membership);
  }

  @RunAsSystem
  @Override
  public Collection<RoleMembership> getMemberships(Collection<Role> roles) {
    Fetch roleFetch = new Fetch().field(RoleMetadata.NAME).field(RoleMetadata.LABEL);
    Fetch userFetch = new Fetch().field(UserMetaData.USERNAME).field(UserMetaData.ID);
    Fetch fetch =
        new Fetch()
            .field(RoleMembershipMetadata.ROLE, roleFetch)
            .field(RoleMembershipMetadata.USER, userFetch)
            .field(RoleMembershipMetadata.FROM)
            .field(RoleMembershipMetadata.TO)
            .field(RoleMembershipMetadata.ID);

    return dataService
        .query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class)
        .in(RoleMembershipMetadata.ROLE, roles)
        .fetch(fetch)
        .findAll()
        .filter(RoleMembership::isCurrent)
        .collect(Collectors.toList());
  }
}
