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

	Permissions getGroupPluginPermissions(String groupId);

	Permissions getGroupEntityClassPermissions(String groupId);

	Permissions getUserPluginPermissions(String userId);

	Permissions getUserEntityClassPermissions(String userId);

	void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, String groupId);

	void replaceGroupEntityClassPermissions(List<GroupAuthority> entityAuthorities, String groupId);

	void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, String userId);

	void replaceUserEntityClassPermissions(List<UserAuthority> entityAuthorities, String userId);
}