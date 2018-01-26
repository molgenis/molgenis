package org.molgenis.security.permission;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.data.security.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.security.auth.UserMetaData.USER;

@Service
public class PermissionManagerServiceImpl implements PermissionManagerService
{
	private final DataService dataService;
	private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	public PermissionManagerServiceImpl(DataService dataService, GrantedAuthoritiesMapper grantedAuthoritiesMapper)
	{
		this.dataService = requireNonNull(dataService);
		this.grantedAuthoritiesMapper = requireNonNull(grantedAuthoritiesMapper);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<User> getUsers()
	{
		return dataService.findAll(USER, User.class).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<Group> getGroups()
	{
		return dataService.findAll(GROUP, Group.class).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<Plugin> getPlugins()
	{
		return dataService.findAll(PLUGIN, Plugin.class).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public List<Object> getEntityClassIds()
	{
		return dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA).map(Entity::getIdValue).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getGroupPluginPermissions(String groupId)
	{
		Group group = dataService.findOneById(GROUP, groupId, Group.class);
		if (group == null) throw new RuntimeException("unknown group id [" + groupId + "]");

		List<Authority> groupPermissions = getGroupPermissions(group);
		Permissions permissions = createPermissions(groupPermissions, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
		permissions.setGroupId(groupId);
		return permissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getGroupEntityClassPermissions(String groupId)
	{
		Group group = dataService.findOneById(GROUP, groupId, Group.class);
		if (group == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		List<Authority> groupPermissions = getGroupPermissions(group);
		Permissions permissions = createPermissions(groupPermissions, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
		permissions.setGroupId(groupId);
		return permissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getUserPluginPermissions(String userId)
	{
		List<? extends Authority> userPermissions = getUserPermissions(userId);
		Permissions permissions = createPermissions(userPermissions, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
		permissions.setUserId(userId);
		return permissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getUserEntityClassPermissions(String userId)
	{
		List<? extends Authority> userPermissions = getUserPermissions(userId);

		Permissions permissions = createPermissions(userPermissions, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
		permissions.setUserId(userId);
		return permissions;
	}

	private List<? extends Authority> getUserPermissions(String userId)
	{
		return getUserPermissions(userId, null);
	}

	private List<? extends Authority> getUserPermissions(String userId, String authorityPrefix)
	{
		User user = dataService.findOneById(USER, userId, User.class);
		if (user == null) throw new RuntimeException("unknown user id [" + userId + "]");
		List<Authority> userPermissions = getUserPermissions(user, authorityPrefix);

		List<GroupMember> groupMembers = dataService.findAll(GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user), GroupMember.class).collect(toList());

		if (!groupMembers.isEmpty())
		{
			List<Group> groups = Lists.transform(groupMembers, GroupMember::getGroup);
			List<Authority> groupAuthorities = getGroupPermissions(groups, authorityPrefix);
			if (groupAuthorities != null && !groupAuthorities.isEmpty()) userPermissions.addAll(groupAuthorities);
		}

		return userPermissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, String groupId)
	{
		replaceGroupPermissions(pluginAuthorities, groupId, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void replaceGroupEntityClassPermissions(List<GroupAuthority> entityAuthorities, String groupId)
	{
		replaceGroupPermissions(entityAuthorities, groupId, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
	}

	private void replaceGroupPermissions(List<GroupAuthority> entityAuthorities, String groupId, String authorityPrefix)
	{
		Group group = dataService.findOneById(GROUP, groupId, Group.class);
		if (group == null) throw new RuntimeException("unknown group id [" + groupId + "]");

		// inject user
		for (GroupAuthority entityAuthority : entityAuthorities)
			entityAuthority.setGroup(group);

		// delete old plugin authorities
		Stream<Authority> oldEntityAuthorities = getGroupPermissions(group, authorityPrefix).stream();
		if (oldEntityAuthorities != null) dataService.delete(GROUP_AUTHORITY, oldEntityAuthorities);

		// insert new plugin authorities
		if (!entityAuthorities.isEmpty()) dataService.add(GROUP_AUTHORITY, entityAuthorities.stream());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, String userId)
	{
		replaceUserPermissions(pluginAuthorities, userId, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void replaceUserEntityClassPermissions(List<UserAuthority> pluginAuthorities, String userId)
	{
		replaceUserPermissions(pluginAuthorities, userId, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
	}

	private void replaceUserPermissions(List<UserAuthority> entityAuthorities, String userId, String authorityType)
	{
		User user = dataService.findOneById(USER, userId, User.class);
		if (user == null) throw new RuntimeException("unknown user id [" + userId + "]");

		// inject user
		for (UserAuthority entityAuthority : entityAuthorities)
			entityAuthority.setUser(user);

		// delete old plugin authorities
		List<? extends Authority> oldEntityAuthorities = getUserPermissions(user, authorityType);
		if (oldEntityAuthorities != null && !oldEntityAuthorities.isEmpty())
			dataService.delete(USER_AUTHORITY, oldEntityAuthorities.stream());

		// insert new plugin authorities
		if (!entityAuthorities.isEmpty()) dataService.add(USER_AUTHORITY, entityAuthorities.stream());
	}

	private List<Authority> getUserPermissions(User user, final String authorityPrefix)
	{
		Stream<UserAuthority> authorities = dataService.findAll(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user), UserAuthority.class);

		return authorities.filter(
				authority -> authorityPrefix != null ? authority.getRole().startsWith(authorityPrefix) : true)
						  .collect(toList());
	}

	private List<Authority> getGroupPermissions(Group group)
	{
		return getGroupPermissions(Arrays.asList(group));
	}

	private List<Authority> getGroupPermissions(Group group, String authorityPrefix)
	{
		return getGroupPermissions(Arrays.asList(group), authorityPrefix);
	}

	private List<Authority> getGroupPermissions(List<Group> groups)
	{
		return getGroupPermissions(groups, null);
	}

	private List<Authority> getGroupPermissions(List<Group> groups, final String authorityPrefix)
	{
		Stream<GroupAuthority> authorities = dataService.findAll(GROUP_AUTHORITY,
				new QueryImpl<GroupAuthority>().in(GroupAuthorityMetaData.GROUP, groups), GroupAuthority.class);

		return authorities.filter(
				authority -> authorityPrefix != null ? authority.getRole().startsWith(authorityPrefix) : true)
						  .collect(toList());
	}

	private Permissions createPermissions(List<? extends Authority> entityAuthorities, String authorityPrefix)
	{
		Permissions permissions = new Permissions();
		switch (authorityPrefix)
		{
			case SecurityUtils.AUTHORITY_PLUGIN_PREFIX:
				List<Plugin> plugins = this.getPlugins();
				if (plugins != null)
				{
					plugins.sort(Comparator.comparing(Plugin::getId));
					Map<String, String> pluginMap = new LinkedHashMap<>();
					for (Plugin plugin : plugins)
						pluginMap.put(plugin.getId(), plugin.getId());
					permissions.setEntityIds(pluginMap);
				}
				break;
			case SecurityUtils.AUTHORITY_ENTITY_PREFIX:
				List<Object> entityClassIds = this.getEntityClassIds();
				List<EntityType> entityTypes = dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA,
						entityClassIds.stream(),
						new Fetch().field(EntityTypeMetadata.ID).field(EntityTypeMetadata.PACKAGE), EntityType.class)
														  .collect(Collectors.toList());
				if (entityClassIds != null)
				{
					Map<String, String> entityClassMap = new TreeMap<>();
					for (EntityType entityType : entityTypes)
						entityClassMap.put(entityType.getId(), entityType.getId());
					permissions.setEntityIds(entityClassMap);
				}
				break;
			default:
				throw new RuntimeException("Invalid authority prefix [" + authorityPrefix + "]");
		}

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
					permission.setGroup(((GroupAuthority) authority).getGroup().getName());
					permissions.addGroupPermission(authorityPluginId, permission);
				}
				else
				{
					permissions.addUserPermission(authorityPluginId, permission);
				}
			}

			// add permissions for inherited authorities from authority that match prefix
			SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority.getRole());
			Collection<? extends GrantedAuthority> hierarchyAuthorities = grantedAuthoritiesMapper.mapAuthorities(
					Collections.singletonList(grantedAuthority));
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
