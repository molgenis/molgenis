package org.molgenis.security.core.service.impl;

import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class GroupServiceImpl implements GroupService
{
	@Override
	public void addUserToGroup(User user, Group group)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUserToGroup(User user, Group group, LocalDate start)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUserToGroup(User user, Group group, LocalDate start, LocalDate end)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeUserFromGroup(User user, Group group)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<GroupMembership> getGroupMemberships(User user)
	{
		return Collections.emptyList();
	}

	@Override
	public Set<Group> getCurrentGroups(User user)
	{
		return Collections.emptySet();
	}
}
