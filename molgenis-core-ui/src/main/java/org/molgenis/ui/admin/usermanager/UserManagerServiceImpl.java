package org.molgenis.ui.admin.usermanager;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER;
import static org.molgenis.auth.MolgenisGroupMetaData.MOLGENIS_GROUP;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisGroupMemberFactory;
import org.molgenis.auth.MolgenisGroupMemberMetaData;
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
	private final MolgenisGroupMemberFactory molgenisGroupMemberFactory;

	@Autowired
	public UserManagerServiceImpl(DataService dataService, MolgenisGroupMemberFactory molgenisGroupMemberFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.molgenisGroupMemberFactory = requireNonNull(molgenisGroupMemberFactory);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisUserViewData> getAllMolgenisUsers()
	{
		Stream<MolgenisUser> users = dataService.findAll(MOLGENIS_USER, MolgenisUser.class);
		return this.parseToMolgenisUserViewData(users);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void setActivationUser(String userId, Boolean active)
	{
		MolgenisUser mu = this.dataService.findOneById(MOLGENIS_USER, userId, MolgenisUser.class);
		mu.setActive(active);
		this.dataService.update(MOLGENIS_USER, mu);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void setActivationGroup(String groupId, Boolean active)
	{
		MolgenisGroup mg = this.dataService.findOneById(MOLGENIS_GROUP, groupId, MolgenisGroup.class);
		mg.setActive(active);
		this.dataService.update(MOLGENIS_GROUP, mg);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getAllMolgenisGroups()
	{
		return dataService.findAll(MOLGENIS_GROUP, MolgenisGroup.class).collect(toList());
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
		return this.parseToMolgenisUserViewData(this.getMolgenisUsers(groupId).stream());
	}

	private List<MolgenisGroup> getMolgenisGroups(String userId)
	{
		final MolgenisUser molgenisUser = dataService.findOneById(MOLGENIS_USER, userId, MolgenisUser.class);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, molgenisUser),
				MolgenisGroupMember.class)
				.collect(toList());

		return this.getAllMolgenisGroupsFromGroupMembers(groupMembers);
	}

	private List<MolgenisUser> getMolgenisUsers(final String groupId)
	{
		final MolgenisGroup molgenisGroup = dataService.findOneById(MOLGENIS_GROUP, groupId,
				MolgenisGroup.class);

		if (molgenisGroup == null)
		{
			throw new RuntimeException("unknown user id [" + groupId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_GROUP, molgenisGroup),
				MolgenisGroupMember.class)
				.collect(toList());

		return this.getAllMolgenisUsersFromGroupMembers(groupMembers);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getGroupsWhereUserIsNotMember(final String userId)
	{
		final MolgenisUser molgenisUser = dataService.findOneById(MOLGENIS_USER, userId, MolgenisUser.class);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, molgenisUser),
				MolgenisGroupMember.class)
				.collect(toList());

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
		MolgenisGroup group = dataService.findOneById(MOLGENIS_GROUP, molgenisGroupId, MolgenisGroup.class);
		MolgenisUser user = dataService.findOneById(MOLGENIS_USER, molgenisUserId, MolgenisUser.class);

		MolgenisGroupMember molgenisGroupMember = molgenisGroupMemberFactory.create();
		molgenisGroupMember.setMolgenisGroup(group);
		molgenisGroupMember.setMolgenisUser(user);
		dataService.add(MOLGENIS_GROUP_MEMBER, molgenisGroupMember);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void removeUserFromGroup(String molgenisGroupId, String molgenisUserId)
	{
		final MolgenisUser molgenisUser = dataService.findOneById(MOLGENIS_USER, molgenisUserId,
				MolgenisUser.class);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisUserId + "]");
		}

		final MolgenisGroup molgenisGroup = dataService.findOneById(MOLGENIS_GROUP, molgenisGroupId,
				MolgenisGroup.class);

		if (molgenisGroup == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisGroupId + "]");
		}

		Query<MolgenisGroupMember> q = new QueryImpl<MolgenisGroupMember>()
				.eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, molgenisUser).and()
				.eq(MolgenisGroupMemberMetaData.MOLGENIS_GROUP, molgenisGroup);

		final List<MolgenisGroupMember> molgenisGroupMembers = dataService
				.findAll(MOLGENIS_GROUP_MEMBER, q, MolgenisGroupMember.class).collect(toList());

		if (null == molgenisGroupMembers || molgenisGroupMembers.isEmpty())
		{
			throw new RuntimeException("molgenis group member is not found");
		}

		if (molgenisGroupMembers.size() > 1)
		{
			throw new RuntimeException("there are more than one group member found");
		}

		MolgenisGroupMember molgenisGroupMember = molgenisGroupMembers.get(0);
		dataService.delete(MOLGENIS_GROUP_MEMBER, molgenisGroupMember);
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

	private List<MolgenisUserViewData> parseToMolgenisUserViewData(Stream<MolgenisUser> users)
	{
		return users.map(user -> new MolgenisUserViewData(user, getMolgenisGroups(user.getId()))).collect(toList());
	}
}
