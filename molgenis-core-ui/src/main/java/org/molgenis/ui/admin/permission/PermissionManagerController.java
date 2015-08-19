package org.molgenis.ui.admin.permission;

import static org.molgenis.ui.admin.permission.PermissionManagerController.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionManagerService;
import org.molgenis.security.permission.Permissions;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger LOG = LoggerFactory.getLogger(PermissionManagerController.class);

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
	public String init(Model model)
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
	public Permissions getGroupPluginPermissions(@PathVariable String groupId)
	{
		return pluginPermissionManagerService.getGroupPluginPermissions(groupId);
	}

	@RequestMapping(value = "/entityclass/group/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getGroupEntityClassPermissions(@PathVariable String groupId)
	{
		return pluginPermissionManagerService.getGroupEntityClassPermissions(groupId);
	}

	@RequestMapping(value = "/plugin/user/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getUserPluginPermissions(@PathVariable String userId)
	{
		return pluginPermissionManagerService.getUserPluginPermissions(userId);
	}

	@RequestMapping(value = "/entityclass/user/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getUserEntityClassPermissions(@PathVariable String userId)
	{
		return pluginPermissionManagerService.getUserEntityClassPermissions(userId);
	}

	@RequestMapping(value = "/update/plugin/group", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupPluginPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		List<GroupAuthority> authorities = new ArrayList<GroupAuthority>();
		for (MolgenisPlugin plugin : pluginPermissionManagerService.getPlugins())
		{
			String param = "radio-" + plugin.getId();
			String value = webRequest.getParameter(param);

			if (value.equalsIgnoreCase(Permission.READ.toString())
					|| value.equalsIgnoreCase(Permission.COUNT.toString())
					|| value.equalsIgnoreCase(Permission.WRITE.toString()))
			{
				GroupAuthority authority = new GroupAuthority();
				authority.setRole(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + value.toUpperCase() + "_"
						+ plugin.getId().toUpperCase());
				authorities.add(authority);
			}
		}
		pluginPermissionManagerService.replaceGroupPluginPermissions(authorities, groupId);
	}

	@RequestMapping(value = "/update/entityclass/group", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupEntityClassPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		List<GroupAuthority> authorities = new ArrayList<GroupAuthority>();
		for (String entityClassId : pluginPermissionManagerService.getEntityClassIds())
		{
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value.equalsIgnoreCase(Permission.READ.toString())
					|| value.equalsIgnoreCase(Permission.COUNT.toString())
					|| value.equalsIgnoreCase(Permission.WRITE.toString()))
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
	public void updateUserPluginPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		List<UserAuthority> authorities = new ArrayList<UserAuthority>();
		for (MolgenisPlugin plugin : pluginPermissionManagerService.getPlugins())
		{
			String param = "radio-" + plugin.getId();
			String value = webRequest.getParameter(param);
			if (value.equalsIgnoreCase(Permission.READ.toString())
					|| value.equalsIgnoreCase(Permission.COUNT.toString())
					|| value.equalsIgnoreCase(Permission.WRITE.toString()))
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
	public void updateUserEntityClassPermissions(@RequestParam String userId, WebRequest webRequest)
	{
		List<UserAuthority> authorities = new ArrayList<UserAuthority>();
		for (String entityClassId : pluginPermissionManagerService.getEntityClassIds())
		{
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value.equalsIgnoreCase(Permission.READ.toString())
					|| value.equalsIgnoreCase(Permission.COUNT.toString())
					|| value.equalsIgnoreCase(Permission.WRITE.toString()))
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
		LOG.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}
