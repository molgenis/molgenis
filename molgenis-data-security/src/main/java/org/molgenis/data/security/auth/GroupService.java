package org.molgenis.data.security.auth;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.security.auth.GroupMetadata.GROUP;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;

@Service
public class GroupService
{
	private final DataService dataService;
	private final UserService userService;
	private final RoleMembershipFactory roleMembershipFactory;
	private final GroupFactory groupFactory;
	private final RoleFactory roleFactory;
	private final PackageFactory packageFactory;

	public GroupService(GroupFactory groupFactory, RoleFactory roleFactory, PackageFactory packageFactory,
			DataService dataService, UserService userService, RoleMembershipFactory roleMembershipFactory)
	{
		this.groupFactory = requireNonNull(groupFactory);
		this.roleFactory = requireNonNull(roleFactory);
		this.packageFactory = requireNonNull(packageFactory);
		this.dataService = requireNonNull(dataService);
		this.userService = requireNonNull(userService);
		this.roleMembershipFactory = requireNonNull(roleMembershipFactory);
	}

	/**
	 * Creates {@link Group}, {@link Role} and {@link Package} entities and adds them to the dataService.
	 *
	 * @param groupValue details of the group that should be created
	 */
	@Transactional
	public void persist(GroupValue groupValue)
	{
		Package rootPackage = packageFactory.create(groupValue.getRootPackage());

		Group group = groupFactory.create(groupValue);
		group.setRootPackage(rootPackage);

		dataService.add(PACKAGE, rootPackage);
		dataService.add(GROUP, group);
		List<Role> roles = groupValue.getRoles().stream().map(roleFactory::create).collect(Collectors.toList());
		roles.forEach(role -> role.setGroup(group));
		dataService.add(ROLE, roles.stream());

		User groupCreator = userService.getUser(SecurityUtils.getCurrentUsername());

		RoleMembership roleMembership = roleMembershipFactory.create();
		roleMembership.setRole(roles.stream()
									.filter(role -> StringUtils.containsIgnoreCase(role.getLabel(), "manager"))
									.findFirst()
									.orElseThrow(() -> new NullPointerException("No manager role")));
		roleMembership.setFrom(Instant.now());
		roleMembership.setUser(groupCreator);

		dataService.add(ROLE_MEMBERSHIP, roleMembership);
	}
}