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
import org.molgenis.omx.auth.db.MolgenisGroupMemberEntityImporter;
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
	public List<MolgenisUser> getAllMolgenisUsers() throws DatabaseException
	{
		return database.find(MolgenisUser.class);
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
	public List<MolgenisUser> getUsersMemberInGroup(Integer groupId) throws DatabaseException
	{
		return this.getMolgenisUsers(groupId);
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
	
	protected class PredicateNotInMolgenisGroupList implements Predicate<MolgenisGroup> {
		final List<MolgenisGroup> toFilterItemList;
		
		PredicateNotInMolgenisGroupList(List<MolgenisGroup> notInList){
			this.toFilterItemList = notInList;
			
			for(MolgenisGroup group: this.toFilterItemList){
				logger.info(group);
			}
		}
		
		@Override
		public boolean apply(MolgenisGroup item)
		{
			logger.info("item: " + item);
			Integer id = item.getId();
			for(MolgenisGroup toFilterItem: toFilterItemList){
				if(toFilterItem.getId().equals(id)) return false;
				logger.info("toFilterItem.getId().equals(id);: " + toFilterItem.getId().equals(id));
			}
			return true;
		}
		
	}

	@Override
	public List<MolgenisUser> addGroup(Integer molgenisGroup_id, Integer molgenisUser_id) throws DatabaseException
	{
		MolgenisGroupMember molgenisGroupMember = new MolgenisGroupMember();
		molgenisGroupMember.setMolgenisGroup(molgenisGroup_id);
		molgenisGroupMember.setMolgenisUser(molgenisUser_id);
		database.add(molgenisGroupMember);
		
		// TODO THIS
		return null;
	}
}
