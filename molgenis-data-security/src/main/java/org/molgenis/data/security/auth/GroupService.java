package org.molgenis.data.security.auth;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.model.GroupValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.security.auth.GroupMetadata.GROUP;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.security.core.PermissionSet.*;
import static org.molgenis.security.core.SidUtils.createRoleAuthority;
import static org.molgenis.security.core.SidUtils.createRoleSid;

@Service
public class GroupService
{
	private final DataService dataService;
	private final PermissionService permissionService;
	private final GroupFactory groupFactory;
	private final RoleFactory roleFactory;
	private final PackageFactory packageFactory;
	private final RoleMetadata roleMetadata;

	public static final String MANAGER = "Manager";
	private static final String EDITOR = "Editor";
	private static final String VIEWER = "Viewer";

	public static final String AUTHORITY_MANAGER = createRoleAuthority(MANAGER.toUpperCase());
	public static final String AUTHORITY_EDITOR = createRoleAuthority(EDITOR.toUpperCase());
	public static final String AUTHORITY_VIEWER = createRoleAuthority(VIEWER.toUpperCase());


	public static final Map<String, PermissionSet> DEFAULT_ROLES = ImmutableMap.of(MANAGER, WRITEMETA, EDITOR, WRITE,
			VIEWER, READ);

	GroupService(GroupFactory groupFactory, RoleFactory roleFactory, PackageFactory packageFactory,
			DataService dataService, PermissionService permissionService, RoleMetadata roleMetadata)
	{
		this.groupFactory = requireNonNull(groupFactory);
		this.roleFactory = requireNonNull(roleFactory);
		this.packageFactory = requireNonNull(packageFactory);
		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
		this.roleMetadata = requireNonNull(roleMetadata);
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

		List<Role> roles = groupValue.getRoles()
									 .stream()
									 .map(roleFactory::create)
									 .map(this::addIncludedRole).collect(toList());

		Group group = groupFactory.create(groupValue);
		group.setRootPackage(rootPackage.getId());
		group.setRoles(roles);

		dataService.add(PACKAGE, rootPackage);
		dataService.add(GROUP, group);
		roles.forEach(role -> role.setGroup(group));
		dataService.add(ROLE, roles.stream());
	}

	private Role addIncludedRole(Role role)
	{
		role.setIncludes(singletonList(findRoleNamed(role.getLabel().toUpperCase())));
		return role;
	}

	private Role findRoleNamed(String rolename)
	{
		Query<Role> query = QueryImpl.EQ(RoleMetadata.NAME, rolename);
		Role result = dataService.findOne(ROLE, query, Role.class);
		if (result == null)
		{
			throw new UnknownEntityException(roleMetadata, roleMetadata.getAttribute(NAME), rolename);
		}
		return result;
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
}
