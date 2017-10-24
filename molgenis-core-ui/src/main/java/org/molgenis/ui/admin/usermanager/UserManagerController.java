package org.molgenis.ui.admin.usermanager;

import org.molgenis.security.core.service.UserService;
import org.molgenis.web.PluginController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static org.molgenis.ui.admin.usermanager.UserManagerController.URI;

@Controller
@RequestMapping(URI)
public class UserManagerController extends PluginController
{
	public final static String URI = PluginController.PLUGIN_URI_PREFIX + "usermanager";
	private final UserManagerService userManagerService;
	private final UserService userService;

	public UserManagerController(UserManagerService userManagerService, UserService userService)
	{
		super(URI);
		this.userManagerService = Objects.requireNonNull(userManagerService);
		this.userService = Objects.requireNonNull(userService);
	}

	@GetMapping
	public String init(Model model)
	{
		model.addAttribute("users", this.userManagerService.getAllUsers());
		return "view-usermanager";
	}

	@PutMapping("/activation")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody
	ActivationResponse setActive(@RequestParam String id, @RequestParam boolean active)
	{
		ActivationResponse activationResponse = new ActivationResponse();
		activationResponse.setId(id);
		if (active)
		{
			userService.activateUser(id);
		}
		else
		{
			userService.deactivateUser(id);
		}
		activationResponse.setSuccess(true);
		return activationResponse;
	}

	public class ActivationResponse
	{
		private boolean success = false;
		private String type;
		private String id;

		public boolean isSuccess()
		{
			return success;
		}

		public void setSuccess(boolean success)
		{
			this.success = success;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}
	}
}
