package org.molgenis.core.ui.admin.permission;

import com.google.common.collect.Lists;
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
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.acl.SidUtils;
import org.molgenis.security.permission.Permissions;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.core.ui.admin.permission.PermissionManagerController.URI;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.data.plugin.model.PluginPermissionUtils.getCumulativePermission;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserMetaData.USER;

@Controller
@RequestMapping(URI)
public class PermissionManagerController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(PermissionManagerController.class);

	public static final String URI = PluginController.PLUGIN_URI_PREFIX + "permissionmanager";

	private final DataService dataService;
	private final MutableAclService mutableAclService;

	public PermissionManagerController(DataService dataService, MutableAclService mutableAclService)
	{
		super(URI);
		this.dataService = requireNonNull(dataService);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@GetMapping
	public String init(Model model)
	{
		model.addAttribute("users", Lists.newArrayList(getUsers().stream().filter(user ->
		{
			Boolean superuser = user.isSuperuser();
			return superuser == null || !superuser;
		}).collect(Collectors.toList())));
		model.addAttribute("groups", getGroups());
		return "view-permissionmanager";
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	@GetMapping("/entityclass/group/{groupId}")
	@ResponseBody
	public Permissions getGroupEntityClassPermissions(@PathVariable String groupId)
	{
		Sid sid = getSidForGroupId(groupId);
		return getEntityTypePermissions(sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	@GetMapping("/plugin/user/{userId}")
	@ResponseBody
	public Permissions getUserPluginPermissions(@PathVariable String userId)
	{
		Sid sid = getSidForUserId(userId);
		return getPluginPermissions(sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	@GetMapping("/entityclass/user/{userId}")
	@ResponseBody
	public Permissions getUserEntityClassPermissions(@PathVariable String userId)
	{
		Sid sid = getSidForUserId(userId);
		return getEntityTypePermissions(sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/plugin/group")
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupPluginPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		Sid sid = getSidForGroupId(groupId);
		updatePluginPermission(webRequest, sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/entityclass/group")
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupEntityClassPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		Sid sid = getSidForGroupId(groupId);
		updateEntityTypePermissions(webRequest, sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/plugin/user")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserPluginPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		Sid sid = getSidForUserId(userId);
		updatePluginPermission(webRequest, sid);
	}

	private void removeSidPluginPermission(Plugin plugin, Sid sid)
	{
		ObjectIdentity objectIdentity = new PluginIdentity(plugin);
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));

		boolean aclUpdated = deleteAceIfExists(sid, acl);
		if (aclUpdated)
		{
			mutableAclService.updateAcl(acl);
		}
	}

	private void createSidPluginPermission(Plugin plugin, Sid sid, PluginPermission pluginPermission)
	{
		ObjectIdentity objectIdentity = new PluginIdentity(plugin);
		createSidPermission(sid, objectIdentity, getCumulativePermission(pluginPermission));
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	@GetMapping("/plugin/group/{groupId}")
	@ResponseBody
	public Permissions getGroupPluginPermissions(@PathVariable String groupId)
	{
		Sid sid = getSidForGroupId(groupId);
		return getPluginPermissions(sid);
	}

	/**
	 * package-private for testability
	 */
	List<Plugin> getPlugins()
	{
		return dataService.findAll(PLUGIN, Plugin.class).collect(toList());
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/entityclass/user")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserEntityClassPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		Sid sid = getSidForUserId(userId);
		updateEntityTypePermissions(webRequest, sid);
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

	private void updatePluginPermission(WebRequest webRequest, Sid sid)
	{
		for (Plugin plugin : getPlugins())
		{
			String param = "radio-" + plugin.getId();
			String value = webRequest.getParameter(param);
			if (value != null)
			{
				if (!value.equals("none"))
				{
					createSidPluginPermission(plugin, sid, toPluginPermission(value));
				}
				else
				{
					removeSidPluginPermission(plugin, sid);
				}
			}
		}
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
		boolean isUser = setUserOrGroup(sid, permissions);

		// set permissions: permissions
		aclMap.forEach((objectIdentity, acl) ->
		{
			String pluginId = objectIdentity.getIdentifier().toString();
			acl.getEntries().forEach(ace ->
			{
				if (ace.getSid().equals(sid))
				{
					org.molgenis.security.permission.Permission pluginPermission = toPluginPermission(ace);
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

	private org.molgenis.security.permission.Permission toPluginPermission(AccessControlEntry ace)
	{
		org.molgenis.security.permission.Permission pluginPermission = new org.molgenis.security.permission.Permission();
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

	private void removeSidEntityTypePermission(EntityType entityType, Sid sid)
	{
		ObjectIdentity objectIdentity = new EntityTypeIdentity(entityType);
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));

		boolean aclUpdated = deleteAceIfExists(sid, acl);
		if (aclUpdated)
		{
			mutableAclService.updateAcl(acl);
		}
	}

	private void createSidEntityTypePermission(EntityType entityType, Sid sid,
			EntityTypePermission entityTypePermission)
	{
		ObjectIdentity objectIdentity = new EntityTypeIdentity(entityType);
		createSidPermission(sid, objectIdentity,
				EntityTypePermissionUtils.getCumulativePermission(entityTypePermission));
	}

	private void updateEntityTypePermissions(WebRequest webRequest, Sid sid)
	{
		getEntityTypes().forEach(entityType ->
		{
			String param = "radio-" + entityType.getId();
			String value = webRequest.getParameter(param);
			if (value != null)
			{
				if (!value.equals("none"))
				{
					createSidEntityTypePermission(entityType, sid, toEntityTypePermission(value));
				}
				else
				{
					removeSidEntityTypePermission(entityType, sid);
				}
			}
		});
	}

	private Permissions getEntityTypePermissions(Sid sid)
	{
		List<EntityType> entityTypes = getEntityTypes().collect(toList());

		List<ObjectIdentity> objectIdentities = entityTypes.stream().map(EntityTypeIdentity::new).collect(toList());
		Map<ObjectIdentity, Acl> aclMap = mutableAclService.readAclsById(objectIdentities, singletonList(sid));

		return toEntityTypePermissions(entityTypes, aclMap, sid);
	}

	private org.molgenis.security.permission.Permission toEntityTypePermission(AccessControlEntry ace)
	{
		org.molgenis.security.permission.Permission entityTypePermission = new org.molgenis.security.permission.Permission();
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

		boolean isUser = setUserOrGroup(sid, permissions);

		// set permissions: permissions
		aclMap.forEach((objectIdentity, acl) ->
		{
			String entityTypeId = objectIdentity.getIdentifier().toString();
			acl.getEntries().forEach(ace ->
			{
				if (ace.getSid().equals(sid))
				{
					org.molgenis.security.permission.Permission entityTypePermission = toEntityTypePermission(ace);
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

	private void createSidPermission(Sid sid, ObjectIdentity objectIdentity, CumulativePermission cumulativePermission)
	{
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));

		deleteAceIfExists(sid, acl);
		acl.insertAce(0, cumulativePermission, sid, true);
		mutableAclService.updateAcl(acl);
	}

	private Sid getSidForUserId(String userId)
	{
		User user = getUser(userId);
		return SidUtils.createSid(user);
	}

	private Sid getSidForGroupId(String groupId)
	{
		Group group = getGroup(groupId);
		return SidUtils.createSid(group);
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

	/**
	 * package-private for testability
	 */
	List<User> getUsers()
	{
		return dataService.findAll(USER, User.class).collect(toList());
	}

	/**
	 * package-private for testability
	 */
	List<Group> getGroups()
	{
		return dataService.findAll(GROUP, Group.class).collect(toList());
	}

	private boolean setUserOrGroup(Sid sid, Permissions permissions)
	{
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
		return isUser;
	}

	private boolean deleteAceIfExists(Sid sid, MutableAcl acl)
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
		return aclUpdated;
	}

	private Stream<EntityType> getEntityTypes()
	{
		return dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		LOG.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}