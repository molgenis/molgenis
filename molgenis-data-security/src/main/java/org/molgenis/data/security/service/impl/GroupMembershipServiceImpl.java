package org.molgenis.data.security.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.model.GroupMembershipEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.impl.GroupMembershipService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.model.GroupMembershipMetadata.GROUP_MEMBERSHIP;
import static org.molgenis.data.security.model.GroupMembershipMetadata.USER;

@Component
public class GroupMembershipServiceImpl implements GroupMembershipService
{
	private final DataService dataService;

	public GroupMembershipServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public List<GroupMembership> getGroupMemberships(User user)
	{
		Query<GroupMembershipEntity> forUser = new QueryImpl<GroupMembershipEntity>().eq(USER, user.getId());
		return dataService.findAll(GROUP_MEMBERSHIP, forUser, GroupMembershipEntity.class)
						  .map(GroupMembershipEntity::toGroupMembership)
						  .collect(Collectors.toList());
	}
}
