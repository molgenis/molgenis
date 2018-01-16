package org.molgenis.core.ui.admin.usermanager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.molgenis.web.PluginController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static org.molgenis.core.ui.admin.usermanager.UserManagerController.URI;

@Api("User manager")
@Controller
@RequestMapping(URI)
@SessionAttributes("viewState")
// either users or groups
public class UserManagerController extends PluginController
{
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + "usermanager";
	private final UserManagerService pluginUserManagerService;

	public UserManagerController(UserManagerService pluginUserManagerService)
	{
		super(URI);
		if (pluginUserManagerService == null)
		{
			throw new IllegalArgumentException("PluginUserManagerService is null");
		}
		this.pluginUserManagerService = pluginUserManagerService;
	}

	@ApiOperation("Return user manager view")
	@ApiResponses({ @ApiResponse(code = 200, message = "Return the user manager view") })
	@GetMapping
	public String init(Model model)
	{
		model.addAttribute("users", this.pluginUserManagerService.getAllUsers());
		model.addAttribute("groups", this.pluginUserManagerService.getAllGroups());

		if (!model.containsAttribute("viewState")) model.addAttribute("viewState", "users");

		return "view-usermanager";
	}

	@ApiOperation("Sets viewState")
	@ApiResponses({ @ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 500, message = "ViewState could not be set") })
	@PutMapping("/setViewState/{viewState}")
	@ResponseStatus(HttpStatus.OK)
	public void setViewState(@PathVariable("viewState") String viewState, Model model)
	{
		model.addAttribute("viewState", viewState);
	}

	@ApiOperation("Sets activation status for a user")
	@ApiResponses({ @ApiResponse(code = 200, message = "Ok", response = ActivationResponse.class),
			@ApiResponse(code = 404, message = "If response doesn't have success set to true, the user wasn't found", response = ActivationResponse.class) })
	@PutMapping("/activation")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody
	ActivationResponse activation(@RequestBody Activation activation)
	{
		ActivationResponse activationResponse = new ActivationResponse();
		activationResponse.setId(activation.getId());
		activationResponse.setType(activation.getType());
		if ("user".equals(activation.getType()))
		{
			this.pluginUserManagerService.setActivationUser(activation.getId(), activation.getActive());
			activationResponse.setSuccess(true);
		}
		else if ("group".equals(activation.getType()))
		{
			this.pluginUserManagerService.setActivationGroup(activation.getId(), activation.getActive());
			activationResponse.setSuccess(true);
		}
		else throw new RuntimeException(
					"Trying to deactivate entity. Type may only be 'user' or 'group', however, value is: "
							+ activation.getType());

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

	public class Activation
	{
		private String type;
		private String id;
		private Boolean active;

		Activation(String type, String id, Boolean active)
		{
			this.id = id;
			this.type = type;
			this.active = active;
		}

		/**
		 * @return the type
		 */
		public String getType()
		{
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type)
		{
			this.type = type;
		}

		/**
		 * @return the id
		 */
		public String getId()
		{
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id)
		{
			this.id = id;
		}

		/**
		 * @return the active
		 */
		public Boolean getActive()
		{
			return active;
		}

		/**
		 * @param active the active to set
		 */
		public void setActive(Boolean active)
		{
			this.active = active;
		}
	}

	@ApiOperation("Change group membership")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Updated groupMemberShip", response = GroupMembershipResponse.class), })
	@PutMapping("/changeGroupMembership")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody
	GroupMembershipResponse changeGroupMembership(@RequestBody GroupMembership groupMembership)
	{
		GroupMembershipResponse groupMembershipResponse = new GroupMembershipResponse();
		groupMembershipResponse.setUserId(groupMembership.getUserId());

		if (null != groupMembership.getMember())
		{
			if (groupMembership.getMember())
			{
				this.pluginUserManagerService.addUserToGroup(groupMembership.getGroupId(), groupMembership.getUserId());
			}
			else if (!groupMembership.getMember())
			{
				this.pluginUserManagerService.removeUserFromGroup(groupMembership.getGroupId(),
						groupMembership.getUserId());
			}

			groupMembershipResponse.setSuccess(true);
		}

		return groupMembershipResponse;
	}

	public class GroupMembership
	{
		/**
		 * @return the userId
		 */
		public String getUserId()
		{
			return userId;
		}

		/**
		 * @param userId the userId to set
		 */
		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		/**
		 * @return the groupId
		 */
		public String getGroupId()
		{
			return groupId;
		}

		/**
		 * @param groupId the groupId to set
		 */
		public void setGroupId(String groupId)
		{
			this.groupId = groupId;
		}

		/**
		 * @return the member
		 */
		public Boolean getMember()
		{
			return member;
		}

		/**
		 * @param member the member to set
		 */
		public void setMember(Boolean member)
		{
			this.member = member;
		}

		String userId;
		String groupId;
		Boolean member;
	}

	public class GroupMembershipResponse
	{
		String userId;
		boolean success;

		public boolean isSuccess()
		{
			return success;
		}

		public void setSuccess(boolean success)
		{
			this.success = success;
		}

		/**
		 * @return the userId
		 */
		public String getUserId()
		{
			return userId;
		}

		/**
		 * @param userId the userId to set
		 */
		public void setUserId(String userId)
		{
			this.userId = userId;
		}
	}
}
