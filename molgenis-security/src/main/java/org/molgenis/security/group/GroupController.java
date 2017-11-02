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
import java.time.ZoneId;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Api("Group")
@Controller
@RequestMapping("/group")
public class GroupController
{
	private final GroupService groupService;
	private final RoleService roleService;
	private final UserService userService;

	private final String GROUP_NOT_FOUND_MESSAGE = "Group not found";
	private final String ROLE_NOT_FOUND_MESSAGE ="Role not found";

	public GroupController(GroupService groupService, RoleService roleService, UserService userService)
	{
		this.groupService = requireNonNull(groupService);
		this.roleService = requireNonNull(roleService);
		this.userService = requireNonNull(userService);
	}

	/**
	 * Creates a new group with standard subgroups and roles
	 *
	 * @param id the label of the new group
	 */
	@ApiOperation("Create a group with roles")
	@ApiResponses({
			@ApiResponse(code= 200, message = "Group with roles will be returned", response = Group.class),
			@ApiResponse(code = 400, message = "Invalid group-label supplied", response = IllegalStateException.class),
			@ApiResponse(code = 404, message = "Could not add group", response = IllegalStateException.class)
	})
	@PostMapping("/")
	public ResponseEntity<Group> createGroup(@RequestParam("id") String id)
	{
		List<Role> roles = roleService.createRolesForGroup(id);
		Group group = groupService.createGroup(Group.builder().label(id).roles(roles).build());
		String groupId = group.getId().orElseThrow(IllegalStateException::new);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(groupId).toUri();
		return ResponseEntity.created(location).body(group);
	}

	@ApiOperation("Get group members")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Returns a list of GroupMemberships", response = GroupMembership.class),
			@ApiResponse(code = 400, message = "Invalid groupId supplied"),
			@ApiResponse(code = 404, message = "Not allowed to see groupId")
	})
	@GetMapping(value = "/{groupId}/members", produces = "application/json")
	public ResponseEntity<List<GroupMembership>> getGroupMembers(@PathVariable String groupId,
			@RequestParam(required = false) String filter, @RequestParam(required = false) String sort)
	{
		return groupService.findGroupById(groupId)
						   .map(groupService::getGroupMemberships)
						   .map(ResponseEntity.ok()::body)
						   .orElse(ResponseEntity.notFound().build());
	}

	@ApiOperation("Add a role the a group")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Role is added to group"),
			@ApiResponse(code = 400, message = "User or group is not found", response = MolgenisDataException.class)
	})
	@PostMapping("addGroupRole")
	public void addGroupRole(@RequestBody GroupRole groupRole) {
		Group group = groupService.findGroupById(groupRole.getGroupId()).orElseThrow(() -> new MolgenisDataException(GROUP_NOT_FOUND_MESSAGE));
		Role role = roleService.findRoleById(groupRole.getRoleId()).orElseThrow(() -> new MolgenisDataException(ROLE_NOT_FOUND_MESSAGE));
		groupService.addRoleToGroup(group, role);
	}

	@ApiOperation("Remove a role from a group")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Role is removed from group"),
			@ApiResponse(code = 400, message = "User or group is not found", response = MolgenisDataException.class)
	})
	@DeleteMapping("removeGroupRole")
	public void removeGroupRole(@RequestBody GroupRole groupRole) {
		Group group = groupService.findGroupById(groupRole.getGroupId()).orElseThrow(() -> new MolgenisDataException(GROUP_NOT_FOUND_MESSAGE));
		Role role = roleService.findRoleById(groupRole.getRoleId()).orElseThrow(() -> new MolgenisDataException(ROLE_NOT_FOUND_MESSAGE));
		groupService.removeRoleFromGroup(group, role);
	}


	@ApiOperation("Add a user tot group")
	@ApiResponses({
			@ApiResponse(code = 200, message = "User is added in group"),
			@ApiResponse(code = 400, message = "User or group is not found", response = MolgenisDataException.class)
	})
	@PostMapping("updateUserMembership")
	public void updateUserMembership(@RequestBody UpdateUserMembership updateUserMembership) {
		Group group = groupService.findGroupById(updateUserMembership.getGroupId()).orElseThrow(() -> new MolgenisDataException(GROUP_NOT_FOUND_MESSAGE));
		User user = userService.findUserById(updateUserMembership.getUserId()).orElseThrow(() -> new MolgenisDataException(ROLE_NOT_FOUND_MESSAGE));

		Instant startDate = updateUserMembership.getStart().atStartOfDay(ZoneId.systemDefault()).toInstant();
		groupService.addUserToGroup(user, group, startDate);
	}
}
