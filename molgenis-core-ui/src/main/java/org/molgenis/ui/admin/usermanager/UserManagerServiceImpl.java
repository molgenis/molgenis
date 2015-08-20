package org.molgenis.ui.admin.usermanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
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
		Iterable<MolgenisUser> users = dataService.findAll(MolgenisUser.ENTITY_NAME, MolgenisUser.class);
		if (users == null) users = Collections.emptyList();

		return this.parseToMolgenisUserViewData(users);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void setActivationUser(String userId, Boolean active)
	{
		MolgenisUser mu = this.dataService.findOne(MolgenisUser.ENTITY_NAME, userId, MolgenisUser.class);
		mu.setActive(active);
		this.dataService.update(MolgenisUser.ENTITY_NAME, mu);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void setActivationGroup(String groupId, Boolean active)
	{
		MolgenisGroup mg = this.dataService.findOne(MolgenisGroup.ENTITY_NAME, groupId, MolgenisGroup.class);
		mg.setActive(active);
		this.dataService.update(MolgenisGroup.ENTITY_NAME, mg);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getAllMolgenisGroups()
	{

		Iterable<MolgenisGroup> groups = dataService.findAll(MolgenisGroup.ENTITY_NAME, MolgenisGroup.class);
		if (groups == null)
		{
			return Collections.emptyList();
		}

		return Lists.newArrayList(groups);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getGroupsWhereUserIsMember(String userId)
	{
		return this.getMolgenisGroups(userId);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisUserViewData> getUsersMemberInGroup(String groupId)
	{
		return this.parseToMolgenisUserViewData(this.getMolgenisUsers(groupId));
	}

	private List<MolgenisGroup> getMolgenisGroups(String userId)
	{
		final MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME, userId, MolgenisUser.class);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = Lists.newArrayList(dataService.findAll(
				MolgenisGroupMember.ENTITY_NAME, new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, molgenisUser),
				MolgenisGroupMember.class));

		return this.getAllMolgenisGroupsFromGroupMembers(groupMembers);
	}

	private List<MolgenisUser> getMolgenisUsers(final String groupId)
	{
		final MolgenisGroup molgenisGroup = dataService
				.findOne(MolgenisGroup.ENTITY_NAME, groupId, MolgenisGroup.class);

		if (molgenisGroup == null)
		{
			throw new RuntimeException("unknown user id [" + groupId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = Lists.newArrayList(dataService.findAll(
				MolgenisGroupMember.ENTITY_NAME, new QueryImpl().eq(MolgenisGroupMember.MOLGENISGROUP, molgenisGroup),
				MolgenisGroupMember.class));

		return this.getAllMolgenisUsersFromGroupMembers(groupMembers);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getGroupsWhereUserIsNotMember(final String userId)
	{
		final MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME, userId, MolgenisUser.class);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = Lists.newArrayList(dataService.findAll(
				MolgenisGroupMember.ENTITY_NAME, new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, molgenisUser),
				MolgenisGroupMember.class));

		final List<MolgenisGroup> groupsWhereUserIsMember = this.getAllMolgenisGroupsFromGroupMembers(groupMembers);

		Predicate<MolgenisGroup> predicate = new PredicateNotInMolgenisGroupList(groupsWhereUserIsMember);
		List<MolgenisGroup> molgenisGroups = this.getAllMolgenisGroups();

		return Lists.<MolgenisGroup> newArrayList(Iterables.filter(molgenisGroups, predicate));
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void addUserToGroup(String molgenisGroupId, String molgenisUserId)
	{
		MolgenisGroup group = dataService.findOne(MolgenisGroup.ENTITY_NAME, molgenisGroupId, MolgenisGroup.class);
		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME, molgenisUserId, MolgenisUser.class);

		MolgenisGroupMember molgenisGroupMember = new MolgenisGroupMember();
		molgenisGroupMember.setMolgenisGroup(group);
		molgenisGroupMember.setMolgenisUser(user);
		dataService.add(MolgenisGroupMember.ENTITY_NAME, molgenisGroupMember);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void removeUserFromGroup(String molgenisGroupId, String molgenisUserId)
	{
		final MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME, molgenisUserId,
				MolgenisUser.class);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisUserId + "]");
		}

		final MolgenisGroup molgenisGroup = dataService.findOne(MolgenisGroup.ENTITY_NAME, molgenisGroupId,
				MolgenisGroup.class);

		if (molgenisGroup == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisGroupId + "]");
		}

		Query q = new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, molgenisUser).and()
				.eq(MolgenisGroupMember.MOLGENISGROUP, molgenisGroup);

		final List<MolgenisGroupMember> molgenisGroupMembers = Lists.newArrayList(dataService.findAll(
				MolgenisGroupMember.ENTITY_NAME, q, MolgenisGroupMember.class));

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
			Object id = item.getId();
			for (MolgenisGroup toFilterItem : toFilterItemList)
			{
				if (toFilterItem.getId().equals(id)) return false;
			}
			return true;
		}

	}

	private List<MolgenisUserViewData> parseToMolgenisUserViewData(Iterable<MolgenisUser> users)
	{

		List<MolgenisUserViewData> results = new ArrayList<MolgenisUserViewData>();
		for (MolgenisUser user : users)
		{
			results.add(new MolgenisUserViewData(user, getMolgenisGroups(user.getId())));
		}
		return results;
	}
}
