package org.molgenis.security.usermanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
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
	private static final Logger logger = Logger.getLogger(UserManagerServiceImpl.class);
	private final Database database;

	@Autowired
	public UserManagerServiceImpl(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisUserViewData> getAllMolgenisUsers() throws DatabaseException
	{
		return this.parseToMolgenisUserViewData(database.find(MolgenisUser.class));
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisGroup> getAllMolgenisGroups() throws DatabaseException
	{
		return database.find(MolgenisGroup.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisGroup> getGroupsWhereUserIsMember(Integer userId) throws DatabaseException
	{
		return this.getMolgenisGroups(userId);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisUserViewData> getUsersMemberInGroup(Integer groupId) throws DatabaseException
	{
		return this.parseToMolgenisUserViewData(this.getMolgenisUsers(groupId));
	}

	private List<MolgenisGroup> getMolgenisGroups(Integer userId) throws DatabaseException
	{
		final MolgenisUser molgenisUser = MolgenisUser.findById(database, userId);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = database.find(MolgenisGroupMember.class, new QueryRule(
				MolgenisGroupMember.MOLGENISUSER, Operator.EQUALS, molgenisUser));

		return this.getAllMolgenisGroupsFromGroupMembers(groupMembers);
	}

	private List<MolgenisUser> getMolgenisUsers(final Integer groupId) throws DatabaseException
	{
		final MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, groupId);

		if (molgenisGroup == null)
		{
			throw new RuntimeException("unknown user id [" + groupId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = database.find(MolgenisGroupMember.class, new QueryRule(
				MolgenisGroupMember.MOLGENISGROUP, Operator.EQUALS, molgenisGroup));

		return this.getAllMolgenisUsersFromGroupMembers(groupMembers);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisGroup> getGroupsWhereUserIsNotMember(final Integer userId) throws DatabaseException
	{
		final MolgenisUser molgenisUser = MolgenisUser.findById(database, userId);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}

		final List<MolgenisGroupMember> groupMembers = database.find(MolgenisGroupMember.class, 
				new QueryRule(MolgenisGroupMember.MOLGENISUSER, Operator.EQUALS, molgenisUser));
		final List<MolgenisGroup> groupsWhereUserIsMember = this.getAllMolgenisGroupsFromGroupMembers(groupMembers);
		
		Predicate<MolgenisGroup> predicate = new PredicateNotInMolgenisGroupList(groupsWhereUserIsMember);
		List<MolgenisGroup> molgenisGroups = this.getAllMolgenisGroups();
		
		return Lists.<MolgenisGroup> newArrayList(Iterables.filter(molgenisGroups, predicate));
	}
	
	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public Integer addUserToGroup(Integer molgenisGrougId, Integer molgenisUserId) throws DatabaseException
	{
		MolgenisGroupMember molgenisGroupMember = new MolgenisGroupMember();
		molgenisGroupMember.setMolgenisGroup(molgenisGrougId);
		molgenisGroupMember.setMolgenisUser(molgenisUserId);
		return database.add(molgenisGroupMember);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public Integer removeUserFromGroup(Integer molgenisGroupId, Integer molgenisUserId)
			throws DatabaseException
	{
		final MolgenisUser molgenisUser = MolgenisUser.findById(database, molgenisUserId);

		if (molgenisUser == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisUserId + "]");
		}
		
		final MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, molgenisGroupId);

		if (molgenisGroup == null)
		{
			throw new RuntimeException("unknown user id [" + molgenisGroupId + "]");
		}
		
		final List<MolgenisGroupMember> molgenisGroupMembers = database.find(MolgenisGroupMember.class, 
				new QueryRule(MolgenisGroupMember.MOLGENISUSER, Operator.EQUALS, molgenisUser), 
				new QueryRule(MolgenisGroupMember.MOLGENISGROUP, Operator.EQUALS, molgenisGroup));
		
		if(null == molgenisGroupMembers || molgenisGroupMembers.isEmpty()){
			throw new RuntimeException("molgenis group member is not found");
		}
		
		if(molgenisGroupMembers.size() > 1){
			throw new RuntimeException("there are more than one group member found");
		}
		
		MolgenisGroupMember molgenisGroupMember = molgenisGroupMembers.get(0);

		return database.remove(molgenisGroupMember);
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
	
	private static class PredicateNotInMolgenisGroupList implements Predicate<MolgenisGroup> {
		final List<MolgenisGroup> toFilterItemList;
		
		PredicateNotInMolgenisGroupList(List<MolgenisGroup> notInList){
			this.toFilterItemList = notInList;
		}
		
		@Override
		public boolean apply(MolgenisGroup item)
		{
			Integer id = item.getId();
			for(MolgenisGroup toFilterItem: toFilterItemList){
				if(toFilterItem.getId().equals(id)) return false;
			}
			return true;
		}
		
	}
	
	private List<MolgenisUserViewData> parseToMolgenisUserViewData(List<MolgenisUser> users){
		List<MolgenisUserViewData> results = new ArrayList<MolgenisUserViewData>();
		for(MolgenisUser user: users){
			results.add(new MolgenisUserViewData(user.getId(), user.getUsername()));
		}
		return results;
	}
}
