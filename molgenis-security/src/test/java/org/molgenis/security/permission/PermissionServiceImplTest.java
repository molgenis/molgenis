package org.molgenis.security.permission;

import org.molgenis.security.core.Permission;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class PermissionServiceImplTest
{
	private static PermissionServiceImpl permissionService;

	@BeforeClass
	public void beforeClass()
	{
		permissionService = new PermissionServiceImpl();
	}

	@Test
	public void hasPermissionOnEntity()
	{
		assertTrue(permissionService.hasPermissionOnEntityType("entity1", Permission.READ));
	}

	@Test
	public void hasPermissionOnPlugin()
	{
		assertTrue(permissionService.hasPermissionOnPlugin("plugin1", Permission.READ));
	}
}
