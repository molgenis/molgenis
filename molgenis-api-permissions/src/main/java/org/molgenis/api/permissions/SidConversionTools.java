package org.molgenis.api.permissions;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.security.core.SidUtils.ROLE_PREFIX;
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.molgenis.security.core.SidUtils.createUserSid;
import static org.molgenis.security.core.SidUtils.getRoleName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.api.permissions.exceptions.InsufficientInheritancePermissionsException;
import org.molgenis.api.permissions.exceptions.MissingUserOrRoleException;
import org.molgenis.api.permissions.exceptions.UserAndRoleException;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.exception.UnknownRoleException;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.SidUtils;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
public class SidConversionTools {

  private final UserService userService;
  private final DataService dataService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  SidConversionTools(
      UserService userService,
      DataService dataService,
      UserPermissionEvaluator userPermissionEvaluator) {
    this.userService = requireNonNull(userService);
    this.dataService = requireNonNull(dataService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  public List<Sid> getSids(PermissionsQuery permissionsQuery) {
    return getSids(permissionsQuery.getUsers(), permissionsQuery.getRoles());
  }

  public Sid getSid(String user, String role) {
    if (isNullOrEmpty(user) && isNullOrEmpty(role)) {
      throw new MissingUserOrRoleException();
    } else if (!isNullOrEmpty(user) && !isNullOrEmpty(role)) {
      throw new UserAndRoleException();
    } else if (!isNullOrEmpty(user)) {
      checkUserExists(user);
      return createUserSid(user);
    }
    checkRoleExists(role);
    return createRoleSid(role);
  }

  private void checkRoleExists(String role) {
    if (dataService
            .query(RoleMetadata.ROLE, Role.class)
            .eq(RoleMetadata.NAME, ROLE_PREFIX + role.toUpperCase())
            .findOne()
        == null) {
      throw new UnknownRoleException(role);
    }
  }

  private void checkUserExists(String user) {
    if (userService.getUser(user) == null) {
      throw new UnknownUserException(user);
    }
  }

  private List<Sid> getSids(List<String> users, List<String> roles) {
    List<Sid> results = new ArrayList<>();
    for (String user : users) {
      checkUserExists(user);
      results.add(createUserSid(user));
    }
    for (String role : roles) {
      checkRoleExists(role);
      results.add(createRoleSid(role));
    }
    return results;
  }

  public static String getUser(Sid sid) {
    if (sid instanceof PrincipalSid) {
      return ((PrincipalSid) sid).getPrincipal();
    }
    return null;
  }

  public static String getRole(Sid sid) {
    if (sid instanceof GrantedAuthoritySid) {
      String role = ((GrantedAuthoritySid) sid).getGrantedAuthority();
      return SidUtils.getRoleName(role);
    }
    return null;
  }

  public static String getName(Sid sid) {
    String name = getRole(sid);
    if (name == null) {
      name = getUser(sid);
    }
    if (name == null) {
      throw new IllegalStateException(
          "Sid should always be either a GrantedAuthoritySid or a PrincipalSid");
    }
    return name;
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
    String username = SidConversionTools.getUser(sid);
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
    Set sids =
        userService
            .getUsers()
            .stream()
            .map(user -> new PrincipalSid(user.getUsername()))
            .collect(toSet());

    Set roles =
        dataService
            .findAll(RoleMetadata.ROLE)
            .map(role -> new GrantedAuthoritySid(ROLE_PREFIX + role.getString(RoleMetadata.NAME)))
            .collect(toSet());

    sids.addAll(roles);

    return sids;
  }
}
