package org.molgenis.api.identities;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.molgenis.api.ApiNamespace;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupPermission;
import org.molgenis.data.security.auth.GroupService;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.exception.GroupNameNotAvailableException;
import org.molgenis.data.security.exception.GroupPermissionDeniedException;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.model.GroupValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Validated
@Api("Group")
public class GroupRestController {
  public static final String USER = "/user";

  @SuppressWarnings("squid:S1075") // URIs should not be hardcoded
  private static final String SECURITY_API_PATH = ApiNamespace.API_PATH + "/identities";

  static final String GROUP_END_POINT = SECURITY_API_PATH + "/group";
  private static final String GROUP_MEMBER_END_POINT = GROUP_END_POINT + "/{groupName}/member";
  private static final String ROLE_EXTEND_END_POINT = GROUP_END_POINT + "/{groupName}/role";
  private static final String GROUP_PERMISSION_END_POINT =
      GROUP_END_POINT + "/{groupName}/permission";
  static final String TEMP_USER_END_POINT = SECURITY_API_PATH + USER;

  private final GroupValueFactory groupValueFactory;
  private final GroupService groupService;
  private final RoleMembershipService roleMembershipService;
  private final RoleService roleService;
  private final UserService userService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  GroupRestController(
      GroupValueFactory groupValueFactory,
      GroupService groupService,
      RoleMembershipService roleMembershipService,
      RoleService roleService,
      UserService userService,
      UserPermissionEvaluator userPermissionEvaluator) {

    this.groupValueFactory = requireNonNull(groupValueFactory);
    this.groupService = requireNonNull(groupService);
    this.roleMembershipService = requireNonNull(roleMembershipService);
    this.roleService = requireNonNull(roleService);
    this.userService = requireNonNull(userService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  @PostMapping(GROUP_END_POINT)
  @ApiOperation(value = "Create a new group", response = ResponseEntity.class)
  @Transactional
  @ApiResponses({
    @ApiResponse(code = 201, message = "New group created", response = ResponseEntity.class),
    @ApiResponse(code = 400, message = "Group name not available", response = ResponseEntity.class)
  })
  public ResponseEntity createGroup(@RequestBody GroupCommand group) {
    GroupValue groupValue =
        groupValueFactory.createGroup(
            group.getName(), group.getLabel(), GroupService.DEFAULT_ROLES);

    if (!groupService.isGroupNameAvailable(groupValue)) {
      throw new GroupNameNotAvailableException(group.getName());
    }

    groupService.persist(groupValue);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{name}")
            .buildAndExpand(groupValue.getName())
            .toUri();

    return ResponseEntity.created(location).build();
  }

  @DeleteMapping(GROUP_END_POINT + "/{groupName}")
  @ApiOperation(value = "Delete a group", response = ResponseEntity.class)
  @Transactional
  @ApiResponses({
    @ApiResponse(code = 204, message = "Group deleted", response = ResponseEntity.class),
  })
  public ResponseEntity deleteGroup(@PathVariable(value = "groupName") String groupName) {
    groupService.deleteGroup(groupName);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(GROUP_END_POINT)
  @ApiOperation(value = "Get list with groups", response = ResponseEntity.class)
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message = "List of groupResponse object available to user",
        response = List.class)
  })
  @ResponseBody
  public List<GroupResponse> getGroups() {
    return groupService.getGroups().stream()
        .filter(
            group ->
                userPermissionEvaluator.hasPermission(
                    new GroupIdentity(group), GroupPermission.VIEW))
        .map(GroupResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @GetMapping(GROUP_MEMBER_END_POINT)
  @ApiOperation(value = "Get group members", response = Collection.class)
  @ResponseBody
  public Collection<GroupMemberResponse> getMembers(
      @PathVariable(value = "groupName") String groupName) {
    checkGroupPermission(groupName, GroupPermission.VIEW_MEMBERSHIP);
    Iterable<Role> roles = groupService.getGroup(groupName).getRoles();
    return roleMembershipService.getMemberships(Lists.newArrayList(roles)).stream()
        .map(GroupMemberResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @PostMapping(GROUP_MEMBER_END_POINT)
  @ApiOperation(value = "Add member to group", response = ResponseEntity.class)
  @Transactional
  @ApiResponses({
    @ApiResponse(code = 201, message = "Member added to group", response = ResponseEntity.class)
  })
  public ResponseEntity addMember(
      @PathVariable(value = "groupName") String groupName,
      @RequestBody AddGroupMemberCommand addMemberCommand) {
    checkGroupPermission(groupName, GroupPermission.ADD_MEMBERSHIP);
    final Group group = groupService.getGroup(groupName);
    final String username = addMemberCommand.getUsername();
    final String roleName = addMemberCommand.getRoleName();
    final Role role = roleService.getRole(roleName);
    final User user = userService.getUser(username);

    groupService.addMember(group, user, role);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{group}/member/{member}")
            .buildAndExpand(groupName, username)
            .toUri();

    return ResponseEntity.created(location).build();
  }

  @DeleteMapping(GROUP_MEMBER_END_POINT + "/{memberName}")
  @ApiOperation(value = "Remove member from group", response = ResponseEntity.class)
  @Transactional
  @ApiResponses({
    @ApiResponse(code = 204, message = "Member removed from group", response = ResponseEntity.class)
  })
  public ResponseEntity removeMember(
      @PathVariable(value = "groupName") String groupName,
      @PathVariable(value = "memberName") String memberName) {
    checkGroupPermission(groupName, GroupPermission.REMOVE_MEMBERSHIP);
    final Group group = groupService.getGroup(groupName);
    final User member = userService.getUser(memberName);

    groupService.removeMember(group, member);

    return ResponseEntity.noContent().build();
  }

  @PutMapping(GROUP_MEMBER_END_POINT + "/{memberName}")
  @ApiOperation(value = "Change membership role", response = ResponseEntity.class)
  @Transactional
  @ResponseStatus(HttpStatus.OK)
  @ApiResponses({
    @ApiResponse(code = 200, message = "Updated membership role", response = ResponseEntity.class)
  })
  public void updateMember(
      @PathVariable(value = "groupName") String groupName,
      @PathVariable(value = "memberName") String memberName,
      @RequestBody UpdateGroupMemberCommand groupMember) {
    checkGroupPermission(groupName, GroupPermission.UPDATE_MEMBERSHIP);
    final Group group = groupService.getGroup(groupName);
    final User member = userService.getUser(memberName);

    final Role newRole = roleService.getRole(groupMember.getRoleName());

    groupService.updateMemberRole(group, member, newRole);
  }

  @PutMapping(ROLE_EXTEND_END_POINT + "/{roleName}")
  @ApiOperation(value = "Change group role extension", response = ResponseEntity.class)
  @Transactional
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses({
    @ApiResponse(code = 204, message = "Updated membership role", response = ResponseEntity.class)
  })
  public void updateExtends(
      @PathVariable(value = "groupName") String groupName,
      @PathVariable(value = "roleName") String roleName,
      @RequestBody UpdateIncludeCommand updateExtendsCommand) {
    checkGroupPermission(groupName, GroupPermission.UPDATE_MEMBERSHIP);
    final Group group = groupService.getGroup(groupName);
    final String groupRoleName = updateExtendsCommand.getRole();
    final Role includingRole = roleService.getRole(roleName);
    final Role groupRole = roleService.getRole(groupRoleName);

    groupService.updateExtendsRole(group, groupRole, includingRole);
  }

  @DeleteMapping(ROLE_EXTEND_END_POINT + "/{roleName}")
  @ApiOperation(
      value = "Remove extension from a grouprole from a role",
      response = ResponseEntity.class)
  @Transactional
  @ApiResponses({
    @ApiResponse(
        code = 204,
        message = "Group role extension removed from role",
        response = ResponseEntity.class)
  })
  public ResponseEntity removeExtends(
      @PathVariable(value = "groupName") String groupName,
      @PathVariable(value = "roleName") String includingRoleName) {
    checkGroupPermission(groupName, GroupPermission.REMOVE_MEMBERSHIP);
    final Group group = groupService.getGroup(groupName);
    final Role includingRole = roleService.getRole(includingRoleName);

    groupService.removeExtendsRole(group, includingRole);

    return ResponseEntity.noContent().build();
  }

  @GetMapping(GROUP_END_POINT + "/{groupName}/role")
  @ApiOperation(value = "Get group roles", response = Collection.class)
  @ResponseBody
  public Collection<RoleResponse> getGroupRoles(
      @PathVariable(value = "groupName") String groupName) {
    checkGroupPermission(groupName, GroupPermission.VIEW);
    Iterable<Role> roles = groupService.getGroup(groupName).getRoles();
    Collection<Role> roleCollection = new ArrayList<>();
    roles.forEach(roleCollection::add);

    return roleCollection.stream().map(RoleResponse::fromEntity).collect(Collectors.toList());
  }

  @GetMapping(TEMP_USER_END_POINT)
  @ApiOperation(value = "Get all users", response = Collection.class)
  @ResponseBody
  @PreAuthorize("hasAnyRole('SU', 'MANAGER')")
  public Collection<UserResponse> getUsers() {
    return userService.getUsers().stream()
        .filter(u -> !u.getUsername().equals("anonymous"))
        .map(UserResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @GetMapping(GROUP_PERMISSION_END_POINT)
  @ApiOperation(value = "Get group permissions", response = Collection.class)
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message = "List of permissions for current user on group",
        response = Collection.class)
  })
  @ResponseBody
  public Collection<Permission> getPermissions(
      @PathVariable(value = "groupName") String groupName) {
    return userPermissionEvaluator.getPermissions(
        new GroupIdentity(groupName), GroupPermission.values());
  }

  private void checkGroupPermission(
      @PathVariable(value = "groupName") String groupName, GroupPermission permission) {
    if (!userPermissionEvaluator.hasPermission(new GroupIdentity(groupName), permission)) {
      throw new GroupPermissionDeniedException(permission, groupName);
    }
  }
}
