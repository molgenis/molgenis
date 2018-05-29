package org.molgenis.data.security.auth;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.PackageValue;
import org.molgenis.security.core.model.RoleValue;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.SidUtils.createRoleSid;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
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
	private Group group;
	@Mock
	private Role role;
	@Mock
	private Package aPackage;

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
		groupService = new GroupService(groupFactory, roleFactory, packageFactory, dataService, permissionService);
	}

	@Test
	public void testPersist()
	{
		when(groupFactory.create(groupValue)).thenReturn(group);
		when(roleFactory.create(roleValue)).thenReturn(role);
		when(packageFactory.create(packageValue)).thenReturn(aPackage);

		groupService.persist(groupValue);

		verify(dataService).add(GroupMetadata.GROUP, group);
		verify(dataService).add(eq(RoleMetadata.ROLE), roleCaptor.capture());
		verify(dataService).add(PackageMetadata.PACKAGE, aPackage);
		assertEquals(roleCaptor.getValue().collect(Collectors.toList()), Collections.singletonList(role));
		verify(role).setGroup(group);
	}

	@Test
	public void testGrantPermissions()
	{
		groupService.grantPermissions(groupValue);

		verify(permissionService).grant(new PackageIdentity("package"), WRITEMETA, createRoleSid("NAME_MANAGER"));
	}
}