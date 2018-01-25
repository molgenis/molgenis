package org.molgenis.core.ui.admin.usermanager;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserMetaData.USER;

/**
 * Manage user in groups
 */
@Service
public class UserManagerServiceImpl implements UserManagerService
{
	private final DataService dataService;
	private final GroupMemberFactory groupMemberFactory;

	public UserManagerServiceImpl(DataService dataService, GroupMemberFactory groupMemberFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.groupMemberFactory = requireNonNull(groupMemberFactory);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<UserViewData> getAllUsers()
	{
		Stream<User> users = dataService.findAll(USER, User.class);
		return this.parseToMolgenisUserViewData(users);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void setActivationUser(String userId, Boolean active)
	{
		User mu = this.dataService.findOneById(USER, userId, User.class);
		mu.setActive(active);
		this.dataService.update(USER, mu);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void setActivationGroup(String groupId, Boolean active)
	{
		Group mg = this.dataService.findOneById(GROUP, groupId, Group.class);
		mg.setActive(active);
		this.dataService.update(GROUP, mg);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<Group> getAllGroups()
	{
		return dataService.findAll(GROUP, Group.class).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<Group> getGroupsWhereUserIsMember(String userId)
	{
		return this.getMolgenisGroups(userId);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<UserViewData> getUsersMemberInGroup(String groupId)
	{
		return this.parseToMolgenisUserViewData(this.getMolgenisUsers(groupId).stream());
	}

	private List<Group> getMolgenisGroups(String userId)
	{
		final User user = dataService.findOneById(USER, userId, User.class);

		if (user == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<GroupMember> groupMembers = dataService.findAll(GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user), GroupMember.class).collect(toList());

		return this.getAllMolgenisGroupsFromGroupMembers(groupMembers);
	}

	private List<User> getMolgenisUsers(final String groupId)
	{
		final Group group = dataService.findOneById(GROUP, groupId, Group.class);

		if (group == null)
		{
			throw new RuntimeException("unknown user id [" + groupId + "]");
		}

		final List<GroupMember> groupMembers = dataService.findAll(GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.GROUP, group), GroupMember.class).collect(toList());

		return this.getAllMolgenisUsersFromGroupMembers(groupMembers);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<Group> getGroupsWhereUserIsNotMember(final String userId)
	{
		final User user = dataService.findOneById(USER, userId, User.class);

		if (user == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<GroupMember> groupMembers = dataService.findAll(GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user), GroupMember.class).collect(toList());

		final List<Group> groupsWhereUserIsMember = this.getAllMolgenisGroupsFromGroupMembers(groupMembers);

		Predicate<Group> predicate = new PredicateNotInMolgenisGroupList(groupsWhereUserIsMember);
		List<Group> groups = this.getAllGroups();

		return Lists.newArrayList(Iterables.filter(groups, predicate));
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void addUserToGroup(String molgenisGroupId, String molgenisUserId)
	{
		Group group = dataService.findOneById(GROUP, molgenisGroupId, Group.class);
		User user = dataService.findOneById(USER, molgenisUserId, User.class);

		GroupMember groupMember = groupMemberFactory.create();
		groupMember.setGroup(group);
		groupMember.setUser(user);
		dataService.add(GROUP_MEMBER, groupMember);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void removeUserFromGroup(String molgenisGroupId, String molgenisUserId)
	{
		final User user = dataService.findOneById(USER, molgenisUserId, User.class);

		if (user == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisUserId + "]");
		}

		final Group group = dataService.findOneById(GROUP, molgenisGroupId, Group.class);

		if (group == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisGroupId + "]");
		}

		Query<GroupMember> q = new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user)
														   .and()
														   .eq(GroupMemberMetaData.GROUP, group);

		final List<GroupMember> groupMembers = dataService.findAll(GROUP_MEMBER, q, GroupMember.class)
														  .collect(toList());

		if (null == groupMembers || groupMembers.isEmpty())
		{
			throw new RuntimeException("molgenis group member is not found");
		}

		if (groupMembers.size() > 1)
		{
			throw new RuntimeException("there are more than one group member found");
		}

		GroupMember groupMember = groupMembers.get(0);
		dataService.delete(GROUP_MEMBER, groupMember);
	}

	/**
	 * Get All the molgenis groups from the list of molgenis group members
	 *
	 * @param groupMembers A list of MolgenisGroupMember instances
	 * @return List<MolgenisGroup>
	 */
	private List<Group> getAllMolgenisGroupsFromGroupMembers(final List<GroupMember> groupMembers)
	{
		List<Group> groups = new ArrayList<>();

		if (groupMembers != null && !groupMembers.isEmpty())
		{
			groups = Lists.transform(groupMembers, GroupMember::getGroup);
		}

		return groups;
	}

	/**
	 * Get All the molgenis users from the list of molgenis group members
	 *
	 * @param groupMembers A list of MolgenisGroupMember instances
	 * @return List<MolgenisUser>
	 */
	private List<User> getAllMolgenisUsersFromGroupMembers(final List<GroupMember> groupMembers)
	{
		List<User> user = new ArrayList<>();

		if (groupMembers != null && !groupMembers.isEmpty())
		{
			user = Lists.transform(groupMembers, GroupMember::getUser);
		}

		return user;
	}

	private static class PredicateNotInMolgenisGroupList implements Predicate<Group>
	{
		final List<Group> toFilterItemList;

		PredicateNotInMolgenisGroupList(List<Group> notInList)
		{
			this.toFilterItemList = notInList;
		}

		@Override
		public boolean apply(Group item)
		{
			Object id = item.getId();
			for (Group toFilterItem : toFilterItemList)
			{
				if (toFilterItem.getId().equals(id)) return false;
			}
			return true;
		}

	}

	private List<UserViewData> parseToMolgenisUserViewData(Stream<User> users)
	{
		return users.map(user -> new UserViewData(user, getMolgenisGroups(user.getId()))).collect(toList());
	}
}
