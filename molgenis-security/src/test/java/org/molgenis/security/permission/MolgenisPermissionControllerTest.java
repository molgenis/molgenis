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
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.READ)).thenReturn(true);
		assertTrue(molgenisPermissionController.hasReadPermission(entityTypeId));
	}

	@Test
	public void hasReadPermissionFalse()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.READ)).thenReturn(false);
		assertFalse(molgenisPermissionController.hasReadPermission(entityTypeId));
	}

	@Test
	public void hasWritePermissionTrue()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.WRITE)).thenReturn(true);
		assertTrue(molgenisPermissionController.hasWritePermission(entityTypeId));
	}

	@Test
	public void hasWritePermissionFalse()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.WRITE)).thenReturn(false);
		assertFalse(molgenisPermissionController.hasWritePermission(entityTypeId));
	}
}
