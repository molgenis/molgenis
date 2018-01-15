package org.molgenis.core.ui.admin.usermanager;

import org.molgenis.data.security.auth.Group;

import java.util.List;

public interface UserManagerService
{
	List<UserViewData> getAllUsers();

	void setActivationUser(String userId, Boolean active);

	void setActivationGroup(String groupId, Boolean active);

	List<Group> getAllGroups();

	List<Group> getGroupsWhereUserIsMember(String userId);

	List<Group> getGroupsWhereUserIsNotMember(String userId);

	List<UserViewData> getUsersMemberInGroup(String groupId);

	void addUserToGroup(String molgenisGroupId, String molgenisUserId);

	void removeUserFromGroup(String molgenisGroupId, String molgenisUserId);
}
