package org.molgenis.core.ui.admin.usermanager;

import org.molgenis.data.security.auth.Role;

import java.util.List;

public interface UserManagerService
{
	List<UserViewData> getAllUsers();

	void setActivationUser(String userId, Boolean active);

	void setActivationGroup(String groupId, Boolean active);

	List<Role> getAllGroups();

	List<Role> getGroupsWhereUserIsMember(String userId);

	List<Role> getGroupsWhereUserIsNotMember(String userId);

	List<UserViewData> getUsersMemberInGroup(String groupId);

	void addUserToGroup(String molgenisGroupId, String molgenisUserId);

	void removeUserFromGroup(String molgenisGroupId, String molgenisUserId);
}
