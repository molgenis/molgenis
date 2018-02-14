package org.molgenis.security.permission;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.plugin.model.PluginPermissionUtils;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.EntityTypePermissionUtils;
import org.molgenis.data.security.auth.*;
import org.molgenis.security.acl.SidUtils;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserMetaData.USER;

/**
 * @deprecated use {@link org.springframework.security.acls.model.MutableAclService}
 */
@Deprecated
@Service
public class PermissionManagerServiceImpl implements PermissionManagerService
{
	private final DataService dataService;
	private final MutableAclService mutableAclService;

	public PermissionManagerServiceImpl(DataService dataService, MutableAclService mutableAclService)
	{
		this.dataService = requireNonNull(dataService);
		this.mutableAclService = requireNonNull(mutableAclService);
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
		return getEntityTypes().map(EntityType::getId).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getGroupPluginPermissions(String groupId)
	{
		Group group = getGroup(groupId);
		return getPluginPermissions(group);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getGroupEntityClassPermissions(String groupId)
	{
		Group group = getGroup(groupId);
		return getEntityTypePermissions(group);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getUserPluginPermissions(String userId)
	{
		User user = getUser(userId);
		return getPluginPermissions(user);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public Permissions getUserEntityClassPermissions(String userId)
	{
		User user = getUser(userId);
		return getEntityTypePermissions(user);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void replaceGroupPluginPermissions(List<GroupAuthority> pluginAuthorities, String groupId)
	{
		Group group = getGroup(groupId);
		Sid sid = SidUtils.createSid(group);
		removeSidPluginPermissions(sid);
		Map<String, PluginPermission> pluginPermissions = toPluginPermissions(pluginAuthorities);
		replaceSidPluginPermissions(sid, pluginPermissions);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void replaceGroupEntityClassPermissions(List<GroupAuthority> entityTypeAuthorities, String groupId)
	{
		Group group = getGroup(groupId);
		Sid sid = SidUtils.createSid(group);
		removeSidEntityTypePermissions(sid);
		Map<String, EntityTypePermission> entityTypePermissions = toEntityTypePermissions(entityTypeAuthorities);
		replaceSidEntityTypePermissions(sid, entityTypePermissions);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void replaceUserPluginPermissions(List<UserAuthority> pluginAuthorities, String userId)
	{
		User user = getUser(userId);
		Sid sid = SidUtils.createSid(user);
		removeSidPluginPermissions(sid);
		Map<String, PluginPermission> pluginPermissions = toPluginPermissions(pluginAuthorities);
		replaceSidPluginPermissions(sid, pluginPermissions);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public void replaceUserEntityClassPermissions(List<UserAuthority> entityTypeAuthorities, String userId)
	{
		User user = getUser(userId);
		Sid sid = SidUtils.createSid(user);
		removeSidEntityTypePermissions(sid);
		Map<String, EntityTypePermission> entityTypePermissions = toEntityTypePermissions(entityTypeAuthorities);
		replaceSidEntityTypePermissions(sid, entityTypePermissions);
	}

	private Stream<EntityType> getEntityTypes()
	{
		return dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class);
	}

	private Group getGroup(String groupId)
	{
		Group group = dataService.findOneById(GROUP, groupId, Group.class);
		if (group == null)
		{
			throw new RuntimeException("unknown group id [" + groupId + "]");
		}
		return group;
	}

	private User getUser(String userId)
	{
		User user = dataService.findOneById(USER, userId, User.class);
		if (user == null)
		{
			throw new RuntimeException("unknown user id [" + userId + "]");
		}
		return user;
	}

	private Permissions getPluginPermissions(User user)
	{
		Sid sid = SidUtils.createSid(user);
		return getPluginPermissions(sid);
	}

	private Permissions getPluginPermissions(Group group)
	{
		Sid sid = SidUtils.createSid(group);
		return getPluginPermissions(sid);
	}

	private Permissions getPluginPermissions(Sid sid)
	{
		List<Plugin> plugins = getPlugins();

		List<ObjectIdentity> pluginIdentities = plugins.stream().map(PluginIdentity::new).collect(toList());
		Map<ObjectIdentity, Acl> aclMap = mutableAclService.readAclsById(pluginIdentities, singletonList(sid));

		return toPluginPermissions(plugins, aclMap, sid);
	}

	private Permissions toPluginPermissions(List<Plugin> plugins, Map<ObjectIdentity, Acl> aclMap, Sid sid)
	{
		Permissions permissions = new Permissions();

		// set permissions: entity ids
		Map<String, String> pluginMap = plugins.stream().collect(toMap(Plugin::getId, Plugin::getId, (u, v) ->
		{
			throw new IllegalStateException(format("Duplicate key %s", u));
		}, LinkedHashMap::new));
		permissions.setEntityIds(pluginMap);

		// set permissions: user of group id
		boolean isUser = sid instanceof PrincipalSid;
		if (isUser)
		{
			String userId = ((PrincipalSid) sid).getPrincipal();
			permissions.setUserId(userId);
		}
		else
		{
			String groupId = ((GrantedAuthoritySid) sid).getGrantedAuthority().substring("ROLE_".length());
			permissions.setGroupId(groupId);
		}

		// set permissions: permissions
		aclMap.forEach((objectIdentity, acl) ->
		{
			String pluginId = objectIdentity.getIdentifier().toString();
			acl.getEntries().forEach(ace ->
			{
				if (ace.getSid().equals(sid))
				{
					Permission pluginPermission = toPluginPermission(ace);
					if (isUser)
					{
						permissions.addUserPermission(pluginId, pluginPermission);
					}
					else
					{
						permissions.addGroupPermission(pluginId, pluginPermission);
					}
				}
			});
		});
		return permissions;
	}

	private Permission toEntityTypePermission(AccessControlEntry ace)
	{
		Permission entityTypePermission = new Permission();
		if (ace.getPermission()
			   .equals(EntityTypePermissionUtils.getCumulativePermission(EntityTypePermission.WRITEMETA)))
		{
			entityTypePermission.setType("writemeta");
		}
		else if (ace.getPermission()
					.equals(EntityTypePermissionUtils.getCumulativePermission(EntityTypePermission.WRITE)))
		{
			entityTypePermission.setType("write");
		}
		else if (ace.getPermission()
					.equals(EntityTypePermissionUtils.getCumulativePermission(EntityTypePermission.READ)))
		{
			entityTypePermission.setType("read");
		}
		else if (ace.getPermission()
					.equals(EntityTypePermissionUtils.getCumulativePermission(EntityTypePermission.COUNT)))
		{
			entityTypePermission.setType("count");
		}
		else
		{
			throw new IllegalArgumentException(format("Illegal permission '%s'", ace.getPermission()));
		}
		return entityTypePermission;
	}

	private Permission toPluginPermission(AccessControlEntry ace)
	{
		Permission pluginPermission = new Permission();
		if (ace.getPermission().equals(PluginPermissionUtils.getCumulativePermission(PluginPermission.WRITEMETA)))
		{
			pluginPermission.setType("writemeta");
		}
		else if (ace.getPermission().equals(PluginPermissionUtils.getCumulativePermission(PluginPermission.WRITE)))
		{
			pluginPermission.setType("write");
		}
		else if (ace.getPermission().equals(PluginPermissionUtils.getCumulativePermission(PluginPermission.READ)))
		{
			pluginPermission.setType("read");
		}
		else if (ace.getPermission().equals(PluginPermissionUtils.getCumulativePermission(PluginPermission.COUNT)))
		{
			pluginPermission.setType("count");
		}
		else
		{
			throw new IllegalArgumentException(format("Illegal permission '%s'", ace.getPermission()));
		}
		return pluginPermission;
	}

	private Permissions toEntityTypePermissions(List<EntityType> entityTypes, Map<ObjectIdentity, Acl> aclMap, Sid sid)
	{
		Permissions permissions = new Permissions();

		// set permissions: entity ids
		Map<String, String> entityTypeMap = entityTypes.stream()
													   .collect(toMap(EntityType::getId, EntityType::getId, (u, v) ->
													   {
														   throw new IllegalStateException(
																   format("Duplicate key %s", u));
													   }, LinkedHashMap::new));
		permissions.setEntityIds(entityTypeMap);

		// set permissions: user of group id
		boolean isUser = sid instanceof PrincipalSid;
		if (isUser)
		{
			String userId = ((PrincipalSid) sid).getPrincipal();
			permissions.setUserId(userId);
		}
		else
		{
			String groupId = ((GrantedAuthoritySid) sid).getGrantedAuthority().substring("ROLE_".length());
			permissions.setGroupId(groupId);
		}

		// set permissions: permissions
		aclMap.forEach((objectIdentity, acl) ->
		{
			String entityTypeId = objectIdentity.getIdentifier().toString();
			acl.getEntries().forEach(ace ->
			{
				if (ace.getSid().equals(sid))
				{
					Permission entityTypePermission = toEntityTypePermission(ace);
					if (isUser)
					{
						permissions.addUserPermission(entityTypeId, entityTypePermission);
					}
					else
					{
						permissions.addGroupPermission(entityTypeId, entityTypePermission);
					}
				}
			});
		});
		return permissions;
	}

	private Permissions getEntityTypePermissions(User user)
	{
		Sid sid = SidUtils.createSid(user);
		return getEntityTypePermissions(sid);
	}

	private Permissions getEntityTypePermissions(Group group)
	{
		Sid sid = SidUtils.createSid(group);
		return getEntityTypePermissions(sid);
	}

	private Permissions getEntityTypePermissions(Sid sid)
	{
		List<EntityType> entityTypes = getEntityTypes().collect(toList());

		List<ObjectIdentity> objectIdentities = entityTypes.stream().map(EntityTypeIdentity::new).collect(toList());
		Map<ObjectIdentity, Acl> aclMap = mutableAclService.readAclsById(objectIdentities, singletonList(sid));

		return toEntityTypePermissions(entityTypes, aclMap, sid);
	}

	private Map<String, EntityTypePermission> toEntityTypePermissions(List<? extends Authority> entityTypeAuthorities)
	{
		Map<String, EntityTypePermission> entityTypePermissionMap = new HashMap<>();
		entityTypeAuthorities.forEach(authority ->
		{
			String role = authority.getRole();
			String pluginId = getAuthorityEntityId(role, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
			String pluginPermissionStr = getAuthorityType(role, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
			entityTypePermissionMap.put(pluginId, toEntityTypePermission(pluginPermissionStr));
		});
		return entityTypePermissionMap;
	}

	private static EntityTypePermission toEntityTypePermission(String paramValue)
	{
		switch (paramValue.toUpperCase())
		{
			case "READ":
				return EntityTypePermission.READ;
			case "WRITE":
				return EntityTypePermission.WRITE;
			case "COUNT":
				return EntityTypePermission.COUNT;
			case "WRITEMETA":
				return EntityTypePermission.WRITEMETA;
			default:
				throw new IllegalArgumentException(format("Unknown entity type permission '%s'", paramValue));
		}
	}

	private Map<String, PluginPermission> toPluginPermissions(List<? extends Authority> pluginAuthorities)
	{
		Map<String, PluginPermission> pluginPermissionMap = new HashMap<>();
		pluginAuthorities.forEach(authority ->
		{
			String role = authority.getRole();
			String pluginId = getAuthorityEntityId(role, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
			String pluginPermissionStr = getAuthorityType(role, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
			pluginPermissionMap.put(pluginId, toPluginPermission(pluginPermissionStr));
		});
		return pluginPermissionMap;
	}

	private static PluginPermission toPluginPermission(String paramValue)
	{
		switch (paramValue.toUpperCase())
		{
			case "READ":
				return PluginPermission.READ;
			case "WRITE":
				return PluginPermission.WRITE;
			case "COUNT":
				return PluginPermission.COUNT;
			case "WRITEMETA":
				return PluginPermission.WRITEMETA;
			default:
				throw new IllegalArgumentException(format("Unknown plugin permission '%s'", paramValue));
		}
	}

	private void removeSidPluginPermissions(Sid sid)
	{
		List<ObjectIdentity> objectIdentities = getPlugins().stream().map(PluginIdentity::new).collect(toList());
		removeAclSidEntries(sid, objectIdentities);
	}

	private void removeAclSidEntries(Sid sid, List<ObjectIdentity> objectIdentities)
	{
		Map<ObjectIdentity, MutableAcl> acls = (Map<ObjectIdentity, MutableAcl>) (Map<?, ?>) mutableAclService.readAclsById(
				objectIdentities, singletonList(sid));
		acls.forEach(((objectIdentity, acl) ->
		{
			boolean aclUpdated = false;
			int nrEntries = acl.getEntries().size();
			for (int i = nrEntries - 1; i >= 0; i--)
			{
				AccessControlEntry accessControlEntry = acl.getEntries().get(i);
				if (accessControlEntry.getSid().equals(sid))
				{
					acl.deleteAce(i);
					aclUpdated = true;
				}
			}
			if (aclUpdated)
			{
				mutableAclService.updateAcl(acl);
			}
		}));
	}

	private void replaceSidPluginPermissions(Sid sid, Map<String, PluginPermission> pluginPermissions)
	{
		List<ObjectIdentity> objectIdentities = pluginPermissions.keySet()
																 .stream()
																 .map(PluginIdentity::new)
																 .collect(toList());
		Map<ObjectIdentity, MutableAcl> acls = (Map<ObjectIdentity, MutableAcl>) (Map<?, ?>) mutableAclService.readAclsById(
				objectIdentities, singletonList(sid));
		acls.forEach(((objectIdentity, acl) ->
		{
			PluginPermission pluginPermission = pluginPermissions.get(objectIdentity.getIdentifier().toString());
			CumulativePermission cumulativePermission = PluginPermissionUtils.getCumulativePermission(pluginPermission);

			acl.insertAce(acl.getEntries().size(), cumulativePermission, sid, true);
			mutableAclService.updateAcl(acl);
		}));
	}

	private void removeSidEntityTypePermissions(Sid sid)
	{
		List<ObjectIdentity> objectIdentities = getEntityTypes().map(EntityTypeIdentity::new).collect(toList());
		removeAclSidEntries(sid, objectIdentities);
	}

	private void replaceSidEntityTypePermissions(Sid sid, Map<String, EntityTypePermission> entityTypePermissions)
	{
		List<ObjectIdentity> objectIdentities = entityTypePermissions.keySet()
																	 .stream()
																	 .map(EntityTypeIdentity::new)
																	 .collect(toList());
		Map<ObjectIdentity, MutableAcl> acls = (Map<ObjectIdentity, MutableAcl>) (Map<?, ?>) mutableAclService.readAclsById(
				objectIdentities, singletonList(sid));
		acls.forEach(((objectIdentity, acl) ->
		{
			EntityTypePermission entityTypePermission = entityTypePermissions.get(
					objectIdentity.getIdentifier().toString());
			CumulativePermission cumulativePermission = EntityTypePermissionUtils.getCumulativePermission(
					entityTypePermission);

			acl.insertAce(0, cumulativePermission, sid, true);
			mutableAclService.updateAcl(acl);
		}));
	}

	private String getAuthorityEntityId(String role, String authorityPrefix)
	{
		role = role.substring(authorityPrefix.length());
		return role.substring(role.indexOf('_') + 1);
	}

	private String getAuthorityType(String role, String authorityPrefix)
	{
		role = role.substring(authorityPrefix.length());
		return role.substring(0, role.indexOf('_'));
	}
}
