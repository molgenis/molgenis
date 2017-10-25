package org.molgenis.data.security.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.model.GroupMembershipEntity;
import org.molgenis.data.support.QueryImpl;
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
import static org.molgenis.data.security.model.GroupMembershipMetadata.GROUP_MEMBERSHIP;
import static org.molgenis.data.security.model.GroupMembershipMetadata.USER;

@Component
public class GroupServiceImpl implements GroupService
{
	private final DataService dataService;

	public GroupServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
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
		Query<GroupMembershipEntity> forUser = new QueryImpl<GroupMembershipEntity>().eq(USER, user.getId());
		return dataService.findAll(GROUP_MEMBERSHIP, forUser, GroupMembershipEntity.class)
						  .map(GroupMembershipEntity::toGroupMembership)
						  .collect(Collectors.toList());
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
