package org.molgenis.security.permission;

import org.mockito.Mock;
import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.data.security.acl.EntityIdentity;
import org.molgenis.security.core.Permission;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.security.core.Permission.*;
import static org.testng.Assert.assertEquals;

public class PermissionServiceImplTest extends AbstractMockitoTest
{
	@Mock
	private EntityAclService entityAclService;

	private PermissionServiceImpl molgenisPermissionService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		molgenisPermissionService = new PermissionServiceImpl(entityAclService);
	}

	@DataProvider(name = "hasPermissionOnEntityProvider")
	public static Iterator<Object[]> testHasPermissionOnEntityProvider()
	{
		List<Object[]> dataList = new ArrayList<>();
		dataList.add(new Object[] { "myEntityType", READ, true });
		dataList.add(new Object[] { "myEntityType", WRITE, false });
		dataList.add(new Object[] { "myOtherEntityType", COUNT, false });
		return dataList.iterator();
	}

	@Test(dataProvider = "hasPermissionOnEntityProvider")
	public void testHasPermissionOnEntity(String entityTypeId, Permission permission, boolean expectedIsGranted)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(ENTITY_TYPE_META_DATA, "myEntityType");
		when(entityAclService.isGranted(entityIdentity, READ)).thenReturn(true);
		when(entityAclService.isGranted(entityIdentity, WRITE)).thenReturn(false);
		assertEquals(molgenisPermissionService.hasPermissionOnEntityType(entityTypeId, permission), expectedIsGranted);
	}

	@DataProvider(name = "hasPermissionOnPluginProvider")
	public static Iterator<Object[]> testHasPermissionOnPlugin()
	{
		List<Object[]> dataList = new ArrayList<>();
		dataList.add(new Object[] { "myPlugin", READ, true });
		dataList.add(new Object[] { "myOtherPlugin", READ, false });
		return dataList.iterator();
	}

	@Test(dataProvider = "hasPermissionOnPluginProvider")
	public void testHasPermissionOnPlugin(String pluginId, Permission permission, boolean expectedIsGranted)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(PLUGIN, "myPlugin");
		when(entityAclService.isGranted(entityIdentity, READ)).thenReturn(true);
		assertEquals(molgenisPermissionService.hasPermissionOnPlugin(pluginId, permission), expectedIsGranted);
	}
}
