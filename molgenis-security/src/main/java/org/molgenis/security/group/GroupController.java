package org.molgenis.security.group;

import org.molgenis.security.core.service.GroupService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
	@PostMapping("/group/{label}")
	public void createGroup(@PathVariable(name = "label") String label)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
