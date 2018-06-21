package org.molgenis.security.group;

import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.*;
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
import java.util.ArrayList;
import java.util.Collection;
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

	@PostMapping(GROUP_END_POINT)
	@ApiOperation(value = "Create a new group", response = ResponseEntity.class)
	@Transactional
	@ApiResponses({ @ApiResponse(code = 201, message = "New group created", response = ResponseEntity.class) })
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
	@ApiOperation(value = "Get list with groups", response = ResponseEntity.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "List of groupResponse object available to user", response = List.class) })
	@ResponseBody
	public List<GroupResponse> getGroups()
	{
		return dataService.findAll(GroupMetadata.GROUP, Group.class)
						  .map(GroupResponse::fromEntity)
						  .collect(Collectors.toList());
	}

	@GetMapping(GROUP_END_POINT + "/{groupName}/member")
	@ApiOperation(value = "Get group members", response = Collection.class)
	@ResponseBody
	public Collection<GroupMemberResponse> getMembers(@PathVariable(value = "groupName") String groupName)
	{
		Iterable<Role> roles = groupService.getGroup(groupName).getRoles();
		return roleMembershipService.getMemberships(Lists.newArrayList(roles))
							 .stream()
							 .map(GroupMemberResponse::fromEntity)
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
