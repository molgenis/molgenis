package org.molgenis.security.usermanager;

import static org.molgenis.security.usermanager.UserManagerController.URI;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class UserManagerController extends MolgenisPluginController
{
	private final Logger logger = Logger.getLogger(UserManagerController.class);
	public final static String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "usermanager";
	private final UserManagerService pluginUserManagerService;

	private Integer selectedUserId = null;
	private Integer selectedGroupId = null;

	@Autowired
	public UserManagerController(UserManagerService pluginUserManagerService)
	{
		super(URI);
		if (pluginUserManagerService == null) throw new IllegalArgumentException("PluginUserManagerService is null");
		this.pluginUserManagerService = pluginUserManagerService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws DatabaseException
	{
		model.addAttribute("users", this.pluginUserManagerService.getAllMolgenisUsers());
		model.addAttribute("user_selected_id", (null == this.selectedUserId ? -1 : this.selectedUserId));
		model.addAttribute("groups", this.pluginUserManagerService.getAllMolgenisGroups());
		model.addAttribute("group_selected_id", (null == this.selectedGroupId ? -1 : this.selectedGroupId));
		return "view-usermanager";
	}

	@RequestMapping(value = "/users/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public List<MolgenisUser> getUsersMemberingGroup(@PathVariable
	Integer groupId) throws DatabaseException
	{
		return this.pluginUserManagerService.getUsersMemberInGroup(groupId);
	}

	@RequestMapping(method = RequestMethod.POST)
	public String updateView(HttpServletRequest request, Model model) throws DatabaseException
	{
		logger.info("first post !!!!!!!!!");
		
		String userIdParam = request.getParameter("userId");
		String groupIdParam = request.getParameter("groupId");
		String groupToAddId = request.getParameter("groupToAddId");

		logger.info("groupToAddId: " + groupToAddId);
		
		try
		{
			this.selectedUserId = Integer.parseInt(userIdParam);
			this.selectedGroupId = Integer.parseInt(groupIdParam);
		}
		catch (NumberFormatException e)
		{
			this.selectedUserId = null;
			this.selectedGroupId = null;
			logger.error("not a number");
		}

		model.addAttribute("user_selected_id", (null == this.selectedUserId ? -1 : this.selectedUserId));
		model.addAttribute("group_selected_id", (null == this.selectedGroupId ? -1 : this.selectedGroupId));

		if (null != this.selectedUserId)
		{
			model.addAttribute("users", this.pluginUserManagerService.getAllMolgenisUsers());
			model.addAttribute("groupsWhereUserIsMember",
					this.pluginUserManagerService.getGroupsWhereUserIsMember(this.selectedUserId));
			model.addAttribute("groupsWhereUserIsNotMember",
					this.pluginUserManagerService.getGroupsWhereUserIsNotMember(this.selectedUserId));
			model.addAttribute("groups", this.pluginUserManagerService.getAllMolgenisGroups());
		}

		return "view-usermanager";
	}
//	
	@RequestMapping(value = "/addgroup/", method = RequestMethod.POST)
	public String addGroup(HttpServletRequest request, Model model) throws DatabaseException
	{
		logger.info("second post !!!!!!!!!");
		
		String userIdParam = request.getParameter("userId");
		String groupIdParam = request.getParameter("groupId");
		Integer groupToAddId = Integer.parseInt(request.getParameter("groupToAddId"));

		if (null != groupToAddId && null != this.selectedUserId)
		{
			this.pluginUserManagerService.addGroup(groupToAddId, selectedUserId);
			logger.info("groupToAddId: " + groupToAddId);
		}

		try
		{
			this.selectedUserId = Integer.parseInt(userIdParam);
			this.selectedGroupId = Integer.parseInt(groupIdParam);
		}
		catch (NumberFormatException e)
		{
			this.selectedUserId = null;
			this.selectedGroupId = null;
			logger.error("not a number");
		}

		model.addAttribute("user_selected_id", (null == this.selectedUserId ? -1 : this.selectedUserId));
		model.addAttribute("group_selected_id", (null == this.selectedGroupId ? -1 : this.selectedGroupId));

		if (null != this.selectedUserId)
		{
			model.addAttribute("users", this.pluginUserManagerService.getAllMolgenisUsers());
			model.addAttribute("groupsWhereUserIsMember",
					this.pluginUserManagerService.getGroupsWhereUserIsMember(this.selectedUserId));
			model.addAttribute("groupsWhereUserIsNotMember",
					this.pluginUserManagerService.getGroupsWhereUserIsNotMember(this.selectedUserId));
			model.addAttribute("groups", this.pluginUserManagerService.getAllMolgenisGroups());
		}

		return "view-usermanager";
	}
}
