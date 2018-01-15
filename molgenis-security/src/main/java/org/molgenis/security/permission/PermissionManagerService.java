package org.molgenis.security.permission;

import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupAuthority;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserAuthority;

import java.util.List;

/**
 * Manage user and group permissions for plugins and entity classes
 */
public interface PermissionManagerService
{
	List<User> getUsers();

	List<Group> getGroups();

	List<Plugin> getPlugins();

	List<Object> getEntityClassIds();

	Permissions getGroupPluginPermissions(String groupId);

	Permissions getGroupEntityClassPermissions(String groupId);

	Permissions getUserPluginPermissions(String userId);

	Permissions getUserEntityClassPermissions(String userId);

	void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, String groupId);

	void replaceGroupEntityClassPermissions(List<GroupAuthority> entityAuthorities, String groupId);

	void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, String userId);

	void replaceUserEntityClassPermissions(List<UserAuthority> entityAuthorities, String userId);
}