package org.molgenis.security.group;

import io.swagger.annotations.*;
import org.molgenis.data.security.auth.GroupService;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.RoleValue;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.GroupService.DEFAULT_ROLES;
import static org.molgenis.data.security.auth.GroupService.MANAGER;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@RestController
@Api("Group")
public class GroupRestController
{
	private final GroupValueFactory groupValueFactory;
	private final GroupService groupService;
	private final RoleMembershipService roleMembershipService;

	GroupRestController(GroupValueFactory groupValueFactory, GroupService groupService,
			RoleMembershipService roleMembershipService)
	{
		this.groupValueFactory = requireNonNull(groupValueFactory);
		this.groupService = requireNonNull(groupService);
		this.roleMembershipService = requireNonNull(roleMembershipService);
	}

	@PostMapping("api/plugin/group")
	@ApiOperation(value = "Create a new Group", response = String.class)
	@Transactional
	@ApiResponses({
			@ApiResponse(code = 200, message = "The name of the newly created group", response = String.class) })
	public String createGroup(
			@ApiParam("Alphanumeric name for the group") @RequestParam(value = "name") @Nullable String name,
			@ApiParam("Label for the group") @RequestParam("label") String label,
			@ApiParam("Description for the group") @RequestParam(value = "description", required = false) @Nullable String description,
			@ApiParam("Indication if this group should be publicly visible (not yet implemented!)") @RequestParam(value = "public", required = false, defaultValue = "true") boolean publiclyVisible)
	{
		GroupValue groupValue = groupValueFactory.createGroup(name, label, description, publiclyVisible,
				DEFAULT_ROLES.keySet());

		groupService.persist(groupValue);
		groupService.grantPermissions(groupValue);
		roleMembershipService.addUserToRole(getCurrentUsername(), getManagerRoleName(groupValue));

		return groupValue.getName();
	}

	private String getManagerRoleName(GroupValue groupValue)
	{
		return groupValue.getRoles()
						 .stream()
						 .filter(role -> role.getLabel().equals(MANAGER))
						 .map(RoleValue::getName)
						 .findFirst()
						 .orElseThrow(() -> new IllegalStateException("Manager role is missing"));
	}

}
