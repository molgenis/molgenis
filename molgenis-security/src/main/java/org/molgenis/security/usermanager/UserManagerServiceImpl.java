package org.molgenis.security.usermanager;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisGroupMember;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Manage user in groups
 */
@Service
public class UserManagerServiceImpl implements UserManagerService
{
	private final DataService dataService;

	@Autowired
	public UserManagerServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("Database is null");
		this.dataService = dataService;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisUserViewData> getAllMolgenisUsers()
	{
		List<MolgenisUser> users = dataService.findAllAsList(MolgenisUser.ENTITY_NAME, new QueryImpl());
		return this.parseToMolgenisUserViewData(users);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getAllMolgenisGroups()
	{
		return dataService.findAllAsList(MolgenisGroup.ENTITY_NAME, new QueryImpl());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getGroupsWhereUserIsMember(Integer userId)
	{
		return this.getMolgenisGroups(userId);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisUserViewData> getUsersMemberInGroup(Integer groupId)
	{
		return this.parseToMolgenisUserViewData(this.getMolgenisUsers(groupId));
	}

	private List<MolgenisGroup> getMolgenisGroups(Integer userId)
	{
		final MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME, userId);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = dataService.findAllAsList(MolgenisGroupMember.ENTITY_NAME,
				new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, molgenisUser));

		return this.getAllMolgenisGroupsFromGroupMembers(groupMembers);
	}

	private List<MolgenisUser> getMolgenisUsers(final Integer groupId)
	{
		final MolgenisGroup molgenisGroup = dataService.findOne(MolgenisGroup.ENTITY_NAME, groupId);

		if (molgenisGroup == null)
		{
			throw new RuntimeException("unknown user id [" + groupId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = dataService.findAllAsList(MolgenisGroupMember.ENTITY_NAME,
				new QueryImpl().eq(MolgenisGroupMember.MOLGENISGROUP, molgenisGroup));

		return this.getAllMolgenisUsersFromGroupMembers(groupMembers);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getGroupsWhereUserIsNotMember(final Integer userId)
	{
		final MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME, userId);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = dataService.findAllAsList(MolgenisGroupMember.ENTITY_NAME,
				new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, molgenisUser));

		final List<MolgenisGroup> groupsWhereUserIsMember = this.getAllMolgenisGroupsFromGroupMembers(groupMembers);

		Predicate<MolgenisGroup> predicate = new PredicateNotInMolgenisGroupList(groupsWhereUserIsMember);
		List<MolgenisGroup> molgenisGroups = this.getAllMolgenisGroups();

		return Lists.<MolgenisGroup> newArrayList(Iterables.filter(molgenisGroups, predicate));
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public void addUserToGroup(Integer molgenisGrougId, Integer molgenisUserId)
	{
		MolgenisGroupMember molgenisGroupMember = new MolgenisGroupMember();
		molgenisGroupMember.setMolgenisGroup(molgenisGrougId);
		molgenisGroupMember.setMolgenisUser(molgenisUserId);
		dataService.add(MolgenisGroupMember.ENTITY_NAME, molgenisGroupMember);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public void removeUserFromGroup(Integer molgenisGroupId, Integer molgenisUserId)
	{
		final MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME, molgenisUserId);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisUserId + "]");
		}

		final MolgenisGroup molgenisGroup = dataService.findOne(MolgenisGroup.ENTITY_NAME, molgenisGroupId);

		if (molgenisGroup == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisGroupId + "]");
		}

		final List<MolgenisGroupMember> molgenisGroupMembers = dataService
				.findAllAsList(MolgenisGroupMember.ENTITY_NAME, new QueryRule(MolgenisGroupMember.MOLGENISUSER,
						Operator.EQUALS, molgenisUser), new QueryRule(MolgenisGroupMember.MOLGENISGROUP,
						Operator.EQUALS, molgenisGroup));

		if (null == molgenisGroupMembers || molgenisGroupMembers.isEmpty())
		{
			throw new RuntimeException("molgenis group member is not found");
		}

		if (molgenisGroupMembers.size() > 1)
		{
			throw new RuntimeException("there are more than one group member found");
		}

		MolgenisGroupMember molgenisGroupMember = molgenisGroupMembers.get(0);
		dataService.delete(MolgenisGroupMember.ENTITY_NAME, molgenisGroupMember);
	}

	/**
	 * Get All the molgenis groups from the list of molgenis group members
	 * 
	 * @param groupMembers
	 *            A list of MolgenisGroupMember instances
	 * @return List<MolgenisGroup>
	 */
	private List<MolgenisGroup> getAllMolgenisGroupsFromGroupMembers(final List<MolgenisGroupMember> groupMembers)
	{
		List<MolgenisGroup> molgenisGroups = new ArrayList<MolgenisGroup>();

		if (groupMembers != null && !groupMembers.isEmpty())
		{
			molgenisGroups = Lists.transform(groupMembers, new Function<MolgenisGroupMember, MolgenisGroup>()
			{
				@Override
				public MolgenisGroup apply(MolgenisGroupMember molgenisGroupMember)
				{
					return molgenisGroupMember.getMolgenisGroup();
				}
			});
		}

		return molgenisGroups;
	}

	/**
	 * Get All the molgenis users from the list of molgenis group members
	 * 
	 * @param groupMembers
	 *            A list of MolgenisGroupMember instances
	 * @return List<MolgenisUser>
	 */
	private List<MolgenisUser> getAllMolgenisUsersFromGroupMembers(final List<MolgenisGroupMember> groupMembers)
	{
		List<MolgenisUser> molgenisUser = new ArrayList<MolgenisUser>();

		if (groupMembers != null && !groupMembers.isEmpty())
		{
			molgenisUser = Lists.transform(groupMembers, new Function<MolgenisGroupMember, MolgenisUser>()
			{
				@Override
				public MolgenisUser apply(MolgenisGroupMember molgenisGroupMember)
				{
					return molgenisGroupMember.getMolgenisUser();
				}
			});
		}

		return molgenisUser;
	}

	private static class PredicateNotInMolgenisGroupList implements Predicate<MolgenisGroup>
	{
		final List<MolgenisGroup> toFilterItemList;

		PredicateNotInMolgenisGroupList(List<MolgenisGroup> notInList)
		{
			this.toFilterItemList = notInList;
		}

		@Override
		public boolean apply(MolgenisGroup item)
		{
			Integer id = item.getId();
			for (MolgenisGroup toFilterItem : toFilterItemList)
			{
				if (toFilterItem.getId().equals(id)) return false;
			}
			return true;
		}

	}

	private List<MolgenisUserViewData> parseToMolgenisUserViewData(List<MolgenisUser> users)
	{
		List<MolgenisUserViewData> results = new ArrayList<MolgenisUserViewData>();
		for (MolgenisUser user : users)
		{
			results.add(new MolgenisUserViewData(user.getId(), user.getUsername()));
		}
		return results;
	}
}
