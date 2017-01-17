package org.molgenis.ui.admin.permission;

import com.google.common.collect.Lists;
import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.GroupAuthorityFactory;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.security.permission.PermissionManagerService;
import org.molgenis.security.permission.Permissions;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.Permission.*;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_PREFIX;
import static org.molgenis.ui.admin.permission.PermissionManagerController.URI;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class PermissionManagerController extends MolgenisPluginController
{
	private static final Logger LOG = getLogger(PermissionManagerController.class);

	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "permissionmanager";

	private final PermissionManagerService pluginPermissionManagerService;
	private final UserAuthorityFactory userAuthorityFactory;
	private final GroupAuthorityFactory groupAuthorityFactory;

	@Autowired
	public PermissionManagerController(PermissionManagerService pluginPermissionManagerService,
			UserAuthorityFactory userAuthorityFactory, GroupAuthorityFactory groupAuthorityFactory)
	{
		super(URI);

		this.pluginPermissionManagerService = requireNonNull(pluginPermissionManagerService);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("users", Lists.newArrayList(pluginPermissionManagerService.getUsers().stream().filter(user ->
		{
			Boolean superuser = user.isSuperuser();
			return superuser == null || !superuser;
		}).collect(Collectors.toList())));
		model.addAttribute("groups", pluginPermissionManagerService.getGroups());
		return "view-permissionmanager";
	}

	@RequestMapping(value = "/plugin/group/{groupId}", method = GET)
	@ResponseBody
	public Permissions getGroupPluginPermissions(@PathVariable String groupId)
	{
		return pluginPermissionManagerService.getGroupPluginPermissions(groupId);
	}

	@RequestMapping(value = "/entityclass/group/{groupId}", method = GET)
	@ResponseBody
	public Permissions getGroupEntityClassPermissions(@PathVariable String groupId)
	{
		return pluginPermissionManagerService.getGroupEntityClassPermissions(groupId);
	}

	@RequestMapping(value = "/plugin/user/{userId}", method = GET)
	@ResponseBody
	public Permissions getUserPluginPermissions(@PathVariable String userId)
	{
		return pluginPermissionManagerService.getUserPluginPermissions(userId);
	}

	@RequestMapping(value = "/entityclass/user/{userId}", method = GET)
	@ResponseBody
	public Permissions getUserEntityClassPermissions(@PathVariable String userId)
	{
		return pluginPermissionManagerService.getUserEntityClassPermissions(userId);
	}

	@RequestMapping(value = "/update/plugin/group", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupPluginPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		List<GroupAuthority> authorities = new ArrayList<>();
		for (MolgenisPlugin plugin : pluginPermissionManagerService.getPlugins())
		{
			String pluginName = plugin.getId();
			String value = webRequest.getParameter("radio-" + pluginName);

			if (value.equalsIgnoreCase(COUNT.toString()) || value.equalsIgnoreCase(READ.toString()) || value
					.equalsIgnoreCase(WRITE.toString()) || value.equalsIgnoreCase(WRITEMETA.toString()))
			{
				authorities.add(createGroupAuthority(
						AUTHORITY_PLUGIN_PREFIX + value.toUpperCase() + "_" + pluginName.toUpperCase()));

				Map<String, String> requiredSettingEntities = plugin.getRequiredSettingEntities();
				authorities.addAll(requiredSettingEntities.keySet().stream().map(settingEntity -> createGroupAuthority(
						requiredSettingEntities.get(settingEntity) + settingEntity.toUpperCase()))
						.collect(Collectors.toList()));
			}
		}
		pluginPermissionManagerService.replaceGroupPluginPermissions(authorities, groupId);
	}

	@RequestMapping(value = "/update/entityclass/group", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupEntityClassPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		List<GroupAuthority> authorities = new ArrayList<>();
		for (String entityClassId : pluginPermissionManagerService.getEntityClassIds())
		{
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value.equalsIgnoreCase(READ.toString()) || value.equalsIgnoreCase(COUNT.toString()) || value
					.equalsIgnoreCase(WRITE.toString()) || value.equalsIgnoreCase(WRITEMETA.toString()))
			{
				authorities.add(createGroupAuthority(
						AUTHORITY_ENTITY_PREFIX + value.toUpperCase() + "_" + entityClassId.toUpperCase()));
			}
		}
		pluginPermissionManagerService.replaceGroupEntityClassPermissions(authorities, groupId);
	}

	@RequestMapping(value = "/update/plugin/user", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateUserPluginPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		List<UserAuthority> authorities = new ArrayList<>();
		for (MolgenisPlugin plugin : pluginPermissionManagerService.getPlugins())
		{
			String param = "radio-" + plugin.getId();
			String value = webRequest.getParameter(param);
			if (value.equalsIgnoreCase(COUNT.toString()) || value.equalsIgnoreCase(READ.toString()) || value
					.equalsIgnoreCase(WRITE.toString()) || value.equalsIgnoreCase(WRITEMETA.toString()))
			{
				authorities.add(createUserAuthority(
						AUTHORITY_PLUGIN_PREFIX + value.toUpperCase() + "_" + plugin.getId().toUpperCase()));

				Map<String, String> requiredSettingEntities = plugin.getRequiredSettingEntities();
				authorities.addAll(requiredSettingEntities.keySet().stream().map(settingEntity -> createUserAuthority(
						requiredSettingEntities.get(settingEntity) + settingEntity.toUpperCase()))
						.collect(Collectors.toList()));
			}
		}
		pluginPermissionManagerService.replaceUserPluginPermissions(authorities, userId);
	}

	@RequestMapping(value = "/update/entityclass/user", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateUserEntityClassPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		List<UserAuthority> authorities = new ArrayList<>();
		for (String entityClassId : pluginPermissionManagerService.getEntityClassIds())
		{
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value.equalsIgnoreCase(READ.toString()) || value.equalsIgnoreCase(COUNT.toString()) || value
					.equalsIgnoreCase(WRITE.toString()) || value.equalsIgnoreCase(WRITEMETA.toString()))
			{
				authorities.add(createUserAuthority(
						AUTHORITY_ENTITY_PREFIX + value.toUpperCase() + "_" + entityClassId.toUpperCase()));
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
		return singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}

	private UserAuthority createUserAuthority(String role)
	{
		UserAuthority authority = userAuthorityFactory.create();
		authority.setRole(role);
		return authority;
	}

	private GroupAuthority createGroupAuthority(String role)
	{
		GroupAuthority authority = groupAuthorityFactory.create();
		authority.setRole(role);
		return authority;
	}
}