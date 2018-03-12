package org.molgenis.core.ui.admin.permission;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.*;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.SidUtils;
import org.molgenis.security.permission.Permissions;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.core.ui.admin.permission.PermissionManagerController.URI;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.data.security.auth.UserMetaData.USERNAME;
import static org.molgenis.security.acl.SidUtils.createAnonymousSid;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;

@Controller
@RequestMapping(URI)
public class PermissionManagerController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(PermissionManagerController.class);

	public static final String URI = PluginController.PLUGIN_URI_PREFIX + "permissionmanager";

	private final DataService dataService;
	private final MutableAclService mutableAclService;
	private final MutableAclClassService mutableAclClassService;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;

	PermissionManagerController(DataService dataService, MutableAclService mutableAclService,
			MutableAclClassService mutableAclClassService, SystemEntityTypeRegistry systemEntityTypeRegistry)
	{
		super(URI);
		this.dataService = requireNonNull(dataService);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.mutableAclClassService = requireNonNull(mutableAclClassService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
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
		model.addAttribute("entityTypes", getEntityTypeDtos());
		return "view-permissionmanager";
	}

	private List<EntityTypeRlsResponse> getEntityTypeDtos()
	{
		List<EntityType> entityTypes = getEntityTypes().filter(entityType -> !entityType.isAbstract())
													   .collect(toList());
		Collection<String> aclClasses = mutableAclClassService.getAclClassTypes();
		entityTypes.sort(comparing(EntityType::getLabel));
		return entityTypes.stream().map(entityType ->
		{
			boolean rlsEnabled = aclClasses.contains(EntityIdentityUtils.toType(entityType));
			boolean readOnly = systemEntityTypeRegistry.hasSystemEntityType(entityType.getId());
			return new EntityTypeRlsResponse(entityType.getId(), entityType.getLabel(), rlsEnabled, readOnly);
		}).collect(toList());
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	@GetMapping("/repository/group/{groupId}")
	@ResponseBody
	public PermissionTable getGroupRepositoryPermissions(@PathVariable String groupId)
	{
		Sid sid = getSidForGroupId(groupId);
		return getRepositoryPermissions(sid);
	}

	public static class PermissionTable
	{
		private final List<PermissionRow> permissions;

		public PermissionTable(List<PermissionRow> permissions)
		{

			this.permissions = permissions;
		}

		public List<PermissionRow> getPermissions()
		{
			return permissions;
		}
	}

	public static class PermissionRow
	{
		private final Resource resource;
		private final int mask;

		public PermissionRow(Resource resource, int mask)
		{
			this.resource = resource;
			this.mask = mask;
		}

		public Resource getResource()
		{
			return resource;
		}

		public int getMask()
		{
			return mask;
		}
	}

	public static class Resource
	{
		public String id;
		public String label;
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
	@GetMapping("/package/user/{userId}")
	@ResponseBody
	public Permissions getUserPackagePermissions(@PathVariable String userId)
	{
		Sid sid = getSidForUserId(userId);
		return getPackagePermissions(sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	@GetMapping("/package/group/{groupId}")
	@ResponseBody
	public Permissions getGroupPackagePermissions(@PathVariable String groupId)
	{
		Sid sid = getSidForGroupId(groupId);
		return getPackagePermissions(sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	@GetMapping("/repository/user/{userId}")
	@ResponseBody
	public PermissionTable getUserRepositoryPermissions(@PathVariable String userId)
	{
		Sid sid = getSidForUserId(userId);
		return getRepositoryPermissions(sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/plugin/group")
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupPluginPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		Sid sid = getSidForGroupId(groupId);
		updatePluginPermissions(webRequest, sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/repository/{repositoryId}/group/{groupId}/{mask}")
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupRepositoryPermissions(@PathVariable String repositoryId, @PathVariable String groupId,
			@PathVariable int mask)
	{
		Sid sid = getSidForGroupId(groupId);
		updateRepositoryPermissions(repositoryId, sid, mask);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/package/group")
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupPackagePermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		Sid sid = getSidForGroupId(groupId);
		updatePackagePermissions(webRequest, sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/package/user")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserPackagePermissions(@RequestParam String userId, WebRequest webRequest)
	{
		Sid sid = getSidForUserId(userId);
		updatePackagePermissions(webRequest, sid);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/plugin/user")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserPluginPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		Sid sid = getSidForUserId(userId);
		updatePluginPermissions(webRequest, sid);
	}

	private void removeSidPluginPermission(Plugin plugin, Sid sid)
	{
		ObjectIdentity objectIdentity = new PluginIdentity(plugin);
		removePermissionForSid(sid, objectIdentity);
	}

	private void createSidPluginPermission(Plugin plugin, Sid sid, PluginPermission pluginPermission)
	{
		ObjectIdentity objectIdentity = new PluginIdentity(plugin);
		createSidPermission(sid, objectIdentity, pluginPermission);
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
	@PostMapping("/repository/{repositoryId}/user/{userId}/{mask}")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserRepositoryPermissions(@PathVariable String repositoryId, @PathVariable String userId,
			@PathVariable int mask)
	{
		Sid sid = getSidForUserId(userId);
		updateRepositoryPermissions(repositoryId, sid, mask);
	}

	private void updateRepositoryPermissions(String repositoryId, Sid sid, int mask)
	{
		Repository<Entity> repository = dataService.getRepository(repositoryId);

		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new RepositoryIdentity(repository),
				singletonList(sid));
		List<AccessControlEntry> accessControlEntries = acl.getEntries();

		// update existing entry if exists
		boolean aclUpdated = false;
		for (int i = 0; i < accessControlEntries.size(); ++i)
		{
			AccessControlEntry accessControlEntry = accessControlEntries.get(i);
			if (accessControlEntry.getSid().equals(sid))
			{
				acl.updateAce(i, new RepositoryPermission(mask));
				aclUpdated = true;
				break;
			}
		}

		// otherwise create new entry
		if (!aclUpdated)
		{
			acl.insertAce(acl.getEntries().size(), new RepositoryPermission(mask), sid, true);
		}
		mutableAclService.updateAcl(acl);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	@PostMapping("/update/entityclass/rls")
	@ResponseStatus(HttpStatus.OK)
	public void updateEntityClassRls(@Valid @RequestBody EntityTypeRlsRequest entityTypeRlsRequest)
	{
		String entityTypeId = entityTypeRlsRequest.getId();
		if (systemEntityTypeRegistry.hasSystemEntityType(entityTypeId))
		{
			throw new IllegalArgumentException("Updating system entity type not allowed");
		}

		EntityType entityType = dataService.getEntityType(entityTypeId);
		String aclClassType = EntityIdentityUtils.toType(entityType);
		boolean hasAclClass = mutableAclClassService.hasAclClass(aclClassType);
		if (entityTypeRlsRequest.isRlsEnabled())
		{
			if (!hasAclClass)
			{
				mutableAclClassService.createAclClass(aclClassType, EntityIdentityUtils.toIdType(entityType));
				dataService.findAll(entityType.getId())
						   .forEach(entity -> mutableAclService.createAcl(new EntityIdentity(entity)));
			}
		}
		else
		{
			if (hasAclClass)
			{
				mutableAclClassService.deleteAclClass(aclClassType);
			}
		}
	}

	private static PluginPermission toPluginPermission(String paramValue)
	{
		switch (paramValue.toUpperCase())
		{
			case "READ":
				return PluginPermission.READ;
			default:
				throw new IllegalArgumentException(format("Unknown plugin permission '%s'", paramValue));
		}
	}

	private void updatePluginPermissions(WebRequest webRequest, Sid sid)
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

	private void updatePackagePermissions(WebRequest webRequest, Sid sid)
	{
		for (Package package_ : getPackages())
		{
			String param = "radio-" + package_.getId();
			String value = webRequest.getParameter(param);
			if (value != null)
			{
				if (!value.equals("none"))
				{
					createSidPackagePermission(package_, sid, toRepositoryPermission(value));
				}
				else
				{
					removeSidPackagePermission(package_, sid);
				}
			}
		}
	}

	private Permissions getPackagePermissions(Sid sid)
	{
		List<Package> packages = getPackages();
		List<ObjectIdentity> packageIdentities = packages.stream().map(PackageIdentity::new).collect(toList());
		Map<ObjectIdentity, Acl> aclMap = mutableAclService.readAclsById(packageIdentities, singletonList(sid));

		return toPackagePermissions(packages, aclMap, sid);

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
		if (ace.getPermission().equals(PluginPermission.READ))
		{
			pluginPermission.setType("read");
		}
		else
		{
			throw new IllegalArgumentException(format("Illegal permission '%s'", ace.getPermission()));
		}
		return pluginPermission;
	}

	private void removeSidEntityTypePermission(EntityType entityType, Sid sid)
	{
		ObjectIdentity objectIdentity = new RepositoryIdentity(entityType);
		removePermissionForSid(sid, objectIdentity);
	}

	private void createSidEntityTypePermission(EntityType entityType, Sid sid,
			RepositoryPermission entityTypePermission)
	{
		ObjectIdentity objectIdentity = new RepositoryIdentity(entityType);
		createSidPermission(sid, objectIdentity,
				RepositoryPermissionUtils.getCumulativePermission(entityTypePermission));
	}

	private void createSidPackagePermission(Package package_, Sid sid, RepositoryPermission entityTypePermission)
	{
		ObjectIdentity objectIdentity = new PackageIdentity(package_);
		createSidPermission(sid, objectIdentity,
				RepositoryPermissionUtils.getCumulativePermission(entityTypePermission));
	}

	private void removeSidPackagePermission(Package package_, Sid sid)
	{
		ObjectIdentity objectIdentity = new PackageIdentity(package_);
		removePermissionForSid(sid, objectIdentity);
	}

	private void removePermissionForSid(Sid sid, ObjectIdentity objectIdentity)
	{
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));

		boolean aclUpdated = deleteAceIfExists(sid, acl);
		if (aclUpdated)
		{
			mutableAclService.updateAcl(acl);
		}
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
					createSidEntityTypePermission(entityType, sid, toRepositoryPermission(value));
				}
				else
				{
					removeSidEntityTypePermission(entityType, sid);
				}
			}
		});
	}

	private PermissionTable getRepositoryPermissions(Sid sid)
	{
		List<EntityType> entityTypes = dataService.query(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class)
												  .eq(EntityTypeMetadata.IS_ABSTRACT, false)
												  .sort(new Sort(EntityTypeMetadata.LABEL))
												  .findAll()
												  .collect(toList());

		List<ObjectIdentity> objectIdentities = entityTypes.stream().map(RepositoryIdentity::new).collect(toList());
		Map<ObjectIdentity, Acl> aclMap = mutableAclService.readAclsById(objectIdentities, singletonList(sid));

		List<PermissionRow> permissionRows = new ArrayList<>();
		entityTypes.forEach(entityType ->
		{
			Resource repository = new Resource();
			repository.id = entityType.getId();
			repository.label = entityType.getLabel();

			Acl acl = aclMap.get(new RepositoryIdentity(entityType));
			int mask = 0;
			for (AccessControlEntry ace : acl.getEntries())
			{
				if (ace.getSid().equals(sid))
				{
					mask = ace.getPermission().getMask();
					break;
				}

			}
			permissionRows.add(new PermissionRow(repository, mask));
		});
		return new PermissionTable(permissionRows);
	}

	private org.molgenis.security.permission.Permission toRepositoryPermission(AccessControlEntry ace)
	{
		org.molgenis.security.permission.Permission entityTypePermission = new org.molgenis.security.permission.Permission();
		switch (ace.getPermission().getMask())
		{
			case 1: // count (1)
				entityTypePermission.setType("count");
				break;
			case 3: // count (1) + read (2) --> 3
				entityTypePermission.setType("read");
				break;
			case 7: // count (1) + read (2) + write (4) --> 7
				entityTypePermission.setType("write");
				break;
			case 15: // count (1) + read (2) + write (4) + writemeta (8) --> 7
				entityTypePermission.setType("writemeta");
				break;
			case 19: // count (1) + read (2) + create (16) --> 19
				entityTypePermission.setType("create");
				break;
			default:
				throw new IllegalArgumentException(format("Illegal permission '%s'", ace.getPermission()));
		}
		return entityTypePermission;
	}

	private Permissions toRepositoryPermissions(List<EntityType> entityTypes, Map<ObjectIdentity, Acl> aclMap, Sid sid)
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

		return toRepositoryPermissions(aclMap, sid, permissions);
	}

	private Permissions toPackagePermissions(List<Package> packages, Map<ObjectIdentity, Acl> aclMap, Sid sid)
	{
		Permissions permissions = new Permissions();

		// set permissions: entity ids
		Map<String, String> entityTypeMap = packages.stream().collect(toMap(Package::getId, Package::getId, (u, v) ->
		{
			throw new IllegalStateException(format("Duplicate key %s", u));
		}, LinkedHashMap::new));

		permissions.setEntityIds(entityTypeMap);

		return toRepositoryPermissions(aclMap, sid, permissions);
	}

	private Permissions toRepositoryPermissions(Map<ObjectIdentity, Acl> aclMap, Sid sid, Permissions permissions)
	{
		boolean isUser = setUserOrGroup(sid, permissions);

		// set permissions: permissions
		aclMap.forEach((objectIdentity, acl) ->
		{
			String entityTypeId = objectIdentity.getIdentifier().toString();
			acl.getEntries().forEach(ace ->
			{
				if (ace.getSid().equals(sid))
				{
					org.molgenis.security.permission.Permission entityTypePermission = toRepositoryPermission(ace);
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

	private static RepositoryPermission toRepositoryPermission(String paramValue)
	{
		switch (paramValue.toUpperCase())
		{
			case "READ":
				return RepositoryPermission.READ;
			case "WRITE":
				return RepositoryPermission.WRITE;
			case "COUNT":
				return RepositoryPermission.COUNT;
			case "WRITEMETA":
				return RepositoryPermission.WRITEMETA;
			default:
				throw new IllegalArgumentException(format("Unknown entity type permission '%s'", paramValue));
		}
	}

	private void createSidPermission(Sid sid, ObjectIdentity objectIdentity, Permission permission)
	{
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));

		deleteAceIfExists(sid, acl);
		acl.insertAce(0, permission, sid, true);
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

	List<Package> getPackages()
	{
		return dataService.findAll(PackageMetadata.PACKAGE, Package.class).collect(toList());
	}

	private boolean setUserOrGroup(Sid sid, Permissions permissions)
	{
		boolean isUser;
		if (sid instanceof PrincipalSid)
		{
			String userId = ((PrincipalSid) sid).getPrincipal();
			permissions.setUserId(userId);
			isUser = true;
		}
		else if (sid.equals(createAnonymousSid()))
		{
			String userId = dataService.query(USER, User.class).eq(USERNAME, ANONYMOUS_USERNAME).findOne().getId();
			permissions.setUserId(userId);
			isUser = true;
		}
		else
		{
			String groupId = ((GrantedAuthoritySid) sid).getGrantedAuthority().substring("ROLE_".length());
			permissions.setGroupId(groupId);
			isUser = false;
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