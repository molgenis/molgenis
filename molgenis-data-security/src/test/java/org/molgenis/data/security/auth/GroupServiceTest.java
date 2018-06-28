package org.molgenis.data.security.auth;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.PackageValue;
import org.molgenis.security.core.model.RoleValue;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.testng.Assert.assertEquals;

public class GroupServiceTest extends AbstractMockitoTest
{
	@Mock
	private GroupFactory groupFactory;
	@Mock
	private RoleFactory roleFactory;
	@Mock
	private PackageFactory packageFactory;
	@Mock
	private DataService dataService;
	@Mock
	private PermissionService permissionService;
	@Mock
	private GroupMetadata groupMetadata;
	@Mock
	private RoleMembershipService roleMembershipService;
	@Mock
	private UserService userService;
	@Mock
	private RoleMetadata roleMetadata;
	@Mock
	private Attribute nameAttribute;

	@Mock
	private Group group;
	@Mock
	private Role role;
	@Mock
	private Package aPackage;
	@Mock
	private Role roleManager;

	@Captor
	private ArgumentCaptor<Stream<Role>> roleCaptor;

	private GroupService groupService;

	private GroupValue groupValue;
	private RoleValue roleValue;
	private PackageValue packageValue;

	@BeforeMethod
	public void beforeMethod()
	{
		packageValue = PackageValue.builder().setName("package").setLabel("Package").build();
		roleValue = RoleValue.builder().setName("NAME_MANAGER").setLabel("Manager").build();
		GroupValue.Builder builder = GroupValue.builder()
											   .setRootPackage(packageValue)
											   .setName("name")
											   .setLabel("label")
											   .setPublic(true)
											   .setDescription("description");
		builder.rolesBuilder().add(roleValue);
		groupValue = builder.build();
		groupService = new GroupService(groupFactory, roleFactory, packageFactory, dataService, permissionService,
				groupMetadata, roleMembershipService, userService);
		groupService = new GroupService(groupFactory, roleFactory, packageFactory, dataService, permissionService,
				roleMetadata);
	}

	@Test
	public void testPersist()
	{
		when(groupFactory.create(groupValue)).thenReturn(group);
		when(roleFactory.create(roleValue)).thenReturn(role);
		when(packageFactory.create(packageValue)).thenReturn(aPackage);
		when(role.getLabel()).thenReturn("Manager");

		when(dataService.findOne(ROLE, EQ(NAME, "MANAGER"), Role.class)).thenReturn(roleManager);

		groupService.persist(groupValue);

		verify(dataService).add(GroupMetadata.GROUP, group);
		verify(dataService).add(eq(ROLE), roleCaptor.capture());
		verify(dataService).add(PackageMetadata.PACKAGE, aPackage);
		assertEquals(roleCaptor.getValue().collect(toList()), singletonList(role));

		verify(role).setGroup(group);
		verify(role).setIncludes(singletonList(roleManager));
	}

	@Test(expectedExceptions = UnknownEntityException.class, expectedExceptionsMessageRegExp = "type:sys_sec_Role id:MANAGER attribute:name")
	public void testPersistIncludedRoleNotFound()
	{
		when(roleFactory.create(roleValue)).thenReturn(role);
		when(packageFactory.create(packageValue)).thenReturn(aPackage);
		when(role.getLabel()).thenReturn("Manager");

		when(roleMetadata.getId()).thenReturn(ROLE);
		when(roleMetadata.getAttribute(NAME)).thenReturn(nameAttribute);
		when(nameAttribute.getName()).thenReturn(NAME);

		groupService.persist(groupValue);
	}

	@Test
	public void testGrantPermissions()
	{
		groupService.grantPermissions(groupValue);

		verify(permissionService).grant(new PackageIdentity("package"), WRITEMETA, createRoleSid("NAME_MANAGER"));
	}
}
