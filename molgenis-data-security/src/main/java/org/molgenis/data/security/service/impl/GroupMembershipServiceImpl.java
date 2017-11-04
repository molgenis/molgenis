package org.molgenis.data.security.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.model.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupMembershipService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.model.GroupMembershipMetadata.*;
import static org.molgenis.data.security.model.GroupMetadata.PARENT;

@Component
public class GroupMembershipServiceImpl implements GroupMembershipService
{
	private final DataService dataService;
	private final GroupMembershipFactory membershipFactory;
	private final UserFactory userFactory;
	private final GroupFactory groupFactory;

	public GroupMembershipServiceImpl(DataService dataService, GroupMembershipFactory membershipFactory,
			UserFactory userFactory, GroupFactory groupFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.membershipFactory = requireNonNull(membershipFactory);
		this.userFactory = requireNonNull(userFactory);
		this.groupFactory = requireNonNull(groupFactory);
	}

	@Override
	public void add(List<GroupMembership> memberships)
	{
		dataService.add(GROUP_MEMBERSHIP, memberships.stream()
													 .map(membership -> membershipFactory.create()
																						 .updateFrom(membership,
																								 userFactory,
																								 groupFactory)));
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
		Query<GroupMembershipEntity> forUser = new QueryImpl<GroupMembershipEntity>().eq(USER,
				user.getId().orElseThrow(() -> new IllegalStateException("User has empty ID")));
		forUser.sort().on(START);
		return dataService.findAll(GROUP_MEMBERSHIP, forUser, GroupMembershipEntity.class)
						  .map(GroupMembershipEntity::toGroupMembership)
						  .collect(toList());
	}

	private Stream<Group> getChildGroups(Group parent)
	{
		Query<GroupEntity> forGroup = new QueryImpl<GroupEntity>().eq(PARENT,
				parent.getId().orElseThrow(() -> new IllegalStateException("Parent group has empty ID")));
		return dataService.findAll(GroupMetadata.GROUP, forGroup, GroupEntity.class).map(GroupEntity::toGroup);
	}

	@Override
	public List<GroupMembership> getGroupMemberships(Group parent)
	{
		List<String> groupIds;
		groupIds = Stream.concat(Stream.of(parent), getChildGroups(parent))
						 .map(Group::getId)
						 .filter(Optional::isPresent)
						 .map(Optional::get)
						 .collect(Collectors.toList());
		Query<GroupMembershipEntity> forGroups = new QueryImpl<GroupMembershipEntity>().in(GROUP, groupIds);
		forGroups.sort().on(START);
		return dataService.findAll(GROUP_MEMBERSHIP, forGroups, GroupMembershipEntity.class)
						  .map(GroupMembershipEntity::toGroupMembership)
						  .collect(toList());
	}
}
