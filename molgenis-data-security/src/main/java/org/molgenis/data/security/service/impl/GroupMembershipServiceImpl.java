package org.molgenis.data.security.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.model.GroupMembershipEntity;
import org.molgenis.data.security.model.GroupMembershipFactory;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.impl.GroupMembershipService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.model.GroupMembershipMetadata.*;

@Component
public class GroupMembershipServiceImpl implements GroupMembershipService
{
	private final DataService dataService;
	private final GroupMembershipFactory groupMembershipFactory;

	public GroupMembershipServiceImpl(DataService dataService, GroupMembershipFactory groupMembershipFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.groupMembershipFactory = requireNonNull(groupMembershipFactory);
	}

	@Override
	public void add(List<GroupMembership> groupMemberships)
	{
		dataService.add(GROUP_MEMBERSHIP,
				groupMemberships.stream().map(membership -> groupMembershipFactory.create().updateFrom(membership)));
	}

	@Override
	public void delete(List<GroupMembership> groupMemberships)
	{
		dataService.deleteAll(GROUP_MEMBERSHIP, groupMemberships.stream()
																.map(GroupMembership::getId)
																.flatMap(idOption -> idOption.map(Stream::of)
																							 .orElse(Stream.empty())));
	}

	@Override
	public List<GroupMembership> getGroupMemberships(User user)
	{
		Query<GroupMembershipEntity> forUser = new QueryImpl<GroupMembershipEntity>().eq(USER, user.getId());
		forUser.sort().on(START);
		return dataService.findAll(GROUP_MEMBERSHIP, forUser, GroupMembershipEntity.class)
						  .map(GroupMembershipEntity::toGroupMembership)
						  .collect(Collectors.toList());
	}

	@Override
	public List<GroupMembership> getGroupMemberships(Group group)
	{
		Query<GroupMembershipEntity> forGroup = new QueryImpl<GroupMembershipEntity>().eq(GROUP, group.getId());
		forGroup.sort().on(START);
		return dataService.findAll(GROUP_MEMBERSHIP, forGroup, GroupMembershipEntity.class)
						  .map(GroupMembershipEntity::toGroupMembership)
						  .collect(Collectors.toList());
	}
}
