package org.molgenis.security.core.service.impl;

import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class GroupServiceImpl implements GroupService
{
	private final GroupMembershipService groupMembershipService;

	public GroupServiceImpl(GroupMembershipService groupMembershipService)
	{
		this.groupMembershipService = requireNonNull(groupMembershipService);
	}

	@Override
	public void addUserToGroup(String userId, String groupId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUserToGroup(String userId, String groupId, Instant start)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUserToGroup(String userId, String groupId, Instant start, Instant end)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeUserFromGroup(String userId, String groupId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<GroupMembership> getGroupMemberships(User user)
	{
		return groupMembershipService.getGroupMemberships(user);
	}

	@Override
	public Set<Group> getCurrentGroups(User user)
	{
		return getGroupMemberships(user).stream()
										.filter(GroupMembership::isCurrent)
										.map(GroupMembership::getGroup)
										.collect(Collectors.toSet());
	}
}
