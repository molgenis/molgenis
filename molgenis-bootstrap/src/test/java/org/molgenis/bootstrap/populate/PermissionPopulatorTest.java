package org.molgenis.bootstrap.populate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.mockito.Mock;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class PermissionPopulatorTest extends AbstractMockitoTest
{
	@Mock
	private MutableAclService mutableAclService;

	private PermissionPopulator permissionPopulator;

	@BeforeMethod
	private void setUpBeforeMethod()
	{
		permissionPopulator = new PermissionPopulator(mutableAclService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPermissionPopulator()
	{
		new PermissionPopulator(null);
	}

	@Test
	public void testPopulate()
	{
		ApplicationContext applicationContext = mock(ApplicationContext.class);

		ObjectIdentity objectIdentity0 = new ObjectIdentityImpl("type", "id0");
		PermissionRegistry permissionRegistry0 = mock(PermissionRegistry.class);
		Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> registry0Permissions = ArrayListMultimap.create();
		registry0Permissions.put(objectIdentity0, new Pair<>(mock(PermissionSet.class), mock(Sid.class)));
		when(permissionRegistry0.getPermissions()).thenReturn(registry0Permissions);

		ObjectIdentity objectIdentity1 = new ObjectIdentityImpl("type", "id1");
		Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> registry1Permissions = ArrayListMultimap.create();
		registry1Permissions.put(objectIdentity1, new Pair<>(mock(PermissionSet.class), mock(Sid.class)));
		PermissionRegistry permissionRegistry1 = mock(PermissionRegistry.class);
		when(permissionRegistry1.getPermissions()).thenReturn(registry1Permissions);

		Map<String, PermissionRegistry> registryMap = new LinkedHashMap<>();
		registryMap.put("registry0", permissionRegistry0);
		registryMap.put("registry1", permissionRegistry1);
		when(applicationContext.getBeansOfType(PermissionRegistry.class)).thenReturn(registryMap);

		MutableAcl acl0 = mock(MutableAcl.class);
		doReturn(acl0).when(mutableAclService).readAclById(objectIdentity0);

		MutableAcl acl1 = mock(MutableAcl.class);
		doReturn(acl1).when(mutableAclService).readAclById(objectIdentity1);

		permissionPopulator.populate(applicationContext);

		verify(mutableAclService).updateAcl(acl0);
		verify(mutableAclService).updateAcl(acl1);

	}
}