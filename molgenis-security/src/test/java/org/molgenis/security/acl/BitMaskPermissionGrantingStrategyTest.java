package org.molgenis.security.acl;

import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.Permission;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.molgenis.security.core.PermissionSet.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BitMaskPermissionGrantingStrategyTest
{
	@DataProvider(name = "permissionsMatchProvider")
	public Object[][] permissionsMatchProvider()
	{
		return new Object[][] { { READ, new CumulativePermission().set(READ).set(WRITE) }, { READ, READ } };
	}

	@Test(dataProvider = "permissionsMatchProvider")
	public void testPermissionsMatch(Permission acePermission, Permission testedPermission)
	{
		assertTrue(BitMaskPermissionGrantingStrategy.containsPermission(acePermission.getMask(),
				testedPermission.getMask()),
				format("combined ACE permission %s should match tested permission %s", acePermission,
						testedPermission));
	}

	@DataProvider(name = "permissionsDontMatchProvider")
	public Object[][] permissionsDontMatchProvider()
	{
		return new Object[][] { { READ, WRITE },
				{ COUNT, new CumulativePermission().set(READ).set(WRITE).set(WRITEMETA) },
				{ WRITE, new CumulativePermission().set(READ).set(WRITEMETA) }
		};
	}

	@Test(dataProvider = "permissionsDontMatchProvider")
	public void testPermissionsDontMatch(Permission acePermission, Permission testedPermission)
	{
		assertFalse(BitMaskPermissionGrantingStrategy.containsPermission(acePermission.getMask(),
				testedPermission.getMask()),
				format("combined ACE permission %s should NOT match tested permission %s", acePermission,
						testedPermission));
	}

}