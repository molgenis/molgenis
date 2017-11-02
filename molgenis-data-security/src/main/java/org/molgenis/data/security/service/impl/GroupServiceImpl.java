package org.molgenis.data.security.service.impl;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.molgenis.data.DataService;
import org.molgenis.data.security.model.GroupEntity;
import org.molgenis.data.security.model.GroupFactory;
import org.molgenis.data.security.model.GroupMetadata;
import org.molgenis.data.security.model.RoleFactory;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.impl.GroupMembershipService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
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
	@Transactional
	public void addUserToGroup(User user, Group group)
	{
		addUserToGroup(user, group, Instant.now());
	}

	@Override
	@Transactional
	public void addUserToGroup(User user, Group group, Instant start)
	{
		add(GroupMembership.builder().user(user).group(group).start(start).build());
	}

	@Override
	public Optional<Group> findGroupById(String groupId)
	{
		return Optional.ofNullable(dataService.findOneById(GroupMetadata.GROUP, groupId, GroupEntity.class))
					   .map(GroupEntity::toGroup);
	}

	@Override
	@Transactional
	public void addUserToGroup(User user, Group group, Instant start, Instant end)
	{
		add(GroupMembership.builder().user(user).group(group).start(start).end(end).build());
	}

	private void add(GroupMembership membership)
	{
		List<GroupMembership> overlaps = getOverlapsSameUser(membership);
		groupMembershipService.delete(overlaps);
		Map<Boolean, List<GroupMembership>> partitioned = overlaps.stream()
																  .collect(partitioningBy(membership::isSameGroup));
		groupMembershipService.add(processOtherGroupOverlaps(membership, partitioned.get(false)));
		groupMembershipService.add(processSameGroupOverlaps(membership, partitioned.get(true)));
	}

	private List<GroupMembership> getOverlapsSameUser(GroupMembership newMembership)
	{
		return groupMembershipService.getGroupMemberships(newMembership.getUser())
									 .stream()
									 .filter(newMembership::isOverlappingWith)
									 .collect(toList());
	}

	private List<GroupMembership> processSameGroupOverlaps(GroupMembership newMembership,
			List<GroupMembership> sameGroup)
	{
		RangeSet<Instant> validities = TreeRangeSet.create();
		validities.add(newMembership.getValidity());
		sameGroup.stream().map(GroupMembership::getValidity).forEach(validities::add);
		return validities.asRanges()
						 .stream()
						 .map(validity -> GroupMembership.builder()
														 .group(newMembership.getGroup())
														 .user(newMembership.getUser())
														 .validity(validity)
														 .build())
						 .collect(Collectors.toList());
	}

	private List<GroupMembership> processOtherGroupOverlaps(GroupMembership newMembership,
			List<GroupMembership> conflicts)
	{
		return conflicts.stream().flatMap(conflict -> truncate(conflict, newMembership)).collect(toList());
	}

	private Stream<GroupMembership> truncate(GroupMembership groupMembership, GroupMembership newMembership)
	{
		RangeSet<Instant> leftover = TreeRangeSet.create();
		leftover.add(groupMembership.getValidity());
		leftover.remove(newMembership.getValidity());
		return leftover.asRanges()
					   .stream()
					   .map(validity -> GroupMembership.builder()
													   .group(groupMembership.getGroup())
													   .user(groupMembership.getUser())
													   .validity(validity)
													   .build());
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
		dataService.add(GroupMetadata.GROUP, parentEntity);
		group.getRoles()
			 .forEach(role -> addChildGroups(parentEntity,
					 builder().label(role.getLabel()).roles(newArrayList(role)).build()));
		return parentEntity.toGroup().toBuilder().roles(group.getRoles()).build();
	}

	private Group addChildGroups(GroupEntity parent, Group childGroup)
	{
		GroupEntity childGroupEntity = groupFactory.create().updateFrom(childGroup, groupFactory, roleFactory);
		childGroupEntity.setParent(parent);
		dataService.add(GroupMetadata.GROUP, childGroupEntity);
		return childGroupEntity.toGroup();
	}

	@Override
	public void removeRoleFromGroup(Group group, Role role)
	{
		Group updatedGroup = group.toBuilder()
								  .roles(group.getRoles()
											  .stream()
											  .filter(sourceRole -> !sourceRole.getId().equals(role.getId()))
											  .collect(toList()))
								  .build();
		GroupEntity groupEntity = dataService.findOneById(GroupMetadata.ID, group.getId(), GroupEntity.class);
		groupEntity.updateFrom(updatedGroup, groupFactory, roleFactory);
		dataService.update(GroupMetadata.GROUP, groupEntity);
	}

	@Override
	public void addRoleToGroup(Group group, Role role)
	{
		Group updatedGroup = group.toBuilder().addRole(role).build();
		GroupEntity groupEntity = dataService.findOneById(GroupMetadata.ID, group.getId(), GroupEntity.class);
		groupEntity.updateFrom(updatedGroup, groupFactory, roleFactory);
		dataService.update(GroupMetadata.GROUP, groupEntity);
	}

}
