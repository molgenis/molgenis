package org.molgenis.security.permission;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.molgenis.auth.Authority;
import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Service
public class PermissionManagerServiceImpl implements PermissionManagerService
{
	private final DataService dataService;
	private final MolgenisPluginRegistry molgenisPluginRegistry;
	private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	@Autowired
	public PermissionManagerServiceImpl(DataService dataService, MolgenisPluginRegistry molgenisPluginRegistry,
			GrantedAuthoritiesMapper grantedAuthoritiesMapper)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (molgenisPluginRegistry == null) throw new IllegalArgumentException("Molgenis plugin registry is null");
		if (grantedAuthoritiesMapper == null) throw new IllegalArgumentException("Granted authorities mapper is null");
		this.dataService = dataService;
		this.molgenisPluginRegistry = molgenisPluginRegistry;
		this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisUser> getUsers()
	{
		return dataService.findAll(MolgenisUser.ENTITY_NAME, MolgenisUser.class).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public List<MolgenisGroup> getGroups()
	{
		return dataService.findAll(MolgenisGroup.ENTITY_NAME, MolgenisGroup.class).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public List<MolgenisPlugin> getPlugins()
	{
		return Lists.newArrayList(molgenisPluginRegistry);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public List<String> getEntityClassIds()
	{
		return dataService.getEntityNames().collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getGroupPluginPermissions(String groupId)
	{
		MolgenisGroup molgenisGroup = dataService.findOne(MolgenisGroup.ENTITY_NAME, groupId, MolgenisGroup.class);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");

		List<Authority> groupPermissions = getGroupPermissions(molgenisGroup);
		Permissions permissions = createPermissions(groupPermissions, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
		permissions.setGroupId(groupId);
		return permissions;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getGroupEntityClassPermissions(String groupId)
	{
		MolgenisGroup molgenisGroup = dataService.findOne(MolgenisGroup.ENTITY_NAME, groupId, MolgenisGroup.class);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		List<Authority> groupPermissions = getGroupPermissions(molgenisGroup);
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
		MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME, userId, MolgenisUser.class);
		if (molgenisUser == null) throw new RuntimeException("unknown user id [" + userId + "]");
		List<Authority> userPermissions = getUserPermissions(molgenisUser, authorityPrefix);

		List<MolgenisGroupMember> groupMembers = dataService.findAll(MolgenisGroupMember.ENTITY_NAME,
				new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, molgenisUser), MolgenisGroupMember.class).collect(
				toList());

		if (!groupMembers.isEmpty())
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
		MolgenisGroup molgenisGroup = dataService.findOne(MolgenisGroup.ENTITY_NAME, groupId, MolgenisGroup.class);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");

		// inject user
		for (GroupAuthority entityAuthority : entityAuthorities)
			entityAuthority.setMolgenisGroup(molgenisGroup);

		// delete old plugin authorities
		Stream<Authority> oldEntityAuthorities = getGroupPermissions(molgenisGroup, authorityPrefix).stream();
		if (oldEntityAuthorities != null) dataService.delete(GroupAuthority.ENTITY_NAME, oldEntityAuthorities);

		// insert new plugin authorities
		if (!entityAuthorities.isEmpty()) dataService.add(GroupAuthority.ENTITY_NAME, entityAuthorities.stream());
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
		MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME, userId, MolgenisUser.class);
		if (molgenisUser == null) throw new RuntimeException("unknown user id [" + userId + "]");

		// inject user
		for (UserAuthority entityAuthority : entityAuthorities)
			entityAuthority.setMolgenisUser(molgenisUser);

		// delete old plugin authorities
		List<? extends Authority> oldEntityAuthorities = getUserPermissions(molgenisUser, authorityType);
		if (oldEntityAuthorities != null && !oldEntityAuthorities.isEmpty()) dataService.delete(
				UserAuthority.ENTITY_NAME, oldEntityAuthorities.stream());

		// insert new plugin authorities
		if (!entityAuthorities.isEmpty()) dataService.add(UserAuthority.ENTITY_NAME, entityAuthorities.stream());
	}

	private List<Authority> getUserPermissions(MolgenisUser molgenisUser, final String authorityPrefix)
	{
		Stream<UserAuthority> authorities = dataService.findAll(UserAuthority.ENTITY_NAME,
				new QueryImpl().eq(UserAuthority.MOLGENISUSER, molgenisUser), UserAuthority.class);

		return authorities.filter(authority -> {
			return authorityPrefix != null ? authority.getRole().startsWith(authorityPrefix) : true;
		}).collect(toList());
	}

	private List<Authority> getGroupPermissions(MolgenisGroup molgenisGroup)
	{
		return getGroupPermissions(Arrays.asList(molgenisGroup));
	}

	private List<Authority> getGroupPermissions(MolgenisGroup molgenisGroup, String authorityPrefix)
	{
		return getGroupPermissions(Arrays.asList(molgenisGroup), authorityPrefix);
	}

	private List<Authority> getGroupPermissions(List<MolgenisGroup> molgenisGroups)
	{
		return getGroupPermissions(molgenisGroups, null);
	}

	private List<Authority> getGroupPermissions(List<MolgenisGroup> molgenisGroups, final String authorityPrefix)
	{
		Stream<GroupAuthority> authorities = dataService.findAll(GroupAuthority.ENTITY_NAME,
				new QueryImpl().in(GroupAuthority.MOLGENISGROUP, molgenisGroups), GroupAuthority.class);

		return authorities.filter(authority -> {
			return authorityPrefix != null ? authority.getRole().startsWith(authorityPrefix) : true;
		}).collect(toList());
	}

	private Permissions createPermissions(List<? extends Authority> entityAuthorities, String authorityPrefix)
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
