package org.molgenis.api.permissions.inheritance;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.security.core.SidUtils.getRoleName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.api.permissions.SidConversionUtils;
import org.molgenis.api.permissions.exceptions.InsufficientInheritancePermissionsException;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.SidUtils;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
public class UserRoleInheritanceResolver {

  private UserPermissionEvaluator userPermissionEvaluator;
  private DataService dataService;
  private UserService userService;

  public UserRoleInheritanceResolver(
      UserPermissionEvaluator userPermissionEvaluator,
      DataService dataService,
      UserService userService) {
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
    this.dataService = requireNonNull(dataService);
    this.userService = requireNonNull(userService);
  }

  public List<Sid> getRolesForSid(Sid sid) {
    List<Sid> roles = new ArrayList<>();
    if (sid instanceof PrincipalSid) {
      roles = getRolesForUser(sid);
    } else if (sid instanceof GrantedAuthoritySid) {
      String role = ((GrantedAuthoritySid) sid).getGrantedAuthority();
      roles = getParentRoles(getRoleName(role));
    }
    return roles;
  }

  private List<Sid> getParentRoles(String roleName) {
    if (userPermissionEvaluator.hasPermission(
        new EntityTypeIdentity(RoleMetadata.ROLE), EntityTypePermission.READ_DATA)) {
      Role role =
          dataService
              .getRepository(RoleMetadata.ROLE, Role.class)
              .query()
              .eq(RoleMetadata.NAME, roleName)
              .findOne();
      if (role == null) {
        throw new UnknownEntityException(RoleMetadata.ROLE, roleName);
      }
      return StreamSupport.stream(role.getIncludes().spliterator(), false)
          .map(parentRole -> SidUtils.createRoleSid(parentRole.getName()))
          .collect(Collectors.toList());
    } else {
      throw new InsufficientInheritancePermissionsException();
    }
  }

  private List<Sid> getRolesForUser(Sid sid) {
    String username = SidConversionUtils.getUser(sid);
    if (userPermissionEvaluator.hasPermission(
        new EntityTypeIdentity(RoleMembershipMetadata.ROLE_MEMBERSHIP),
        EntityTypePermission.READ_DATA)) {
      User user = userService.getUser(username);
      if (user == null) {
        throw new UnknownUserException(username);
      }
      return dataService
          .getRepository(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class)
          .query()
          .eq(RoleMembershipMetadata.USER, user.getId())
          .findAll()
          .map(roleMembership -> SidUtils.createRoleSid(roleMembership.getRole().getName()))
          .collect(Collectors.toList());
    } else {
      throw new InsufficientInheritancePermissionsException();
    }
  }

  public Set<Sid> getRoles(Sid sid) {
    Set<Sid> roles = new LinkedHashSet<>();
    resolveRoles(sid, roles);
    return roles;
  }

  public List<Sid> getRoles(Set<Sid> sids) {
    List<Sid> roles = new LinkedList<>();
    sids.forEach(sid -> roles.addAll(getRoles(sid)));
    return roles;
  }

  private void resolveRoles(Sid sid, Set<Sid> roles) {
    List<Sid> inheritedRoles = getRolesForSid(sid);
    roles.addAll(inheritedRoles);
    for (Sid role : inheritedRoles) {
      resolveRoles(role, roles);
    }
  }

  public Set<Sid> getAllAvailableSids() {
    return userService
        .getUsers()
        .stream()
        .map(user -> new PrincipalSid(user.getUsername()))
        .collect(toSet());
  }
}
