package org.molgenis.core.ui.admin.permission;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.security.auth.GroupAuthority;
import org.molgenis.data.security.auth.GroupAuthorityFactory;
import org.molgenis.data.security.auth.UserAuthority;
import org.molgenis.data.security.auth.UserAuthorityFactory;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionManagerService;
import org.molgenis.security.permission.Permissions;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.admin.permission.PermissionManagerController.URI;

@Controller
@RequestMapping(URI)
public class PermissionManagerController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(PermissionManagerController.class);

	public static final String URI = PluginController.PLUGIN_URI_PREFIX + "permissionmanager";

	private final PermissionManagerService pluginPermissionManagerService;
	private final UserAuthorityFactory userAuthorityFactory;
	private final GroupAuthorityFactory groupAuthorityFactory;

	public PermissionManagerController(PermissionManagerService pluginPermissionManagerService,
			UserAuthorityFactory userAuthorityFactory, GroupAuthorityFactory groupAuthorityFactory)
	{
		super(URI);
		this.pluginPermissionManagerService = requireNonNull(pluginPermissionManagerService);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
	}

	@GetMapping
	public String init(Model model)
	{
		model.addAttribute("users",
				Lists.newArrayList(Iterables.filter(pluginPermissionManagerService.getUsers(), user ->
				{
					Boolean superuser = user.isSuperuser();
					return superuser == null || !superuser;
				})));
		model.addAttribute("groups", pluginPermissionManagerService.getGroups());
		return "view-permissionmanager";
	}

	@GetMapping("/plugin/group/{groupId}")
	@ResponseBody
	public Permissions getGroupPluginPermissions(@PathVariable String groupId)
	{
		return pluginPermissionManagerService.getGroupPluginPermissions(groupId);
	}

	@GetMapping("/entityclass/group/{groupId}")
	@ResponseBody
	public Permissions getGroupEntityClassPermissions(@PathVariable String groupId)
	{
		return pluginPermissionManagerService.getGroupEntityClassPermissions(groupId);
	}

	@GetMapping("/plugin/user/{userId}")
	@ResponseBody
	public Permissions getUserPluginPermissions(@PathVariable String userId)
	{
		return pluginPermissionManagerService.getUserPluginPermissions(userId);
	}

	@GetMapping("/entityclass/user/{userId}")
	@ResponseBody
	public Permissions getUserEntityClassPermissions(@PathVariable String userId)
	{
		return pluginPermissionManagerService.getUserEntityClassPermissions(userId);
	}

	@PostMapping("/update/plugin/group")
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupPluginPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		List<GroupAuthority> authorities = new ArrayList<>();
		for (Plugin plugin : pluginPermissionManagerService.getPlugins())
		{
			String param = "radio-" + plugin.getId();
			String value = webRequest.getParameter(param);

			if (value.equalsIgnoreCase(Permission.READ.toString()) || value.equalsIgnoreCase(
					Permission.COUNT.toString()) || value.equalsIgnoreCase(Permission.WRITE.toString())
					|| value.equalsIgnoreCase(Permission.WRITEMETA.toString()))
			{
				GroupAuthority authority = groupAuthorityFactory.create();
				authority.setRole(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + value.toUpperCase() + "_" + plugin.getId());
				authorities.add(authority);
			}
		}
		pluginPermissionManagerService.replaceGroupPluginPermissions(authorities, groupId);
	}

	@PostMapping("/update/entityclass/group")
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupEntityClassPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		List<GroupAuthority> authorities = new ArrayList<>();
		for (Object entityClassId : pluginPermissionManagerService.getEntityClassIds())
		{
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value.equalsIgnoreCase(Permission.READ.toString()) || value.equalsIgnoreCase(
					Permission.COUNT.toString()) || value.equalsIgnoreCase(Permission.WRITE.toString())
					|| value.equalsIgnoreCase(Permission.WRITEMETA.toString()))
			{
				GroupAuthority authority = groupAuthorityFactory.create();
				authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX + value.toUpperCase() + "_" + entityClassId);
				authorities.add(authority);
			}
		}
		pluginPermissionManagerService.replaceGroupEntityClassPermissions(authorities, groupId);
	}

	@PostMapping("/update/plugin/user")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserPluginPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		List<UserAuthority> authorities = new ArrayList<>();
		for (Plugin plugin : pluginPermissionManagerService.getPlugins())
		{
			String param = "radio-" + plugin.getId();
			String value = webRequest.getParameter(param);
			if (value != null && (value.equalsIgnoreCase(Permission.READ.toString()) || value.equalsIgnoreCase(
					Permission.COUNT.toString()) || value.equalsIgnoreCase(Permission.WRITE.toString())
					|| value.equalsIgnoreCase(Permission.WRITEMETA.toString())))
			{
				UserAuthority authority = userAuthorityFactory.create();
				authority.setRole(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + value.toUpperCase() + "_" + plugin.getId());
				authorities.add(authority);
			}
		}
		pluginPermissionManagerService.replaceUserPluginPermissions(authorities, userId);
	}

	@PostMapping("/update/entityclass/user")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserEntityClassPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		List<UserAuthority> authorities = new ArrayList<>();
		for (Object entityClassId : pluginPermissionManagerService.getEntityClassIds())
		{
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value != null && (value.equalsIgnoreCase(Permission.READ.toString()) || value.equalsIgnoreCase(
					Permission.COUNT.toString()) || value.equalsIgnoreCase(Permission.WRITE.toString())
					|| value.equalsIgnoreCase(Permission.WRITEMETA.toString())))
			{
				UserAuthority authority = userAuthorityFactory.create();
				authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX + value.toUpperCase() + "_" + entityClassId);
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
		LOG.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}
