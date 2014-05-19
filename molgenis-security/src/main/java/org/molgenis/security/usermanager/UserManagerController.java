package org.molgenis.security.usermanager;

import static org.molgenis.security.usermanager.UserManagerController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping(URI)
@SessionAttributes("viewState")
// either users or groups
public class UserManagerController extends MolgenisPluginController
{
	public final static String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "usermanager";
	private final UserManagerService pluginUserManagerService;

	@Autowired
	public UserManagerController(UserManagerService pluginUserManagerService)
	{
		super(URI);
		if (pluginUserManagerService == null)
		{
			throw new IllegalArgumentException("PluginUserManagerService is null");
		}
		this.pluginUserManagerService = pluginUserManagerService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute("users", this.pluginUserManagerService.getAllMolgenisUsers());
		model.addAttribute("groups", this.pluginUserManagerService.getAllMolgenisGroups());

		if (!model.containsAttribute("viewState")) model.addAttribute("viewState", "users");

		return "view-usermanager";
	}

	@RequestMapping(value = "/setViewState/{viewState}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void setViewState(@PathVariable String viewState, Model model)
	{
		model.addAttribute("viewState", viewState);
	}

	@RequestMapping(value = "/setActivation/{type}/{id}/{active}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void setActivation(@PathVariable String type, @PathVariable Integer id, @PathVariable Boolean active)
	{
		if ("user".equals(type))
		{
			this.pluginUserManagerService.setActivationUser(id, active);
		}
		else if ("group".equals(type))
		{
			this.pluginUserManagerService.setActivationGroup(id, active);
		}
		else throw new RuntimeException(
				"Trying to deactivate entity. Type may only be 'user' or 'group', however, value is: " + type);
	}

	@RequestMapping(value = "/changeGroupMembership/{userId}/{groupId}/{member}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void changeGroupMembership(@PathVariable Integer userId, @PathVariable Integer groupId,
			@PathVariable Boolean member)
	{
		if (member) this.pluginUserManagerService.addUserToGroup(groupId, userId);
		if (!member) this.pluginUserManagerService.removeUserFromGroup(groupId, userId);
	}
}
