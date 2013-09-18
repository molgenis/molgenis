package org.molgenis.security.permission;

import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;
import static org.molgenis.framework.db.QueryRule.Operator.IN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.model.elements.Entity;
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
public class PermissionManagerServiceImpl implements PermissionManagerService
{
	private final Database database;
	private final MolgenisPluginRegistry molgenisPluginRegistry;

	@Autowired
	public PermissionManagerServiceImpl(Database database, MolgenisPluginRegistry molgenisPluginRegistry)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		if (molgenisPluginRegistry == null) throw new IllegalArgumentException("Molgenis plugin registry is null");
		this.database = database;
		this.molgenisPluginRegistry = molgenisPluginRegistry;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_READ_PLUGINPERMISSIONMANAGER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisUser> getUsers() throws DatabaseException
	{
		return database.find(MolgenisUser.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_READ_PLUGINPERMISSIONMANAGER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisGroup> getGroups() throws DatabaseException
	{
		return database.find(MolgenisGroup.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_READ_PLUGINPERMISSIONMANAGER')")
	public List<MolgenisPlugin> getPlugins() throws DatabaseException
	{
		return new ArrayList<MolgenisPlugin>(molgenisPluginRegistry.getPlugins());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_READ_PLUGINPERMISSIONMANAGER')")
	public List<String> getEntityClassIds() throws DatabaseException
	{
		Vector<Entity> entities = database.getMetaData().getEntities(false, false);
		List<String> entityIds = new ArrayList<String>(entities.size());
		for (Entity entity : entities)
			entityIds.add(entity.getName());
		return entityIds;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_READ_PLUGINPERMISSIONMANAGER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<GroupAuthority> getGroupPluginPermissions(Integer groupId) throws DatabaseException
	{
		MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, groupId);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		return getGroupPermissions(molgenisGroup, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_READ_PLUGINPERMISSIONMANAGER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<GroupAuthority> getGroupEntityClassPermissions(Integer groupId) throws DatabaseException
	{
		MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, groupId);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		return getGroupPermissions(molgenisGroup, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_READ_PLUGINPERMISSIONMANAGER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<? extends Authority> getUserPluginPermissions(Integer userId) throws DatabaseException
	{
		return getUserPermissions(userId, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_READ_PLUGINPERMISSIONMANAGER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<? extends Authority> getUserEntityClassPermissions(Integer userId) throws DatabaseException
	{
		return getUserPermissions(userId, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
	}

	private List<? extends Authority> getUserPermissions(Integer userId, String authorityPrefix)
			throws DatabaseException
	{
		MolgenisUser molgenisUser = MolgenisUser.findById(database, userId);
		if (molgenisUser == null) throw new RuntimeException("unknown user id [" + userId + "]");
		List<Authority> userPermissions = getUserPermissions(molgenisUser, authorityPrefix);
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
			List<GroupAuthority> groupAuthorities = getGroupPermissions(molgenisGroups, authorityPrefix);
			if (groupAuthorities != null && !groupAuthorities.isEmpty()) userPermissions.addAll(groupAuthorities);

		}

		return userPermissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_WRITE_PLUGINPERMISSIONMANAGER')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, Integer groupId)
			throws DatabaseException
	{
		replaceGroupPermissions(pluginAuthorities, groupId, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_WRITE_PLUGINPERMISSIONMANAGER')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void replaceGroupEntityClassPermissions(List<GroupAuthority> entityAuthorities, Integer groupId)
			throws DatabaseException
	{
		replaceGroupPermissions(entityAuthorities, groupId, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
	}

	private void replaceGroupPermissions(List<GroupAuthority> entityAuthorities, Integer groupId, String authorityPrefix)
			throws DatabaseException
	{
		MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, groupId);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");

		// inject user
		for (GroupAuthority entityAuthority : entityAuthorities)
			entityAuthority.setMolgenisGroup(molgenisGroup);

		// delete old plugin authorities
		List<GroupAuthority> oldEntityAuthorities = getGroupPermissions(molgenisGroup, authorityPrefix);
		if (oldEntityAuthorities != null && !oldEntityAuthorities.isEmpty()) database.remove(oldEntityAuthorities);

		// insert new plugin authorities
		if (!entityAuthorities.isEmpty()) database.add(entityAuthorities);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_WRITE_PLUGINPERMISSIONMANAGER')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, Integer userId)
			throws DatabaseException
	{
		replaceUserPermissions(pluginAuthorities, userId, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_PLUGIN_WRITE_PLUGINPERMISSIONMANAGER')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void replaceUserEntityClassPermissions(List<UserAuthority> pluginAuthorities, Integer userId)
			throws DatabaseException
	{
		replaceUserPermissions(pluginAuthorities, userId, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
	}

	private void replaceUserPermissions(List<UserAuthority> entityAuthorities, Integer userId, String authorityType)
			throws DatabaseException
	{
		MolgenisUser molgenisUser = MolgenisUser.findById(database, userId);
		if (molgenisUser == null) throw new RuntimeException("unknown user id [" + userId + "]");

		// inject user
		for (UserAuthority entityAuthority : entityAuthorities)
			entityAuthority.setMolgenisUser(molgenisUser);

		// delete old plugin authorities
		List<? extends Authority> oldEntityAuthorities = getUserPermissions(molgenisUser, authorityType);
		if (oldEntityAuthorities != null && !oldEntityAuthorities.isEmpty()) database.remove(oldEntityAuthorities);

		// insert new plugin authorities
		if (!entityAuthorities.isEmpty()) database.add(entityAuthorities);
	}

	private List<Authority> getUserPermissions(MolgenisUser molgenisUser, final String authorityPrefix)
			throws DatabaseException
	{
		List<UserAuthority> authorities = database.find(UserAuthority.class, new QueryRule(UserAuthority.MOLGENISUSER,
				EQUALS, molgenisUser));

		return Lists.<Authority> newArrayList(Iterables.filter(authorities, new Predicate<UserAuthority>()
		{
			@Override
			public boolean apply(UserAuthority authority)
			{
				return authority.getRole().startsWith(authorityPrefix);
			}
		}));
	}

	private List<GroupAuthority> getGroupPermissions(MolgenisGroup molgenisGroup, String authorityPrefix)
			throws DatabaseException
	{
		return getGroupPermissions(Arrays.asList(molgenisGroup), authorityPrefix);
	}

	private List<GroupAuthority> getGroupPermissions(List<MolgenisGroup> molgenisGroups, final String authorityPrefix)
			throws DatabaseException
	{
		List<GroupAuthority> authorities = database.find(GroupAuthority.class, new QueryRule(
				GroupAuthority.MOLGENISGROUP, IN, molgenisGroups));
		return Lists.newArrayList(Iterables.filter(authorities, new Predicate<GroupAuthority>()
		{
			@Override
			public boolean apply(GroupAuthority authority)
			{
				return authority.getRole().startsWith(authorityPrefix);
			}
		}));
	}
}
