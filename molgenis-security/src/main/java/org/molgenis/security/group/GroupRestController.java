package org.molgenis.security.group;

import io.swagger.annotations.*;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupMetadata;
import org.molgenis.data.security.auth.GroupService;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.RoleValue;
import org.molgenis.web.ErrorMessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.GroupService.DEFAULT_ROLES;
import static org.molgenis.data.security.auth.GroupService.MANAGER;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@RestController
@Validated
@Api("Group")
public class GroupRestController
{
	public final static String SECURITY_API_PATH = "api/plugin/security";
	public final static String GROUP = "/group";

	public final static String GROUP_END_POINT = SECURITY_API_PATH + GROUP;

	private final GroupValueFactory groupValueFactory;
	private final GroupService groupService;
	private final RoleMembershipService roleMembershipService;
	private final DataService dataService;

	GroupRestController(GroupValueFactory groupValueFactory, GroupService groupService,
			RoleMembershipService roleMembershipService, DataService dataService)
	{
		this.groupValueFactory = requireNonNull(groupValueFactory);
		this.groupService = requireNonNull(groupService);
		this.roleMembershipService = requireNonNull(roleMembershipService);
		this.dataService = requireNonNull(dataService);
	}

	@PostMapping("api/plugin/group")
	@ApiOperation(value = "Create a new Group", response = String.class)
	@Transactional
	@ApiResponses({
			@ApiResponse(code = 400, message = "When the request is incorrect", response = ErrorMessageResponse.class),
			@ApiResponse(code = 401, message = "When authentication information is missing", response = ErrorMessageResponse.class),
			@ApiResponse(code = 403, message = "When the authenticated user has insufficient permissions", response = ErrorMessageResponse.class),
			@ApiResponse(code = 200, message = "The name of the newly created group", response = String.class) })
	public String createGroup(
			@ApiParam("Alphanumeric name for the group") @Pattern(regexp = "^[a-z][a-z0-9]*(-[a-z0-9]+)*$") @RequestParam(name = "name", value = "name") String name,
			@ApiParam("Label for the group") @RequestParam("label") String label,
			@ApiParam("Description for the group") @RequestParam(value = "description", required = false) @Nullable String description,
			@ApiParam(value = "Indication if this group should be publicly visible (not yet implemented!)", defaultValue = "true") @RequestParam(value = "public", required = false, defaultValue = "true") boolean publiclyVisible)
	{
		GroupValue groupValue = groupValueFactory.createGroup(name, label, description, publiclyVisible,
				DEFAULT_ROLES.keySet());

		groupService.persist(groupValue);
		groupService.grantPermissions(groupValue);
		roleMembershipService.addUserToRole(getCurrentUsername(), getManagerRoleName(groupValue));

		return groupValue.getName();
	}

	@PostMapping(GROUP_END_POINT)
	@Transactional
	public ResponseEntity createGroup(@RequestBody GroupCommand group)
	{

		GroupValue groupValue = groupValueFactory.createGroup(group.getName(), group.getLabel(), DEFAULT_ROLES.keySet());

		groupService.persist(groupValue);
		groupService.grantPermissions(groupValue);
		roleMembershipService.addUserToRole(getCurrentUsername(), getManagerRoleName(groupValue));

		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest().path("/{name}")
				.buildAndExpand(groupValue.getName()).toUri();

		return ResponseEntity.created(location).build();
	}

	@GetMapping(GROUP_END_POINT)
	@ResponseBody
	public List<GroupResponse> getGroups()
	{
		return dataService.findAll(GroupMetadata.GROUP, Group.class)
						  .map(GroupResponse::fromEntity)
						  .collect(Collectors.toList());
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
