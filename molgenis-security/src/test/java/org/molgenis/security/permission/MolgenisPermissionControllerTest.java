package org.molgenis.security.permission;

import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MolgenisPermissionControllerTest
{

	private UserPermissionEvaluator permissionService;
	private MolgenisPermissionController molgenisPermissionController;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		permissionService = mock(UserPermissionEvaluator.class);
		molgenisPermissionController = new MolgenisPermissionController(permissionService);
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
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), PermissionSet.READ)).thenReturn(
				true);
		assertTrue(molgenisPermissionController.hasReadPermission(entityTypeId));
	}

	@Test
	public void hasReadPermissionFalse()
	{
		String entityTypeId = "entity";
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), PermissionSet.READ)).thenReturn(
				false);
		assertFalse(molgenisPermissionController.hasReadPermission(entityTypeId));
	}

	@Test
	public void hasWritePermissionTrue()
	{
		String entityTypeId = "entity";
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), PermissionSet.WRITE)).thenReturn(
				true);
		assertTrue(molgenisPermissionController.hasWritePermission(entityTypeId));
	}

	@Test
	public void hasWritePermissionFalse()
	{
		String entityTypeId = "entity";
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), PermissionSet.WRITE)).thenReturn(
				false);
		assertFalse(molgenisPermissionController.hasWritePermission(entityTypeId));
	}
}
