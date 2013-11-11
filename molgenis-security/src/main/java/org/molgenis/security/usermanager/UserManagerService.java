package org.molgenis.security.usermanager;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisGroup;

public interface UserManagerService
{
	List<MolgenisUserViewData> getAllMolgenisUsers() throws DatabaseException;

	List<MolgenisGroup> getAllMolgenisGroups() throws DatabaseException;

	List<MolgenisGroup> getGroupsWhereUserIsMember(Integer userId) throws DatabaseException;

	List<MolgenisGroup> getGroupsWhereUserIsNotMember(Integer userId) throws DatabaseException;

	List<MolgenisUserViewData> getUsersMemberInGroup(Integer groupId) throws DatabaseException;

	Integer addUserToGroup(Integer molgenisGroupId, Integer molgenisUserId) throws DatabaseException;

	Integer removeUserFromGroup(Integer molgenisGroupId, Integer molgenisUserId) throws DatabaseException;
}
