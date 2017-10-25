package org.molgenis.security.core.service.impl;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

@Component
public class GroupServiceImpl implements GroupService
{
	private final GroupMembershipService groupMembershipService;

	public GroupServiceImpl(GroupMembershipService groupMembershipService)
	{
		this.groupMembershipService = requireNonNull(groupMembershipService);
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
	public Set<Group> getCurrentGroups(User user)
	{
		return getGroupMemberships(user).stream()
										.filter(GroupMembership::isCurrent)
										.map(GroupMembership::getGroup)
										.collect(Collectors.toSet());
	}
}
