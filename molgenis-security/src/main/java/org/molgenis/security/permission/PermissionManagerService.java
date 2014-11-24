package org.molgenis.security.permission;

import java.util.List;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.framework.ui.MolgenisPlugin;

/**
 * Manage user and group permissions for plugins and entity classes
 */
public interface PermissionManagerService
{
	List<MolgenisUser> getUsers();

	List<MolgenisGroup> getGroups();

	List<MolgenisPlugin> getPlugins();

	List<String> getEntityClassIds();

	Permissions getGroupPluginPermissions(Integer groupId);

	Permissions getGroupEntityClassPermissions(Integer groupId);

	Permissions getUserPluginPermissions(Integer userId);

	Permissions getUserEntityClassPermissions(Integer userId);

	void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, Integer groupId);

	void replaceGroupEntityClassPermissions(List<GroupAuthority> entityAuthorities, Integer groupId);

	void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, Integer userId);

	void replaceUserEntityClassPermissions(List<UserAuthority> entityAuthorities, Integer userId);
}