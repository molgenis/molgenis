package org.molgenis.security.group;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.RoleService;
import org.molgenis.security.core.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.OK;

@Api("Group")
@Controller
@RequestMapping("/group")
public class GroupController
{
	private final GroupService groupService;
	private final RoleService roleService;
	private final UserService userService;

	private static final String GROUP_NOT_FOUND_MESSAGE = "Group not found";
	private static final String ROLE_NOT_FOUND_MESSAGE = "Role not found";

	public GroupController(GroupService groupService, RoleService roleService, UserService userService)
	{
		this.groupService = requireNonNull(groupService);
		this.roleService = requireNonNull(roleService);
		this.userService = requireNonNull(userService);
	}

	/**
	 * Creates a new group with standard subgroups and roles
	 *
	 * @param label the label of the new group
	 */
	@ApiOperation("Create a group with roles")
	@ApiResponses({ @ApiResponse(code = 200, message = "Group with roles will be returned", response = Group.class),
			@ApiResponse(code = 400, message = "Invalid group-label supplied", response = IllegalStateException.class),
			@ApiResponse(code = 404, message = "Could not add group", response = IllegalStateException.class) })
	@PostMapping("/")
	public ResponseEntity<Group> createGroup(@RequestParam("label") String label)
	{
		List<Role> roles = roleService.createRolesForGroup(label);
		Group group = groupService.createGroup(Group.builder().label(label).roles(roles).build());
		String groupId = group.getId().orElseThrow(IllegalStateException::new);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(groupId).toUri();
		return ResponseEntity.created(location).body(group);
	}

	@ApiOperation("Get group members")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Returns a list of GroupMemberships", response = GroupMembership.class),
			@ApiResponse(code = 400, message = "Invalid groupId supplied"),
			@ApiResponse(code = 404, message = "Not allowed to see groupId") })
	@GetMapping(value = "/{groupId}/members", produces = "application/json")
	public ResponseEntity<List<GroupMembership>> getGroupMembers(@PathVariable("groupId") String groupId)
	{
		return groupService.findGroupById(groupId)
						   .map(groupService::getGroupMemberships)
						   .map(ResponseEntity.ok()::body)
						   .orElse(ResponseEntity.notFound().build());
	}

	@ApiOperation("Add a role to a group")
	@ApiResponses({ @ApiResponse(code = 200, message = "Role is added to group"),
			@ApiResponse(code = 400, message = "User or group is not found", response = MolgenisDataException.class) })
	@PostMapping("addGroupRole")
	@ResponseStatus(OK)
	public void addGroupRole(@RequestBody GroupRole groupRole)
	{
		Group group = groupService.findGroupById(groupRole.getGroupId())
								  .orElseThrow(() -> new MolgenisDataException(GROUP_NOT_FOUND_MESSAGE));
		Role role = roleService.findRoleById(groupRole.getRoleId())
							   .orElseThrow(() -> new MolgenisDataException(ROLE_NOT_FOUND_MESSAGE));
		groupService.addRoleToGroup(group, role);
	}

	@ApiOperation("Remove a role from a group")
	@ApiResponses({ @ApiResponse(code = 200, message = "Role is removed from group"),
			@ApiResponse(code = 400, message = "User or group is not found", response = MolgenisDataException.class) })
	@PostMapping("removeGroupRole")
	@ResponseStatus(OK)
	public void removeGroupRole(@RequestBody GroupRole groupRole)
	{
		Group group = groupService.findGroupById(groupRole.getGroupId())
								  .orElseThrow(() -> new MolgenisDataException(GROUP_NOT_FOUND_MESSAGE));
		Role role = roleService.findRoleById(groupRole.getRoleId())
							   .orElseThrow(() -> new MolgenisDataException(ROLE_NOT_FOUND_MESSAGE));
		groupService.removeRoleFromGroup(group, role);
	}

	@ApiOperation("Add a user to group")
	@ApiResponses({ @ApiResponse(code = 200, message = "User is added in group"),
			@ApiResponse(code = 400, message = "User or group is not found", response = MolgenisDataException.class) })
	@PostMapping("updateUserMembership")
	@ResponseStatus(OK)
	public void updateUserMembership(@RequestBody UpdateUserMembership updateUserMembership)
	{
		Group group = groupService.findGroupById(updateUserMembership.getGroupId())
								  .orElseThrow(() -> new MolgenisDataException(GROUP_NOT_FOUND_MESSAGE));
		User user = userService.findUserById(updateUserMembership.getUserId())
							   .orElseThrow(() -> new MolgenisDataException(ROLE_NOT_FOUND_MESSAGE));

		groupService.removeUserFromGroup(user, group);

		Instant startDate = Optional.of(updateUserMembership.getStart())
									.map(start -> start.atStartOfDay(systemDefault()))
									.map(ZonedDateTime::toInstant)
									.filter(now()::isBefore)
									.orElse(now());
		GroupMembership.Builder newMembership = GroupMembership.builder().user(user).group(group).start(startDate);
		updateUserMembership.getStop()
							.map(stop -> stop.atStartOfDay(systemDefault()))
							.map(ZonedDateTime::toInstant)
							.ifPresent(newMembership::end);
		groupService.addGroupMembership(newMembership.build());
	}

	@ApiOperation("Remove a user from a group")
	@ApiResponses({ @ApiResponse(code = 200, message = "User's group membership was terminated effective immediately"),
			@ApiResponse(code = 400, message = "User or group is not found", response = MolgenisDataException.class) })
	@PostMapping("removeUserFromGroup")
	@ResponseStatus(OK)
	public void removeUserFromGroup(@RequestBody RemoveUserMembership removeUserMembership)
	{
		Group group = groupService.findGroupById(removeUserMembership.getGroupId())
								  .orElseThrow(() -> new MolgenisDataException(GROUP_NOT_FOUND_MESSAGE));
		User user = userService.findUserById(removeUserMembership.getUserId())
							   .orElseThrow(() -> new MolgenisDataException(ROLE_NOT_FOUND_MESSAGE));

		groupService.removeUserFromGroup(user, group);
	}
}
