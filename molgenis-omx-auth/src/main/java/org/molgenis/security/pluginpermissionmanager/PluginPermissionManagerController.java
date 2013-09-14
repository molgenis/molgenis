package org.molgenis.security.pluginpermissionmanager;

import static org.molgenis.security.pluginpermissionmanager.PluginPermissionManagerController.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
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
		model.addAttribute("groups", pluginPermissionManagerService.getGroups());
		return "view-pluginpermissionmanager";
	}

	@RequestMapping(value = "/group/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public PluginPermissions getGroupPermissions(@PathVariable Integer groupId) throws DatabaseException
	{
		List<GroupAuthority> authorities = pluginPermissionManagerService.getGroupPluginPermissions(groupId);
		PluginPermissions pluginPermissions = createPluginPermissions(authorities);
		pluginPermissions.setGroupId(groupId);
		pluginPermissions.sort();
		return pluginPermissions;
	}

	@RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public PluginPermissions getUserPermissions(@PathVariable Integer userId) throws DatabaseException
	{
		List<? extends Authority> authorities = pluginPermissionManagerService.getUserPluginPermissions(userId);

		PluginPermissions pluginPermissions = createPluginPermissions(authorities);
		pluginPermissions.setUserId(userId);
		pluginPermissions.sort();
		return pluginPermissions;
	}

	@RequestMapping(value = "/update/group", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateGroupPluginPermissions(@RequestParam Integer groupId, WebRequest webRequest)
			throws DatabaseException
	{
		List<GroupAuthority> authorities = new ArrayList<GroupAuthority>();
		for (String pluginId : MolgenisPluginRegistry.getInstance().getPluginIds())
		{
			String param = "radio-" + pluginId;
			String value = webRequest.getParameter(param);
			if (value.equals("read") || value.equals("write"))
			{
				GroupAuthority authority = new GroupAuthority();
				authority.setRole("ROLE_PLUGIN_" + pluginId.toUpperCase() + "_" + value.toUpperCase() + "_USER");
				authorities.add(authority);
			}
			else if (!value.equals("none"))
			{
				throw new RuntimeException("Invalid value for paramater " + param + " value [" + value + "]");
			}
		}
		pluginPermissionManagerService.replaceGroupPluginPermissions(authorities, groupId);
	}

	@RequestMapping(value = "/update/user", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateUserPluginPermissions(@RequestParam Integer userId, WebRequest webRequest)
			throws DatabaseException
	{
		List<UserAuthority> authorities = new ArrayList<UserAuthority>();
		for (String pluginId : MolgenisPluginRegistry.getInstance().getPluginIds())
		{
			String param = "radio-" + pluginId;
			String value = webRequest.getParameter(param);
			if (value.equals("read") || value.equals("write"))
			{
				UserAuthority authority = new UserAuthority();
				authority.setRole("ROLE_PLUGIN_" + pluginId.toUpperCase() + "_" + value.toUpperCase() + "_USER");
				authorities.add(authority);
			}
			else if (!value.equals("none"))
			{
				throw new RuntimeException("Invalid value for paramater " + param + " value [" + value + "]");
			}
		}
		pluginPermissionManagerService.replaceUserPluginPermissions(authorities, userId);
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

	private PluginPermissions createPluginPermissions(List<? extends Authority> pluginAuthorities)
	{
		PluginPermissions pluginPermissions = new PluginPermissions();
		pluginPermissions.setPluginIds(new ArrayList<String>(MolgenisPluginRegistry.getInstance().getPluginIds()));
		for (Authority authority : pluginAuthorities)
		{
			PluginPermission pluginPermission = new PluginPermission();

			String authorityPluginId = getAuthorityPluginId(authority);
			String authorityType = getAuthorityType(authority);
			pluginPermission.setType(authorityType);
			if (authority instanceof GroupAuthority)
			{
				pluginPermission.setGroup(((GroupAuthority) authority).getMolgenisGroup().getName());
				pluginPermissions.addGroupPluginPermission(authorityPluginId, pluginPermission);
			}
			else
			{
				pluginPermissions.addUserPluginPermission(authorityPluginId, pluginPermission);
			}

		}
		return pluginPermissions;
	}

	private String getAuthorityPluginId(Authority authority)
	{
		String role = authority.getRole().substring(SecurityUtils.PLUGIN_AUTHORITY_PREFIX.length());
		role = role.substring(0, role.length() - "_USER".length());
		return role.substring(0, role.indexOf('_')).toLowerCase();
	}

	private String getAuthorityType(Authority authority)
	{
		String role = authority.getRole().substring(SecurityUtils.PLUGIN_AUTHORITY_PREFIX.length());
		role = role.substring(0, role.length() - "_USER".length());
		return role.substring(role.indexOf('_') + 1).toLowerCase();
	}

	public static class PluginPermissions
	{
		private Integer userId;
		private Integer groupId;
		private List<String> pluginIds;
		private Map<String, List<PluginPermission>> userPluginPermissionMap;
		private Map<String, List<PluginPermission>> groupPluginPermissionMap;

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

		public List<String> getPluginIds()
		{
			return pluginIds;
		}

		public void setPluginIds(List<String> pluginIds)
		{
			this.pluginIds = pluginIds;
		}

		public Map<String, List<PluginPermission>> getUserPluginPermissions()
		{
			return userPluginPermissionMap != null ? userPluginPermissionMap : Collections
					.<String, List<PluginPermission>> emptyMap();
		}

		public void addUserPluginPermission(String pluginId, PluginPermission pluginPermission)
		{
			if (userPluginPermissionMap == null) userPluginPermissionMap = new HashMap<String, List<PluginPermission>>();
			List<PluginPermission> pluginPermissions = userPluginPermissionMap.get(pluginId);
			if (pluginPermissions == null)
			{
				pluginPermissions = new ArrayList<PluginPermission>();
				userPluginPermissionMap.put(pluginId, pluginPermissions);
			}
			pluginPermissions.add(pluginPermission);
		}

		public Map<String, List<PluginPermission>> getGroupPluginPermissions()
		{
			return groupPluginPermissionMap != null ? groupPluginPermissionMap : Collections
					.<String, List<PluginPermission>> emptyMap();
		}

		public void addGroupPluginPermission(String pluginId, PluginPermission pluginPermission)
		{
			if (groupPluginPermissionMap == null) groupPluginPermissionMap = new HashMap<String, List<PluginPermission>>();
			List<PluginPermission> pluginPermissions = groupPluginPermissionMap.get(pluginId);
			if (pluginPermissions == null)
			{
				pluginPermissions = new ArrayList<PluginPermission>();
				groupPluginPermissionMap.put(pluginId, pluginPermissions);
			}
			pluginPermissions.add(pluginPermission);
		}

		public void sort()
		{
			Collections.sort(pluginIds);
			if (userPluginPermissionMap != null)
			{
				for (List<PluginPermission> pluginPermissions : userPluginPermissionMap.values())
				{
					if (pluginPermissions.size() > 1)
					{
						Collections.sort(pluginPermissions, new Comparator<PluginPermission>()
						{
							@Override
							public int compare(PluginPermission o1, PluginPermission o2)
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
			if (groupPluginPermissionMap != null)
			{
				for (List<PluginPermission> pluginPermissions : groupPluginPermissionMap.values())
				{
					if (pluginPermissions.size() > 1)
					{
						Collections.sort(pluginPermissions, new Comparator<PluginPermission>()
						{
							@Override
							public int compare(PluginPermission o1, PluginPermission o2)
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

	public static class PluginPermission
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
