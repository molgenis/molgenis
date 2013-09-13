package org.molgenis.security.pluginpermissionmanager;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.MolgenisUser;

public interface PluginPermissionManagerService
{
	List<MolgenisUser> getUsers() throws DatabaseException;

	List<Authority> getPluginPermissions(Integer userId) throws DatabaseException;

	void updatePluginPermissions(List<Authority> pluginAuthorities, Integer userId) throws DatabaseException;
}