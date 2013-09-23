package org.molgenis.security.permission;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;

/**
 * Manage user and group permissions for plugins and entity classes
 */
public interface PermissionManagerService
{
	List<MolgenisUser> getUsers() throws DatabaseException;

	List<MolgenisGroup> getGroups() throws DatabaseException;

	List<MolgenisPlugin> getPlugins() throws DatabaseException;

	List<String> getEntityClassIds() throws DatabaseException;

	List<GroupAuthority> getGroupPluginPermissions(Integer groupId) throws DatabaseException;

	List<GroupAuthority> getGroupEntityClassPermissions(Integer groupId) throws DatabaseException;

	List<? extends Authority> getUserPluginPermissions(Integer userId) throws DatabaseException;

	List<? extends Authority> getUserEntityClassPermissions(Integer userId) throws DatabaseException;

	void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, Integer groupId)
			throws DatabaseException;

	void replaceGroupEntityClassPermissions(List<GroupAuthority> entityAuthorities, Integer groupId)
			throws DatabaseException;

	void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, Integer userId) throws DatabaseException;

	void replaceUserEntityClassPermissions(List<UserAuthority> entityAuthorities, Integer userId)
			throws DatabaseException;
}