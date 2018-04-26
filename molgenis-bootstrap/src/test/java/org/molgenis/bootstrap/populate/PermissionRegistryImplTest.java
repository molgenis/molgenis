package org.molgenis.bootstrap.populate;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.auth.Group;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.Pair;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
		@SuppressWarnings("unchecked")
		Query<Group> query = mock(Query.class);
		when(query.eq("name", "All Users")).thenReturn(query);
		when(dataService.query("sys_sec_Group", Group.class)).thenReturn(query);
		Group group = when(mock(Group.class).getId()).thenReturn("group0").getMock();
		when(query.findOne()).thenReturn(group);
		Multimap<ObjectIdentity, Pair<Permission, Sid>> expectedPermissions = ImmutableListMultimap.of(
				new PluginIdentity("useraccount"), new Pair<>(PluginPermission.READ,
						new GrantedAuthoritySid("ROLE_group0")));
		assertEquals(permissionRegistryImpl.getPermissions(), expectedPermissions);
	}
}