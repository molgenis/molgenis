package org.molgenis.security.permission;

import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;
import static org.molgenis.framework.db.QueryRule.Operator.IN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
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
	private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	@Autowired
	public PermissionManagerServiceImpl(Database database, MolgenisPluginRegistry molgenisPluginRegistry,
			GrantedAuthoritiesMapper grantedAuthoritiesMapper)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		if (molgenisPluginRegistry == null) throw new IllegalArgumentException("Molgenis plugin registry is null");
		if (grantedAuthoritiesMapper == null) throw new IllegalArgumentException("Granted authorities mapper is null");
		this.database = database;
		this.molgenisPluginRegistry = molgenisPluginRegistry;
		this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisUser> getUsers() throws DatabaseException
	{
		return database.find(MolgenisUser.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<MolgenisGroup> getGroups() throws DatabaseException
	{
		return database.find(MolgenisGroup.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public List<MolgenisPlugin> getPlugins() throws DatabaseException
	{
		return new ArrayList<MolgenisPlugin>(molgenisPluginRegistry.getPlugins());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public List<String> getEntityClassIds() throws DatabaseException
	{
		Vector<Entity> entities = database.getMetaData().getEntities(false, false);
		List<String> entityIds = new ArrayList<String>(entities.size());
		for (Entity entity : entities)
			entityIds.add(entity.getName());
		return entityIds;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public Permissions getGroupPluginPermissions(Integer groupId) throws DatabaseException
	{
		MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, groupId);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		List<Authority> groupPermissions = getGroupPermissions(molgenisGroup);
		Permissions permissions = createPermissions(groupPermissions, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
		permissions.setGroupId(groupId);
		return permissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public Permissions getGroupEntityClassPermissions(Integer groupId) throws DatabaseException
	{
		MolgenisGroup molgenisGroup = MolgenisGroup.findById(database, groupId);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		List<Authority> groupPermissions = getGroupPermissions(molgenisGroup);
		Permissions permissions = createPermissions(groupPermissions, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
		permissions.setGroupId(groupId);
		return permissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public Permissions getUserPluginPermissions(Integer userId) throws DatabaseException
	{
		List<? extends Authority> userPermissions = getUserPermissions(userId);
		Permissions permissions = createPermissions(userPermissions, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
		permissions.setUserId(userId);
		return permissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public Permissions getUserEntityClassPermissions(Integer userId) throws DatabaseException
	{
		List<? extends Authority> userPermissions = getUserPermissions(userId);
		Permissions permissions = createPermissions(userPermissions, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
		permissions.setUserId(userId);
		return permissions;
	}

	private List<? extends Authority> getUserPermissions(Integer userId) throws DatabaseException
	{
		return getUserPermissions(userId, null);
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
			List<Authority> groupAuthorities = getGroupPermissions(molgenisGroups, authorityPrefix);
			if (groupAuthorities != null && !groupAuthorities.isEmpty()) userPermissions.addAll(groupAuthorities);
		}

		return userPermissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, Integer groupId)
			throws DatabaseException
	{
		replaceGroupPermissions(pluginAuthorities, groupId, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
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
		List<Authority> oldEntityAuthorities = getGroupPermissions(molgenisGroup, authorityPrefix);
		if (oldEntityAuthorities != null && !oldEntityAuthorities.isEmpty()) database.remove(oldEntityAuthorities);

		// insert new plugin authorities
		if (!entityAuthorities.isEmpty()) database.add(entityAuthorities);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, Integer userId)
			throws DatabaseException
	{
		replaceUserPermissions(pluginAuthorities, userId, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
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

		return Lists.<Authority> newArrayList(Iterables.filter(authorities, new Predicate<Authority>()
		{
			@Override
			public boolean apply(Authority authority)
			{
				return authorityPrefix != null ? authority.getRole().startsWith(authorityPrefix) : true;
			}
		}));
	}

	private List<Authority> getGroupPermissions(MolgenisGroup molgenisGroup) throws DatabaseException
	{
		return getGroupPermissions(Arrays.asList(molgenisGroup));
	}

	private List<Authority> getGroupPermissions(MolgenisGroup molgenisGroup, String authorityPrefix)
			throws DatabaseException
	{
		return getGroupPermissions(Arrays.asList(molgenisGroup), authorityPrefix);
	}

	private List<Authority> getGroupPermissions(List<MolgenisGroup> molgenisGroups) throws DatabaseException
	{
		return getGroupPermissions(molgenisGroups, null);
	}

	private List<Authority> getGroupPermissions(List<MolgenisGroup> molgenisGroups, final String authorityPrefix)
			throws DatabaseException
	{
		List<GroupAuthority> authorities = database.find(GroupAuthority.class, new QueryRule(
				GroupAuthority.MOLGENISGROUP, IN, molgenisGroups));

		return Lists.<Authority> newArrayList(Iterables.filter(authorities, new Predicate<Authority>()
		{
			@Override
			public boolean apply(Authority authority)
			{
				return authorityPrefix != null ? authority.getRole().startsWith(authorityPrefix) : true;
			}
		}));
	}

	private Permissions createPermissions(List<? extends Authority> entityAuthorities, String authorityPrefix)
			throws DatabaseException
	{
		Permissions permissions = new Permissions();
		if (authorityPrefix.equals(SecurityUtils.AUTHORITY_PLUGIN_PREFIX))
		{
			List<MolgenisPlugin> plugins = this.getPlugins();
			if (plugins != null)
			{
				Collections.sort(plugins, new Comparator<MolgenisPlugin>()
				{
					@Override
					public int compare(MolgenisPlugin o1, MolgenisPlugin o2)
					{
						return o1.getName().compareTo(o2.getName());
					}
				});
				Map<String, String> pluginMap = new LinkedHashMap<String, String>();
				for (MolgenisPlugin plugin : plugins)
					pluginMap.put(plugin.getId(), plugin.getName());
				permissions.setEntityIds(pluginMap);
			}
		}
		else if (authorityPrefix.equals(SecurityUtils.AUTHORITY_ENTITY_PREFIX))
		{
			List<String> entityClassIds = this.getEntityClassIds();
			if (entityClassIds != null)
			{
				Map<String, String> entityClassMap = new TreeMap<String, String>();
				for (String entityClassId : entityClassIds)
					entityClassMap.put(entityClassId, entityClassId);
				permissions.setEntityIds(entityClassMap);
			}
		}
		else throw new RuntimeException("Invalid authority prefix [" + authorityPrefix + "]");

		for (Authority authority : entityAuthorities)
		{

			// add permissions for authorities that match prefix
			if (authority.getRole().startsWith(authorityPrefix))
			{
				Permission permission = new Permission();

				String authorityType = getAuthorityType(authority.getRole(), authorityPrefix);
				String authorityPluginId = getAuthorityEntityId(authority.getRole(), authorityPrefix);
				permission.setType(authorityType);
				if (authority instanceof GroupAuthority)
				{
					permission.setGroup(((GroupAuthority) authority).getMolgenisGroup().getName());
					permissions.addGroupPermission(authorityPluginId, permission);
				}
				else
				{
					permissions.addUserPermission(authorityPluginId, permission);
				}
			}

			// add permissions for inherited authorities from authority that match prefix
			SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority.getRole());
			Collection<? extends GrantedAuthority> hierarchyAuthorities = grantedAuthoritiesMapper
					.mapAuthorities(Collections.singletonList(grantedAuthority));
			hierarchyAuthorities.remove(grantedAuthority);

			for (GrantedAuthority hierarchyAuthority : hierarchyAuthorities)
			{
				if (hierarchyAuthority.getAuthority().startsWith(authorityPrefix))
				{
					String authorityPluginId = getAuthorityEntityId(hierarchyAuthority.getAuthority(), authorityPrefix);

					Permission hierarchyPermission = new Permission();
					hierarchyPermission.setType(getAuthorityType(hierarchyAuthority.getAuthority(), authorityPrefix));
					permissions.addHierarchyPermission(authorityPluginId, hierarchyPermission);
				}
			}
		}

		permissions.sort();
		return permissions;
	}

	private String getAuthorityEntityId(String role, String authorityPrefix)
	{
		role = role.substring(authorityPrefix.length());
		return role.substring(role.indexOf('_') + 1).toLowerCase();
	}

	private String getAuthorityType(String role, String authorityPrefix)
	{
		role = role.substring(authorityPrefix.length());
		return role.substring(0, role.indexOf('_')).toLowerCase();
	}
}
