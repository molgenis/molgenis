package org.molgenis.security.group;

import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
	public void createGroup(@RequestParam String label)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@GetMapping(value = "/{groupId}/members", produces = "application/json")
	public ResponseEntity<List<GroupMembership>> getGroupMembers(@PathVariable String groupId,
			@RequestParam(required = false) String filter, @RequestParam(required = false) String sort)
	{
		Group group = groupService.findGroupById(groupId).orElseThrow(() -> new IllegalArgumentException(""));
		return ResponseEntity.ok().body(groupService.getGroupMemberships(group));
	}
}
