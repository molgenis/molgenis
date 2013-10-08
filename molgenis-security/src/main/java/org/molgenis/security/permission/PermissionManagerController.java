package org.molgenis.security.permission;

import static org.molgenis.security.permission.PermissionManagerController.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginController;
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
		return pluginPermissionManagerService.getGroupPluginPermissions(groupId);
	}

	@RequestMapping(value = "/entityclass/group/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getGroupEntityClassPermissions(@PathVariable Integer groupId) throws DatabaseException
	{
		return pluginPermissionManagerService.getGroupEntityClassPermissions(groupId);
	}

	@RequestMapping(value = "/plugin/user/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getUserPluginPermissions(@PathVariable Integer userId) throws DatabaseException
	{
		return pluginPermissionManagerService.getUserPluginPermissions(userId);
	}

	@RequestMapping(value = "/entityclass/user/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getUserEntityClassPermissions(@PathVariable Integer userId) throws DatabaseException
	{
		return pluginPermissionManagerService.getUserEntityClassPermissions(userId);
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
			if (value.equalsIgnoreCase(MolgenisPermissionService.Permission.READ.toString())
					|| value.equalsIgnoreCase(MolgenisPermissionService.Permission.WRITE.toString()))
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
}
