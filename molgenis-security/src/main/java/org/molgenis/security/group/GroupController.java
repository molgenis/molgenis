package org.molgenis.security.group;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Api("Group")
@Controller
@RequestMapping("/group")
public class GroupController
{
	private final GroupService groupService;

	public GroupController(GroupService groupService)
	{
		this.groupService = groupService;
	}

	/**
	 * Creates a new group with standard subgroups and roles
	 *
	 * @param id the label of the new group
	 */
	@ApiOperation("Create a group")
	@ApiResponses({
			@ApiResponse(code= 200, message = "Group will be returned", response = Group.class),
			@ApiResponse(code = 400, message = "Invalid label supplied"),
			@ApiResponse(code = 404, message = "Could not add label")
	})
	@PostMapping("/")
	public ResponseEntity<Group> createGroup(@RequestParam String id)
	{
		Group group = groupService.createGroups(id);
		String groupId = group.getId().orElseThrow(IllegalStateException::new);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(groupId).toUri();
		return ResponseEntity.created(location).body(group);
	}

	@ApiOperation("Get group members")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Returns a list of Groupmemberships", response = GroupMembership.class),
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
}
