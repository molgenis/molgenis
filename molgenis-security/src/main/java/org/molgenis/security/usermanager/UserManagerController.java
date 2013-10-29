package org.molgenis.security.usermanager;

import static org.molgenis.security.usermanager.UserManagerController.URI;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class UserManagerController extends MolgenisPluginController
{
	private final Logger logger = Logger.getLogger(UserManagerController.class);
	public final static String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "usermanager";
	private final UserManagerService pluginUserManagerService;

	private Integer selectedUserId = new Integer(-1);
	private Integer selectedGroupId = new Integer(-1);

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
		return refreshUserManagerView(Integer.valueOf("-1"), Integer.valueOf("-1"), model);
	}

	@RequestMapping(method = RequestMethod.POST)
	public String updateView(
			@RequestParam Integer userId, 
			@RequestParam(value = "groupId", required=false) Integer groupId, 
			Model model) throws DatabaseException
	{
		return refreshUserManagerView(userId, groupId, model);
	}
	
	@RequestMapping(value = "/users/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public List<MolgenisUserViewData> getUsersMemberInGroup(@PathVariable Integer groupId, Model model) throws DatabaseException
	{
		this.selectedGroupId = groupId;
		model.addAttribute("group_selected_id", this.selectedGroupId);
		
		if(isValidId(this.selectedGroupId)){
			return this.pluginUserManagerService.getUsersMemberInGroup(groupId);
		}else{
			return new ArrayList<MolgenisUserViewData>();
		}
	}

	@RequestMapping(value = "/addusertogroup/{groupToAddId}", method = RequestMethod.GET)
	@ResponseBody
	public String addUserToGroup(@PathVariable Integer groupToAddId) throws DatabaseException
	{
		if (isValidId(this.selectedUserId) && isValidId(groupToAddId))
		{
			this.pluginUserManagerService.addUserToGroup(groupToAddId, this.selectedUserId);
		} 
		return "";
	}
	
	@RequestMapping(value = "/removeuserfromgroup/{groupToRemoveId}", method = RequestMethod.GET)
	@ResponseBody
	public String removeUserFromGroup(@PathVariable Integer groupToRemoveId) throws DatabaseException
	{
		if (isValidId(this.selectedUserId) && isValidId(groupToRemoveId))
		{
			this.pluginUserManagerService.removeUserFromGroup(groupToRemoveId, this.selectedUserId);
		}
		return "";
	}
	
	private boolean isValidId(Integer id){
		return null != id && !Integer.valueOf("-1").equals(id);
	}
	
	private String refreshUserManagerView(Integer userId, Integer groupId, Model model) throws DatabaseException{
		
		if(null != userId){
			this.selectedUserId = userId;
			model.addAttribute("user_selected_id", this.selectedUserId);
		}
		
		if(null != groupId){
			this.selectedGroupId = groupId;
			model.addAttribute("group_selected_id", this.selectedGroupId);
		}

		if (isValidId(this.selectedUserId))
		{
			model.addAttribute("groupsWhereUserIsMember",
					this.pluginUserManagerService.getGroupsWhereUserIsMember(this.selectedUserId));
			model.addAttribute("groupsWhereUserIsNotMember",
					this.pluginUserManagerService.getGroupsWhereUserIsNotMember(this.selectedUserId));
		}
		
		model.addAttribute("users", this.pluginUserManagerService.getAllMolgenisUsers());
		model.addAttribute("groups", this.pluginUserManagerService.getAllMolgenisGroups());

		return "view-usermanager";
	}
}
