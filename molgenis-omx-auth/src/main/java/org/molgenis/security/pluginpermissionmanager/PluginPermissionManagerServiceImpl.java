package org.molgenis.security.pluginpermissionmanager;

import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;
import static org.molgenis.framework.db.QueryRule.Operator.IN;

import java.util.Arrays;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisGroupMember;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
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
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_PLUGINPERMISSIONMANAGER_READ_USER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisUser> getUsers() throws DatabaseException
	{
		return database.find(MolgenisUser.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_PLUGINPERMISSIONMANAGER_READ_USER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisGroup> getGroups() throws DatabaseException
	{
		return database.find(MolgenisGroup.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_PLUGINPERMISSIONMANAGER_READ_USER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<GroupAuthority> getGroupPluginPermissions(Integer groupId) throws DatabaseException
	{
		MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, groupId);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		return getGroupPluginPermissions(molgenisGroup);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_PLUGINPERMISSIONMANAGER_READ_USER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<? extends Authority> getUserPluginPermissions(Integer userId) throws DatabaseException
	{
		MolgenisUser molgenisUser = MolgenisUser.findById(database, userId);
		if (molgenisUser == null) throw new RuntimeException("unknown user id [" + userId + "]");
		List<Authority> pluginPermissions = getUserPluginPermissions(molgenisUser);
		List<MolgenisGroupMember> groupMembers = database.find(MolgenisGroupMember.class, new QueryRule(
				MolgenisGroupMember.MOLGENISUSER, Operator.EQUALS, molgenisUser));
		if (groupMembers != null && !groupMembers.isEmpty())
		{
			List<MolgenisGroup> molgenisGroups = Lists.transform(groupMembers,
					new Function<MolgenisGroupMember, MolgenisGroup>()
					{
						@Override
						public MolgenisGroup apply(MolgenisGroupMember molgenisGroupMember)
						{
							return molgenisGroupMember.getMolgenisGroup();
						}
					});

			List<GroupAuthority> groupAuthorities = database.find(GroupAuthority.class, new QueryRule(
					GroupAuthority.MOLGENISGROUP, Operator.IN, molgenisGroups));
			if (groupAuthorities != null && !groupAuthorities.isEmpty()) pluginPermissions.addAll(groupAuthorities);

		}

		return pluginPermissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_PLUGINPERMISSIONMANAGER_WRITE_USER')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, Integer groupId)
			throws DatabaseException
	{
		MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, groupId);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");

		// inject user
		for (GroupAuthority pluginAuthority : pluginAuthorities)
			pluginAuthority.setMolgenisGroup(molgenisGroup);

		// delete old plugin authorities
		List<GroupAuthority> oldPluginAuthorities = getGroupPluginPermissions(molgenisGroup);
		if (oldPluginAuthorities != null && !oldPluginAuthorities.isEmpty()) database.remove(oldPluginAuthorities);

		// insert new plugin authorities
		if (!pluginAuthorities.isEmpty()) database.add(pluginAuthorities);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_PLUGINPERMISSIONMANAGER_WRITE_USER')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, Integer userId)
			throws DatabaseException
	{
		MolgenisUser molgenisUser = MolgenisUser.findById(database, userId);
		if (molgenisUser == null) throw new RuntimeException("unknown user id [" + userId + "]");

		// inject user
		for (UserAuthority pluginAuthority : pluginAuthorities)
			pluginAuthority.setMolgenisUser(molgenisUser);

		// delete old plugin authorities
		List<? extends Authority> oldPluginAuthorities = getUserPluginPermissions(molgenisUser);
		if (oldPluginAuthorities != null && !oldPluginAuthorities.isEmpty()) database.remove(oldPluginAuthorities);

		// insert new plugin authorities
		if (!pluginAuthorities.isEmpty()) database.add(pluginAuthorities);
	}

	private List<Authority> getUserPluginPermissions(MolgenisUser molgenisUser) throws DatabaseException
	{
		List<UserAuthority> authorities = database.find(UserAuthority.class, new QueryRule(UserAuthority.MOLGENISUSER,
				EQUALS, molgenisUser));

		return Lists.<Authority> newArrayList(Iterables.filter(authorities, new Predicate<UserAuthority>()
		{
			@Override
			public boolean apply(UserAuthority authority)
			{
				return authority.getRole().startsWith(SecurityUtils.PLUGIN_AUTHORITY_PREFIX);
			}
		}));
	}

	private List<GroupAuthority> getGroupPluginPermissions(MolgenisGroup molgenisGroup) throws DatabaseException
	{
		return getGroupPluginPermissions(Arrays.asList(molgenisGroup));
	}

	private List<GroupAuthority> getGroupPluginPermissions(List<MolgenisGroup> molgenisGroups) throws DatabaseException
	{
		List<GroupAuthority> authorities = database.find(GroupAuthority.class, new QueryRule(
				GroupAuthority.MOLGENISGROUP, IN, molgenisGroups));
		return Lists.newArrayList(Iterables.filter(authorities, new Predicate<GroupAuthority>()
		{
			@Override
			public boolean apply(GroupAuthority authority)
			{
				return authority.getRole().startsWith(SecurityUtils.PLUGIN_AUTHORITY_PREFIX);
			}
		}));
	}
}
