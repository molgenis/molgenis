package org.molgenis.security.pluginpermissionmanager;

import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Service
public class PluginPermissionManagerServiceImpl implements PluginPermissionManagerService
{
	private final Database database;

	@Autowired
	public PluginPermissionManagerServiceImpl(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisUser> getUsers() throws DatabaseException
	{
		return database.find(MolgenisUser.class);
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<Authority> getPluginPermissions(Integer userId) throws DatabaseException
	{
		MolgenisUser molgenisUser = MolgenisUser.findById(database, userId);
		if (molgenisUser == null) throw new RuntimeException("unknown user id [" + userId + "]");
		return getPluginPermissions(molgenisUser);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_PLUGINPERMISSIONMANAGER_WRITE_USER')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void updatePluginPermissions(List<Authority> pluginAuthorities, Integer userId) throws DatabaseException
	{
		MolgenisUser molgenisUser = MolgenisUser.findById(database, userId);
		if (molgenisUser == null) throw new RuntimeException("unknown user id [" + userId + "]");

		// inject user
		for (Authority pluginAuthority : pluginAuthorities)
			pluginAuthority.setMolgenisUser(molgenisUser);

		// delete old plugin authorities
		List<Authority> oldPluginAuthorities = getPluginPermissions(molgenisUser);
		if (oldPluginAuthorities != null && !oldPluginAuthorities.isEmpty()) database.remove(oldPluginAuthorities);

		// insert new plugin authorities
		if (!pluginAuthorities.isEmpty()) database.add(pluginAuthorities);
	}

	private List<Authority> getPluginPermissions(MolgenisUser molgenisUser) throws DatabaseException
	{
		List<Authority> authorities = database.find(Authority.class, new QueryRule(Authority.MOLGENISUSER, EQUALS,
				molgenisUser));

		return Lists.newArrayList(Iterables.filter(authorities, new Predicate<Authority>()
		{
			@Override
			public boolean apply(Authority authority)
			{
				return authority.getRole().startsWith(SecurityUtils.PLUGIN_AUTHORITY_PREFIX);
			}
		}));
	}
}
