package org.molgenis.security.pluginpermissionmanager;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;

public interface PluginPermissionManagerService
{
	List<MolgenisUser> getUsers() throws DatabaseException;

	List<MolgenisGroup> getGroups() throws DatabaseException;

	List<GroupAuthority> getGroupPluginPermissions(Integer groupId) throws DatabaseException;

	List<? extends Authority> getUserPluginPermissions(Integer userId) throws DatabaseException;

	void updateGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, Integer groupId) throws DatabaseException;

	void updateUserPluginPermissions(List<UserAuthority> pluginAuthorities, Integer userId) throws DatabaseException;
}