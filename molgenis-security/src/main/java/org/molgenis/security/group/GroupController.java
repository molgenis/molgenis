package org.molgenis.security.group;

import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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
	 * @param label the label of the new group
	 */
	@PostMapping("/")
	public ResponseEntity<String> createGroup(@RequestParam String label)
	{
		String id = groupService.createGroups(label).getId().orElseThrow(IllegalStateException::new);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

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
