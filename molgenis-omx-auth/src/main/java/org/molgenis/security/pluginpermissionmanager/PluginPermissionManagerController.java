package org.molgenis.security.pluginpermissionmanager;

import static org.molgenis.security.pluginpermissionmanager.PluginPermissionManagerController.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.MolgenisUser;
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
public class PluginPermissionManagerController extends MolgenisPlugin
{
	private static final Logger logger = Logger.getLogger(PluginPermissionManagerController.class);

	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "pluginpermissionmanager";

	private final PluginPermissionManagerService pluginPermissionManagerService;

	@Autowired
	public PluginPermissionManagerController(PluginPermissionManagerService pluginPermissionManagerService)
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
		return "view-pluginpermissionmanager";
	}

	@RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public UserPluginPermissions getPermissions(@PathVariable Integer userId, Model model) throws DatabaseException
	{
		List<Authority> authorities = pluginPermissionManagerService.getPluginPermissions(userId);

		UserPluginPermissions userPluginPermissions = new UserPluginPermissions();
		userPluginPermissions.setUserId(userId);

		for (String pluginId : MolgenisPluginRegistry.getInstance().getPluginIds())
		{
			PluginPermission pluginPermission = new PluginPermission();
			boolean canRead = false;
			boolean canWrite = false;
			for (Authority authority : authorities)
			{
				String role = authority.getRole().substring(SecurityUtils.PLUGIN_AUTHORITY_PREFIX.length());
				// TODO use SecurityUtil constants
				if (role.endsWith("_USER"))
				{
					role = role.substring(0, role.length() - "_USER".length());
					if (role.indexOf('_') != -1 && !role.endsWith("_"))
					{
						String plugin = role.substring(0, role.indexOf('_'));
						if (plugin.toLowerCase().equals(pluginId))
						{
							String permission = role.substring(role.indexOf('_') + 1);
							if (permission.equals("READ")) canRead = true;
							if (permission.equals("WRITE")) canWrite = true;
						}
					}
				}
			}
			pluginPermission.setPluginId(pluginId);
			pluginPermission.setCanRead(canRead);
			pluginPermission.setCanWrite(canWrite);
			userPluginPermissions.addPermission(pluginPermission);
		}
		Collections.sort(userPluginPermissions.getPermissions(), new Comparator<PluginPermission>()
		{
			@Override
			public int compare(PluginPermission o1, PluginPermission o2)
			{
				return o1.getPluginId().compareTo(o2.getPluginId());
			}
		});
		return userPluginPermissions;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updatePluginPermissions(@RequestParam Integer userId, WebRequest webRequest) throws DatabaseException
	{
		List<Authority> authorities = new ArrayList<Authority>();
		for (String pluginId : MolgenisPluginRegistry.getInstance().getPluginIds())
		{
			String param = "radio-" + pluginId;
			String value = webRequest.getParameter(param);
			if (value.equals("read") || value.equals("write"))
			{
				Authority authority = new Authority();
				authority.setRole("ROLE_PLUGIN_" + pluginId.toUpperCase() + "_" + value.toUpperCase() + "_USER");
				authorities.add(authority);
			}
			else if (!value.equals("none"))
			{
				throw new RuntimeException("Invalid value for paramater " + param + " value [" + value + "]");
			}
		}
		pluginPermissionManagerService.updatePluginPermissions(authorities, userId);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(e);
		return Collections.singletonMap("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());
	}

	public static class UserPluginPermissions
	{
		private Integer userId;
		private List<PluginPermission> pluginPermissions;

		public Integer getUserId()
		{
			return userId;
		}

		public void setUserId(Integer userId)
		{
			this.userId = userId;
		}

		public List<PluginPermission> getPermissions()
		{
			return pluginPermissions != null ? pluginPermissions : Collections.<PluginPermission> emptyList();
		}

		public void addPermission(PluginPermission userPermission)
		{
			if (pluginPermissions == null) pluginPermissions = new ArrayList<PluginPermissionManagerController.PluginPermission>();
			this.pluginPermissions.add(userPermission);
		}
	}

	public static class PluginPermission
	{
		private String pluginId;
		private boolean canRead;
		private boolean canWrite;

		public String getPluginId()
		{
			return pluginId;
		}

		public void setPluginId(String id)
		{
			this.pluginId = id;
		}

		public boolean isCanRead()
		{
			return canRead;
		}

		public void setCanRead(boolean canRead)
		{
			this.canRead = canRead;
		}

		public boolean isCanWrite()
		{
			return canWrite;
		}

		public void setCanWrite(boolean canWrite)
		{
			this.canWrite = canWrite;
		}
	}
}
