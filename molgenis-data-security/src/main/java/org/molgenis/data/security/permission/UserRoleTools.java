package org.molgenis.data.security.permission;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.molgenis.security.core.SidUtils.ROLE_PREFIX;
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.molgenis.security.core.SidUtils.createUserSid;
import static org.molgenis.security.core.SidUtils.getRoleName;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetadata;
import org.molgenis.data.security.exception.InsufficientInheritancePermissionsException;
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
public class UserRoleTools {

  public static final String ANONYMOUS = "ANONYMOUS";
  private final UserService userService;
  private final DataService dataService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  UserRoleTools(
      UserService userService,
      DataService dataService,
      UserPermissionEvaluator userPermissionEvaluator) {
    this.userService = requireNonNull(userService);
    this.dataService = requireNonNull(dataService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  public void checkRoleExists(String role) {
    if (getRole(role.toUpperCase()) == null) {
      throw new UnknownRoleException(role);
    }
  }

  private Role getRole(String rolename) {
    return dataService
        .query(RoleMetadata.ROLE, Role.class)
        .eq(RoleMetadata.NAME, rolename)
        .findOne();
  }

  public void checkUserExists(String user) {
    if (userService.getUser(user) == null) {
      throw new UnknownUserException(user);
    }
  }

  public List<Sid> getSids(List<String> users, List<String> roles) {
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

  public static Optional<String> getUsername(Sid sid) {
    if (sid instanceof PrincipalSid) {
      return Optional.of(((PrincipalSid) sid).getPrincipal());
    }
    return Optional.empty();
  }

  public static Optional<String> getRolename(Sid sid) {
    if (sid instanceof GrantedAuthoritySid) {
      String role = ((GrantedAuthoritySid) sid).getGrantedAuthority();
      return Optional.of(SidUtils.getRoleName(role));
    }
    return Optional.empty();
  }

  public static String getName(Sid sid) {
    Optional<String> name = getRolename(sid);
    if (!name.isPresent()) {
      name = getUsername(sid);
    }
    if (!name.isPresent()) {
      throw new IllegalStateException(
          "Sid should always be either a GrantedAuthoritySid or a PrincipalSid");
    }
    return name.get();
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
    List<Sid> result = new ArrayList<>();
    if (userPermissionEvaluator.hasPermission(
        new EntityTypeIdentity(RoleMetadata.ROLE), EntityTypePermission.READ_DATA)) {
      if (!roleName.equals(ANONYMOUS)) {
        Role role = getRole(roleName);
        if (role == null) {
          throw new UnknownEntityException(RoleMetadata.ROLE, roleName);
        }
        result.addAll(
            StreamSupport.stream(role.getIncludes().spliterator(), false)
                .map(parentRole -> SidUtils.createRoleSid(parentRole.getName()))
                .collect(Collectors.toList()));
      }
    } else {
      throw new InsufficientInheritancePermissionsException();
    }
    return result;
  }

  private List<Sid> getRolesForUser(Sid sid) {
    String username =
        UserRoleTools.getUsername(sid).orElseThrow(() -> new NullPointerException("null username"));
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

  Set<Sid> getRoles(Sid sid) {
    Set<Sid> roles = new LinkedHashSet<>();
    resolveRoles(sid, roles);
    return roles;
  }

  List<Sid> getRoles(Set<Sid> sids) {
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

  LinkedHashSet<Sid> getInheritedSids(Set<Sid> sids) {
    LinkedList<Sid> result = new LinkedList<>();
    result.addAll(sids);
    result.addAll(getRoles(sids));
    return new LinkedHashSet<>(result);
  }

  Set<Sid> getAllAvailableSids() {
    Set<Sid> sids = new HashSet<>();
    if (userPermissionEvaluator.hasPermission(
        new EntityTypeIdentity(UserMetadata.USER), READ_DATA)) {
      sids =
          userService
              .getUsers()
              .stream()
              .map(user -> new PrincipalSid(user.getUsername()))
              .collect(toSet());
    }
    if (userPermissionEvaluator.hasPermission(
        new EntityTypeIdentity(RoleMetadata.ROLE), READ_DATA)) {
      Set<Sid> roles =
          dataService
              .findAll(RoleMetadata.ROLE)
              .map(role -> new GrantedAuthoritySid(ROLE_PREFIX + role.getString(RoleMetadata.NAME)))
              .collect(toSet());

      sids.addAll(roles);
    }
    sids.add(SidUtils.createSecurityContextSid());

    return sids;
  }

  List<Sid> sortSids(Set<Sid> sids) {
    List<Sid> result = new LinkedList<>(sids);
    result.sort(comparing(UserRoleTools::getName));
    return result;
  }

  boolean isSuperUser(Sid sid) {
    String username = getUsername(sid).orElse(null);
    if (username == null) {
      String rolename =
          getRolename(sid)
              .orElseThrow(() -> new IllegalArgumentException("Sid is neither a user nor a role."));
      return AUTHORITY_SU.equals(SidUtils.createRoleAuthority(rolename));
    }
    User user = userService.getUser(username);
    // no UnknownUserException beccause this results in trouble with "WithMockUser" tests
    return user != null && user.isSuperuser();
  }
}
