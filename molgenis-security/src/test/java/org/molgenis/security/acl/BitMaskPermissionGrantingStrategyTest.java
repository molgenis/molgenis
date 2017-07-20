package org.molgenis.security.acl;

import org.mockito.Mock;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.Permission;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.mockito.Mockito.reset;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.security.acls.domain.BasePermission.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BitMaskPermissionGrantingStrategyTest
{
	private BitMaskPermissionGrantingStrategy permissionGrantingStrategy;
	@Mock
	private AuditLogger auditLogger;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		permissionGrantingStrategy = new BitMaskPermissionGrantingStrategy(auditLogger);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(auditLogger);
	}

	@DataProvider(name = "permissionsMatchProvider")
	public Object[][] permissionsMatchProvider()
	{
		return new Object[][] { { READ, READ }, { new CumulativePermission().set(READ).set(WRITE), READ },
				{ new CumulativePermission().set(READ).set(WRITE), WRITE },
				{ new CumulativePermission().set(READ).set(WRITE), new CumulativePermission().set(READ).set(WRITE) }

		};
	}

	@Test(dataProvider = "permissionsMatchProvider")
	public void testPermissionsMatch(Permission acePermission, Permission testedPermission) throws Exception
	{
		assertTrue(permissionGrantingStrategy.containsPermission(acePermission.getMask(), testedPermission.getMask()),
				format("combined ACE permission {} should match tested permission {}", acePermission,
						testedPermission));
	}

	@DataProvider(name = "permissionsDontMatchProvider")
	public Object[][] permissionsDontMatchProvider()
	{
		return new Object[][] { { READ, WRITE }, { new CumulativePermission().set(READ).set(WRITE), ADMINISTRATION },
				{ WRITE, READ }, { new CumulativePermission().set(READ).set(ADMINISTRATION), WRITE }

		};
	}

	@Test(dataProvider = "permissionsDontMatchProvider")
	public void testPermissionsDontMatch(Permission acePermission, Permission testedPermission) throws Exception
	{
		assertFalse(permissionGrantingStrategy.containsPermission(acePermission.getMask(), testedPermission.getMask()),
				format("combined ACE permission {} should NOT match tested permission {}", acePermission,
						testedPermission));
	}

}