package org.molgenis.data.security.auth;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.model.GroupValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.UserMetaData.USERNAME;
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.molgenis.data.security.auth.GroupMetadata.GROUP;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.security.core.PermissionSet.*;

@Service
public class GroupService
{
	private final DataService dataService;
	private final PermissionService permissionService;
	private final GroupFactory groupFactory;
	private final RoleFactory roleFactory;
	private final PackageFactory packageFactory;
	private final GroupMetadata groupMetadata;
	private final RoleMembershipService roleMembershipService;

	public static final String MANAGER = "Manager";
	private static final String EDITOR = "Editor";
	private static final String VIEWER = "Viewer";

	public static final Map<String, PermissionSet> DEFAULT_ROLES = ImmutableMap.of(MANAGER, WRITEMETA, EDITOR, WRITE,
			VIEWER, READ);

	GroupService(GroupFactory groupFactory, RoleFactory roleFactory, PackageFactory packageFactory,
			DataService dataService, PermissionService permissionService, GroupMetadata groupMetadata,
			RoleMembershipService roleMembershipService)
	{
		this.groupFactory = requireNonNull(groupFactory);
		this.roleFactory = requireNonNull(roleFactory);
		this.packageFactory = requireNonNull(packageFactory);
		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
		this.groupMetadata = requireNonNull(groupMetadata);
		this.roleMembershipService = requireNonNull(roleMembershipService);
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
		List<Role> roles = groupValue.getRoles().stream().map(roleFactory::create).collect(Collectors.toList());

		Group group = groupFactory.create(groupValue);
		group.setRootPackage(rootPackage.getId());
		group.setRoles(roles);

		dataService.add(PACKAGE, rootPackage);
		dataService.add(GROUP, group);
		roles.forEach(role -> role.setGroup(group));
		dataService.add(ROLE, roles.stream());
	}

	/**
	 * Grants default permissions on the root package to the roles of the group
	 *
	 * @param groupValue details of the group for which the permissions will be granted
	 */
	public void grantPermissions(GroupValue groupValue)
	{
		PackageIdentity packageIdentity = new PackageIdentity(groupValue.getRootPackage().getName());
		groupValue.getRoles()
				  .forEach(
						  roleValue -> permissionService.grant(packageIdentity, DEFAULT_ROLES.get(roleValue.getLabel()),
								  createRoleSid(roleValue.getName())));
	}

	/**
	 * Get the group entity by its unique name.
	 *
	 * @param groupName unique group name
	 * @return group with given name
	 * @throws UnknownEntityException in case no group with given name can be retrieved
	 */
	public Group getGroup(String groupName)
	{
		Group group = dataService.query(GroupMetadata.GROUP, Group.class).eq(GroupMetadata.NAME, groupName).findOne();
		if (group == null)
		{
			throw new UnknownEntityException(groupMetadata, groupMetadata.getAttribute(GroupMetadata.NAME), groupName);
		}
		return group;
	}

	/**
	 * Add member to group.
	 * User can only be added to a role that belongs to the group.
	 * The user can only have a single role within the group
	 *
	 * @param group group to add the user to in the given role
	 * @param user user to be added in the given role to the given group
	 * @param role role in which the given user is to be added to given group
	 */
	@Transactional
	public void addMember(final Group group, final User user, final Role role )
	{
		ArrayList<Role> groupRoles = Lists.newArrayList(group.getRoles());

		boolean isGroupRole = groupRoles.contains(role);

		if(!isGroupRole)
		{
			throw new CannotAddMemberToNonGroupRole();
		}

		Collection<RoleMembership> memberships = roleMembershipService.getMemberships(groupRoles);

		boolean isMember = memberships.stream().parallel().anyMatch(m -> m.getUser().equals(user));

		if(isMember)
		{
			throw new CannotAddMultipleRolesToGroupMember();
		}

		roleMembershipService.addUserToRole(user, role);
	}
}
