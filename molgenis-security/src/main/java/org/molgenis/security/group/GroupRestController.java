package org.molgenis.security.group;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.molgenis.data.DataService;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.exception.GroupPermissionDeniedException;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.RoleValue;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.GroupPermission.*;
import static org.molgenis.data.security.auth.GroupService.DEFAULT_ROLES;
import static org.molgenis.data.security.auth.GroupService.MANAGER;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@RestController
@Validated
@Api("Group")
public class GroupRestController
{
	public static final String SECURITY_API_PATH = "/api/plugin/security";
	public static final String USER = "/user";

	public static final String GROUP_END_POINT = SECURITY_API_PATH + "/group";
	public static final String GROUP_MEMBER_END_POINT = GROUP_END_POINT + "/{groupName}/member";
	public static final String GROUP_PERMISSION_END_POINT = GROUP_END_POINT + "/{groupName}/permission";
	public static final String TEMP_USER_END_POINT = SECURITY_API_PATH + USER;

	private final GroupValueFactory groupValueFactory;
	private final GroupService groupService;
	private final RoleMembershipService roleMembershipService;
	private final DataService dataService;
	private final RoleService roleService;
	private final UserService userService;
	private final UserPermissionEvaluator userPermissionEvaluator;

	GroupRestController(GroupValueFactory groupValueFactory, GroupService groupService,
			RoleMembershipService roleMembershipService, DataService dataService, RoleService roleService,
			UserService userService, UserPermissionEvaluator userPermissionEvaluator)
	{
		this.groupValueFactory = requireNonNull(groupValueFactory);
		this.groupService = requireNonNull(groupService);
		this.roleMembershipService = requireNonNull(roleMembershipService);
		this.dataService = requireNonNull(dataService);
		this.roleService = requireNonNull(roleService);
		this.userService = requireNonNull(userService);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
	}

	@PostMapping(GROUP_END_POINT)
	@ApiOperation(value = "Create a new group", response = ResponseEntity.class)
	@Transactional
	@ApiResponses({ @ApiResponse(code = 201, message = "New group created", response = ResponseEntity.class) })
	public ResponseEntity createGroup(@RequestBody GroupCommand group)
	{
		GroupValue groupValue = groupValueFactory.createGroup(group.getName(), group.getLabel(), DEFAULT_ROLES.keySet());

		groupService.persist(groupValue);
		groupService.grantPermissions(groupValue);
		roleMembershipService.addUserToRole(getCurrentUsername(), getManagerRoleName(groupValue));

		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest().path("/{name}")
				.buildAndExpand(groupValue.getName()).toUri();

		return ResponseEntity.created(location).build();
	}

	@GetMapping(GROUP_END_POINT)
	@ApiOperation(value = "Get list with groups", response = ResponseEntity.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "List of groupResponse object available to user", response = List.class) })
	@ResponseBody
	public List<GroupResponse> getGroups()
	{
		return groupService.getGroups()
						   .stream()
						   .filter(group -> userPermissionEvaluator.hasPermission(new GroupIdentity(group), VIEW))
						   .map(GroupResponse::fromEntity)
						   .collect(Collectors.toList());
	}

	@GetMapping(GROUP_MEMBER_END_POINT)
	@ApiOperation(value = "Get group members", response = Collection.class)
	@ResponseBody
	public Collection<GroupMemberResponse> getMembers(@PathVariable(value = "groupName") String groupName)
	{
		checkGroupPermission(groupName, VIEW_MEMBERSHIP);
		Iterable<Role> roles = groupService.getGroup(groupName).getRoles();
		return roleMembershipService.getMemberships(Lists.newArrayList(roles))
							 .stream()
							 .map(GroupMemberResponse::fromEntity)
							 .collect(Collectors.toList());

	}

	@PostMapping(GROUP_MEMBER_END_POINT)
	@ApiOperation(value = "Add member to group", response = ResponseEntity.class)
	@Transactional
	@ApiResponses({ @ApiResponse(code = 201, message = "Member added to group", response = ResponseEntity.class) })
	public ResponseEntity addMember(@PathVariable(value = "groupName") String groupName,
			@RequestBody AddGroupMemberCommand addMemberCommand)
	{
		checkGroupPermission(groupName, ADD_MEMBERSHIP);
		final Group group = groupService.getGroup(groupName);
		final String username = addMemberCommand.getUsername();
		final String roleName = addMemberCommand.getRoleName();
		final Role role = roleService.getRole(roleName);
		final User user = userService.getUser(username);

		groupService.addMember(group, user, role);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
												  .path("/{group}/member/{member}")
												  .buildAndExpand(groupName, username)
												  .toUri();

		return ResponseEntity.created(location).build();
	}

	@DeleteMapping(GROUP_MEMBER_END_POINT + "/{memberName}")
	@ApiOperation(value = "Remove member from group", response = ResponseEntity.class)
	@Transactional
	@ApiResponses({ @ApiResponse(code = 204, message = "Member removed from group", response = ResponseEntity.class) })
	public ResponseEntity removeMember(@PathVariable(value = "groupName") String groupName, @PathVariable(value = "memberName") String memberName)
	{
		checkGroupPermission(groupName, REMOVE_MEMBERSHIP);
		final Group group = groupService.getGroup(groupName);
		final User member = userService.getUser(memberName);

		groupService.removeMember(group, member);

		return ResponseEntity.noContent().build();
	}

	@PutMapping(GROUP_MEMBER_END_POINT + "/{memberName}")
	@ApiOperation(value = "Change membership role", response = ResponseEntity.class)
	@Transactional
	@ApiResponses({ @ApiResponse(code = 201, message = "Updated membership role", response = ResponseEntity.class) })
	public ResponseEntity updateMember(@PathVariable(value = "groupName") String groupName,  @PathVariable(value = "memberName") String memberName,
			@RequestBody UpdateGroupMemberCommand groupMember)
	{
		checkGroupPermission(groupName, UPDATE_MEMBERSHIP);
		final Group group = groupService.getGroup(groupName);
		final User member = userService.getUser(memberName);

		final Role newRole = roleService.getRole(groupMember.getRoleName());

		groupService.updateMemberRole(group, member, newRole);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
												  .path("/{group}/member/{member}")
												  .buildAndExpand(groupName, member)
												  .toUri();

		return ResponseEntity.created(location).build();
	}

	@GetMapping(GROUP_END_POINT + "/{groupName}/role")
	@ApiOperation(value = "Get group roles", response = Collection.class)
	@ResponseBody
	public Collection<RoleResponse> getGroupRoles(@PathVariable(value = "groupName") String groupName)
	{
		checkGroupPermission(groupName, VIEW);
		Iterable<Role> roles = groupService.getGroup(groupName).getRoles();
		Collection<Role> roleCollection = new ArrayList<>();
		roles.forEach(roleCollection::add);

		return roleCollection.stream().map(RoleResponse::fromEntity).collect(Collectors.toList());
	}

	@GetMapping(TEMP_USER_END_POINT)
	@ApiOperation(value = "Get all users", response = Collection.class)
	@ResponseBody
	public Collection<UserResponse> getUsers()
	{
		return dataService.findAll(UserMetaData.USER, User.class)
				.map(UserResponse::fromEntity).
				collect(Collectors.toList());
	}

	@GetMapping(GROUP_PERMISSION_END_POINT)
	@ApiOperation(value = "Get group permissions", response = Collection.class)
	@ResponseBody
	public Collection<Permission> getPermissions(@PathVariable(value = "groupName") String groupName)
	{
		return userPermissionEvaluator.getPermissions(new GroupIdentity(groupName), GroupPermission.values());
	}

	private String getManagerRoleName(GroupValue groupValue)
	{
		return groupValue.getRoles()
						 .stream()
						 .filter(role -> role.getLabel().equals(MANAGER))
						 .map(RoleValue::getName)
						 .findFirst()
						 .orElseThrow(() -> new IllegalStateException("Manager role is missing"));
	}

	private void checkGroupPermission(@PathVariable(value = "groupName") String groupName, GroupPermission permission)
	{
		if (!userPermissionEvaluator.hasPermission(new GroupIdentity(groupName), permission))
		{
			throw new GroupPermissionDeniedException(permission, groupName);
		}
	}

}
