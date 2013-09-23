package org.molgenis.security.permission;

import static org.molgenis.security.permission.PermissionManagerController.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class PermissionManagerController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(PermissionManagerController.class);

	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "permissionmanager";

	private final PermissionManagerService pluginPermissionManagerService;

	@Autowired
	public PermissionManagerController(PermissionManagerService pluginPermissionManagerService)
	{
		super(URI);
		if (pluginPermissionManagerService == null) throw new IllegalArgumentException(
				"PluginPermissionManagerService is null");
		this.pluginPermissionManagerService = pluginPermissionManagerService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws DatabaseException
	{
		model.addAttribute("users", Lists.newArrayList(Iterables.filter(pluginPermissionManagerService.getUsers(),
				new Predicate<MolgenisUser>()
				{
					@Override
					public boolean apply(MolgenisUser molgenisUser)
					{
						Boolean superuser = molgenisUser.getSuperuser();
						return superuser == null || !superuser;
					}
				})));
		model.addAttribute("groups", pluginPermissionManagerService.getGroups());
		return "view-permissionmanager";
	}

	@RequestMapping(value = "/plugin/group/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getGroupPluginPermissions(@PathVariable Integer groupId) throws DatabaseException
	{
		List<GroupAuthority> authorities = pluginPermissionManagerService.getGroupPluginPermissions(groupId);
		Permissions permissions = createPermissions(authorities, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
		permissions.setGroupId(groupId);
		permissions.sort();
		return permissions;
	}

	@RequestMapping(value = "/entityclass/group/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getGroupEntityClassPermissions(@PathVariable Integer groupId) throws DatabaseException
	{
		List<GroupAuthority> authorities = pluginPermissionManagerService.getGroupEntityClassPermissions(groupId);
		Permissions permissions = createPermissions(authorities, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
		permissions.setGroupId(groupId);
		permissions.sort();
		return permissions;
	}

	@RequestMapping(value = "/plugin/user/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getUserPluginPermissions(@PathVariable Integer userId) throws DatabaseException
	{
		List<? extends Authority> authorities = pluginPermissionManagerService.getUserPluginPermissions(userId);
		Permissions permissions = createPermissions(authorities, SecurityUtils.AUTHORITY_PLUGIN_PREFIX);
		permissions.setUserId(userId);
		permissions.sort();
		return permissions;
	}

	@RequestMapping(value = "/entityclass/user/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getUserEntityClassPermissions(@PathVariable Integer userId) throws DatabaseException
	{
		List<? extends Authority> authorities = pluginPermissionManagerService.getUserEntityClassPermissions(userId);
		Permissions permissions = createPermissions(authorities, SecurityUtils.AUTHORITY_ENTITY_PREFIX);
		permissions.setUserId(userId);
		permissions.sort();
		return permissions;
	}

	@RequestMapping(value = "/update/plugin/group", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupPluginPermissions(@RequestParam Integer groupId, WebRequest webRequest)
			throws DatabaseException
	{
		List<GroupAuthority> authorities = new ArrayList<GroupAuthority>();
		for (MolgenisPlugin plugin : pluginPermissionManagerService.getPlugins())
		{
			String param = "radio-" + plugin.getId();
			String value = webRequest.getParameter(param);
			if (value.equals(MolgenisPermissionService.Permission.READ.toString())
					|| value.equals(MolgenisPermissionService.Permission.WRITE.toString()))
			{
				GroupAuthority authority = new GroupAuthority();
				authority.setRole(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + value.toUpperCase() + "_"
						+ plugin.getId().toUpperCase());
				System.out.println(authority.getRole());
				authorities.add(authority);
			}
		}
		pluginPermissionManagerService.replaceGroupPluginPermissions(authorities, groupId);
	}

	@RequestMapping(value = "/update/entityclass/group", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupEntityClassPermissions(@RequestParam Integer groupId, WebRequest webRequest)
			throws DatabaseException
	{
		List<GroupAuthority> authorities = new ArrayList<GroupAuthority>();
		for (String entityClassId : pluginPermissionManagerService.getEntityClassIds())
		{
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value.equals(MolgenisPermissionService.Permission.READ.toString())
					|| value.equals(MolgenisPermissionService.Permission.WRITE.toString()))
			{
				GroupAuthority authority = new GroupAuthority();
				authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX + value.toUpperCase() + "_"
						+ entityClassId.toUpperCase());
				authorities.add(authority);
			}
		}
		pluginPermissionManagerService.replaceGroupEntityClassPermissions(authorities, groupId);
	}

	@RequestMapping(value = "/update/plugin/user", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateUserPluginPermissions(@RequestParam Integer userId, WebRequest webRequest)
			throws DatabaseException
	{
		List<UserAuthority> authorities = new ArrayList<UserAuthority>();
		for (MolgenisPlugin plugin : pluginPermissionManagerService.getPlugins())
		{
			String param = "radio-" + plugin.getId();
			String value = webRequest.getParameter(param);
			if (value.equals(MolgenisPermissionService.Permission.READ.toString())
					|| value.equals(MolgenisPermissionService.Permission.WRITE.toString()))
			{
				UserAuthority authority = new UserAuthority();
				authority.setRole(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + value.toUpperCase() + "_"
						+ plugin.getId().toUpperCase());
				authorities.add(authority);
			}
		}
		pluginPermissionManagerService.replaceUserPluginPermissions(authorities, userId);
	}

	@RequestMapping(value = "/update/entityclass/user", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateUserEntityClassPermissions(@RequestParam Integer userId, WebRequest webRequest)
			throws DatabaseException
	{
		List<UserAuthority> authorities = new ArrayList<UserAuthority>();
		for (String entityClassId : pluginPermissionManagerService.getEntityClassIds())
		{
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value.equals(MolgenisPermissionService.Permission.READ.toString())
					|| value.equals(MolgenisPermissionService.Permission.WRITE.toString()))
			{
				UserAuthority authority = new UserAuthority();
				authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX + value.toUpperCase() + "_"
						+ entityClassId.toUpperCase());
				authorities.add(authority);
			}
		}
		pluginPermissionManagerService.replaceUserEntityClassPermissions(authorities, userId);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());
	}

	private Permissions createPermissions(List<? extends Authority> entityAuthorities, String authorityPrefix)
			throws DatabaseException
	{
		Permissions permissions = new Permissions();
		if (authorityPrefix.equals(SecurityUtils.AUTHORITY_PLUGIN_PREFIX))
		{
			List<MolgenisPlugin> plugins = pluginPermissionManagerService.getPlugins();
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
			List<String> entityClassIds = pluginPermissionManagerService.getEntityClassIds();
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
			Permission permission = new Permission();

			String authorityPluginId = getAuthorityEntityId(authority, authorityPrefix);
			String authorityType = getAuthorityType(authority, authorityPrefix);
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
		return permissions;
	}

	private String getAuthorityEntityId(Authority authority, String authorityPrefix)
	{
		String role = authority.getRole().substring(authorityPrefix.length());
		return role.substring(role.indexOf('_') + 1).toLowerCase();
	}

	private String getAuthorityType(Authority authority, String authorityPrefix)
	{
		String role = authority.getRole().substring(authorityPrefix.length());
		return role.substring(0, role.indexOf('_')).toLowerCase();
	}

	public static class Permissions
	{
		private Integer userId;
		private Integer groupId;
		private Map<String, String> entityIds;
		private Map<String, List<Permission>> userPermissionMap;
		private Map<String, List<Permission>> groupPermissionMap;

		public Integer getUserId()
		{
			return userId;
		}

		public void setUserId(Integer userId)
		{
			this.userId = userId;
		}

		public Integer getGroupId()
		{
			return groupId;
		}

		public void setGroupId(Integer groupId)
		{
			this.groupId = groupId;
		}

		public Map<String, String> getEntityIds()
		{
			return entityIds;
		}

		public void setEntityIds(Map<String, String> entityIds)
		{
			this.entityIds = entityIds;
		}

		public Map<String, List<Permission>> getUserPermissions()
		{
			return userPermissionMap != null ? userPermissionMap : Collections.<String, List<Permission>> emptyMap();
		}

		public void addUserPermission(String pluginId, Permission pluginPermission)
		{
			if (userPermissionMap == null) userPermissionMap = new HashMap<String, List<Permission>>();
			List<Permission> pluginPermissions = userPermissionMap.get(pluginId);
			if (pluginPermissions == null)
			{
				pluginPermissions = new ArrayList<Permission>();
				userPermissionMap.put(pluginId, pluginPermissions);
			}
			pluginPermissions.add(pluginPermission);
		}

		public Map<String, List<Permission>> getGroupPermissions()
		{
			return groupPermissionMap != null ? groupPermissionMap : Collections.<String, List<Permission>> emptyMap();
		}

		public void addGroupPermission(String pluginId, Permission pluginPermission)
		{
			if (groupPermissionMap == null) groupPermissionMap = new HashMap<String, List<Permission>>();
			List<Permission> pluginPermissions = groupPermissionMap.get(pluginId);
			if (pluginPermissions == null)
			{
				pluginPermissions = new ArrayList<Permission>();
				groupPermissionMap.put(pluginId, pluginPermissions);
			}
			pluginPermissions.add(pluginPermission);
		}

		public void sort()
		{
			if (userPermissionMap != null)
			{
				for (List<Permission> pluginPermissions : userPermissionMap.values())
				{
					if (pluginPermissions.size() > 1)
					{
						Collections.sort(pluginPermissions, new Comparator<Permission>()
						{
							@Override
							public int compare(Permission o1, Permission o2)
							{
								String group1 = o1.getGroup();
								String group2 = o2.getGroup();
								if (group1 == null && group2 == null) return 0;
								else if (group1 != null && group2 == null) return 1;
								else if (group1 == null && group2 != null) return -1;
								else return group1.compareTo(group2);
							}
						});
					}
				}
			}
			if (groupPermissionMap != null)
			{
				for (List<Permission> pluginPermissions : groupPermissionMap.values())
				{
					if (pluginPermissions.size() > 1)
					{
						Collections.sort(pluginPermissions, new Comparator<Permission>()
						{
							@Override
							public int compare(Permission o1, Permission o2)
							{
								String group1 = o1.getGroup();
								String group2 = o2.getGroup();
								if (group1 == null && group2 == null) return 0;
								else if (group1 != null && group2 == null) return 1;
								else if (group1 == null && group2 != null) return -1;
								else return group1.compareTo(group2);
							}
						});
					}
				}
			}
		}
	}

	public static class Permission
	{
		private String type;
		private String group;

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getGroup()
		{
			return group;
		}

		public void setGroup(String group)
		{
			this.group = group;
		}
	}
}
