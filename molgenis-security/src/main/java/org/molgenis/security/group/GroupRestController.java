package org.molgenis.security.group;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.molgenis.data.DataService;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.PermissionService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.security.core.PermissionSet.*;

@RestController
@Api("Group")
public class GroupRestController
{
	private final GroupValueFactory groupValueFactory;
	private final GroupService groupService;
	private final PermissionService permissionService;
	private final UserService userService;
	private final RoleMembershipFactory roleMembershipFactory;
	private final DataService dataService;

	private static final String MANAGER = "Manager";
	private static final String EDITOR = "Editor";
	private static final String VIEWER = "Viewer";

	private static final Map<String, PermissionSet> DEFAULT_ROLES = ImmutableMap.of(MANAGER, WRITEMETA, EDITOR, WRITE,
			VIEWER, READ);

	public GroupRestController(GroupValueFactory groupValueFactory, GroupService groupService,
			PermissionService permissionService, UserService userService, RoleMembershipFactory roleMembershipFactory,
			DataService dataService)
	{
		this.groupValueFactory = requireNonNull(groupValueFactory);
		this.groupService = requireNonNull(groupService);
		this.permissionService = requireNonNull(permissionService);
		this.userService = requireNonNull(userService);
		this.roleMembershipFactory = requireNonNull(roleMembershipFactory);
		this.dataService = requireNonNull(dataService);
	}

	@PostMapping("api/plugin/group")
	@ApiOperation(value = "Create a new Group", response = String.class)
	@Transactional
	public String createGroup(
			@ApiParam("Alphanumeric name for the group, will be generated based on the label if not specified.") @RequestParam(name = "name", required = false) @Nullable String name,
			@ApiParam("Label for the group") @RequestParam("label") String label,
			@ApiParam("Description for the group") @RequestParam(name = "description", required = false) @Nullable String description,
			@ApiParam("Indication if this group should be publicly visible (not yet implemented!)") @RequestParam(name = "public", required = false, defaultValue = "true") boolean publiclyVisible)
	{
		GroupValue groupValue = groupValueFactory.createGroup(name, label, description, publiclyVisible,
				DEFAULT_ROLES.keySet());
		Group group = groupService.persist(groupValue);
		grantPermissions(group);

		addGroupCreatorToManagerRole(group);

		return groupValue.getName();
	}

	private void addGroupCreatorToManagerRole(Group group)
	{
		User groupCreator = userService.getUser(SecurityUtils.getCurrentUsername());
		Stream<Role> roles = stream(group.getRoles().spliterator(), false);

		RoleMembership roleMembership = roleMembershipFactory.create();
		roleMembership.setUser(groupCreator);
		roleMembership.setFrom(Instant.now());
		roleMembership.setRole(roles.filter(role -> role.getLabel().equals(MANAGER))
									.findFirst()
									.orElseThrow(() -> new IllegalStateException("Manager role is missing")));

		dataService.add(ROLE_MEMBERSHIP, roleMembership);
	}

	private void grantPermissions(Group group)
	{
		PackageIdentity packageIdentity = new PackageIdentity(group.getRootPackage());
		group.getRoles()
			 .forEach(role -> permissionService.grant(packageIdentity, DEFAULT_ROLES.get(role.getLabel()), role));
	}

}
