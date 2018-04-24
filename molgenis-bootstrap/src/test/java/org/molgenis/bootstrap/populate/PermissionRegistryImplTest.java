package org.molgenis.bootstrap.populate;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PermissionRegistryImplTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;

	private PermissionRegistryImpl permissionRegistryImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		permissionRegistryImpl = new PermissionRegistryImpl(dataService);
	}

	@Test
	public void testGetPermissions()
	{
		assertEquals("", "");
	}
	/*	@SuppressWarnings("unchecked")
		Query<Group> query = mock(Query.class);
		when(query.eq("name", "All Users")).thenReturn(query);
		when(dataService.query("sys_sec_Group", Group.class)).thenReturn(query);
		Group group = when(mock(Group.class).getId()).thenReturn("group0").getMock();
		when(query.findOne()).thenReturn(group);
		Multimap<ObjectIdentity, Pair<Permission, Sid>> expectedPermissions = ImmutableListMultimap.of(
				new PluginIdentity("useraccount"), new Pair<>(PluginPermission.READ,
						new GrantedAuthoritySid("ROLE_group0")));
		assertEquals(permissionRegistryImpl.getPermissions(), expectedPermissions);
	}*/
}