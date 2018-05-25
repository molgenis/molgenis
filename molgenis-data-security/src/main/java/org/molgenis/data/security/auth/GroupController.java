package org.molgenis.data.security.auth;

import org.springframework.stereotype.Controller;

import static java.util.Objects.requireNonNull;

@Controller
public class GroupController
{
	private final GroupService groupService;

	public GroupController(GroupService groupService)
	{
		this.groupService = requireNonNull(groupService);
	}

}
