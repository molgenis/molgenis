package org.molgenis.security.permission;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MolgenisPermissionControllerTest
{

	private MolgenisPermissionService molgenisPermissionService;
	private MolgenisPermissionController molgenisPermissionController;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		molgenisPermissionService = mock(MolgenisPermissionService.class);
		molgenisPermissionController = new MolgenisPermissionController(molgenisPermissionService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisPermissionController()
	{
		new MolgenisPermissionController(null);
	}

	@Test
	public void hasReadPermissionTrue()
	{
		String entityName = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.READ)).thenReturn(true);
		assertTrue(molgenisPermissionController.hasReadPermission(entityName));
	}

	@Test
	public void hasReadPermissionFalse()
	{
		String entityName = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.READ)).thenReturn(false);
		assertFalse(molgenisPermissionController.hasReadPermission(entityName));
	}

	@Test
	public void hasWritePermissionTrue()
	{
		String entityName = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE)).thenReturn(true);
		assertTrue(molgenisPermissionController.hasWritePermission(entityName));
	}

	@Test
	public void hasWritePermissionFalse()
	{
		String entityName = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE)).thenReturn(false);
		assertFalse(molgenisPermissionController.hasWritePermission(entityName));
	}
}
