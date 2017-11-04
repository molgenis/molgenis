package org.molgenis.data.security.service.impl;

import com.google.common.collect.Range;
import org.molgenis.data.DataService;
import org.molgenis.data.security.model.GroupEntity;
import org.molgenis.data.security.model.GroupFactory;
import org.molgenis.data.security.model.RoleFactory;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupMembershipService;
import org.molgenis.security.core.service.GroupService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.model.GroupMetadata.GROUP;
import static org.molgenis.security.core.model.Group.builder;

@Component
public class GroupServiceImpl implements GroupService
{
	private final GroupMembershipService groupMembershipService;
	private final DataService dataService;
	private final GroupFactory groupFactory;
	private final RoleFactory roleFactory;

	public GroupServiceImpl(GroupMembershipService groupMembershipService, DataService dataService,
			GroupFactory groupFactory, RoleFactory roleFactory)
	{
		this.groupMembershipService = requireNonNull(groupMembershipService);
		this.dataService = requireNonNull(dataService);
		this.groupFactory = requireNonNull(groupFactory);
		this.roleFactory = requireNonNull(roleFactory);
	}

	@Override
	public Optional<Group> findGroupById(String groupId)
	{
		return Optional.ofNullable(dataService.findOneById(GROUP, groupId, GroupEntity.class))
					   .map(GroupEntity::toGroup);
	}

	@Override
	public void addGroupMembership(GroupMembership membership)
	{
		groupMembershipService.add(Collections.singletonList(membership));
	}

	private List<GroupMembership> getOverlapsSameUser(GroupMembership newMembership)
	{
		return groupMembershipService.getGroupMemberships(newMembership.getUser())
									 .stream()
									 .filter(newMembership::isOverlappingWith)
									 .collect(toList());
	}

	@Override
	@Transactional
	public void removeUserFromGroup(User user, Group group)
	{
		Instant end = now();
		List<GroupMembership> overlaps = getOverlapsSameUser(
				GroupMembership.builder().user(user).group(group).validity(Range.atLeast(end)).build());
		groupMembershipService.delete(overlaps);
		Optional<GroupMembership> currentMembership = overlaps.stream().filter(GroupMembership::isCurrent).findFirst();
		currentMembership.map(GroupMembership::toBuilder)
						 .map(builder -> builder.end(end))
						 .map(GroupMembership.Builder::build)
						 .map(Collections::singletonList)
						 .ifPresent(groupMembershipService::add);
	}

	@Override
	public List<GroupMembership> getGroupMemberships(User user)
	{
		return groupMembershipService.getGroupMemberships(user);
	}

	@Override
	public List<GroupMembership> getGroupMemberships(Group group)
	{
		return groupMembershipService.getGroupMemberships(group);
	}

	@Override
	public Set<Group> getCurrentGroups(User user)
	{
		return getGroupMemberships(user).stream()
										.filter(GroupMembership::isCurrent)
										.map(GroupMembership::getGroup)
										.collect(Collectors.toSet());
	}

	@Override
	public Group createGroup(Group group)
	{
		GroupEntity parentEntity = groupFactory.create().updateFrom(group, groupFactory, roleFactory);
		dataService.add(GROUP, parentEntity);
		group.getRoles()
			 .forEach(role -> addChildGroups(parentEntity,
					 builder().label(role.getLabel()).roles(newArrayList(role)).build()));
		return parentEntity.toGroup().toBuilder().roles(group.getRoles()).build();
	}

	private Group addChildGroups(GroupEntity parent, Group childGroup)
	{
		GroupEntity childGroupEntity = groupFactory.create().updateFrom(childGroup, groupFactory, roleFactory);
		childGroupEntity.setParent(parent);
		dataService.add(GROUP, childGroupEntity);
		return childGroupEntity.toGroup();
	}

	@Override
	public void removeRoleFromGroup(Group group, Role role)
	{
		updateGroup(group.toBuilder()
						 .roles(group.getRoles()
									 .stream()
									 .filter(sourceRole -> !sourceRole.getId().equals(role.getId()))
									 .collect(toList()))
						 .build());
	}

	@Override
	public void addRoleToGroup(Group group, Role role)
	{
		updateGroup(group.toBuilder().addRole(role).build());
	}

	private void updateGroup(Group group)
	{
		dataService.update(GROUP, groupFactory.create().updateFrom(group, groupFactory, roleFactory));
	}

}
