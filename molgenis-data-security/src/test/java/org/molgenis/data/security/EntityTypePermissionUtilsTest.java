package org.molgenis.data.security;

import org.springframework.security.acls.domain.CumulativePermission;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.molgenis.data.security.EntityTypePermission.*;
import static org.testng.Assert.assertEquals;

public class EntityTypePermissionUtilsTest
{
	@DataProvider(name = "testGetCumulativePermissionProvider")
	public static Iterator<Object[]> testGetCumulativePermissionProvider()
	{
		return asList(
				new Object[] { WRITEMETA, new CumulativePermission().set(WRITEMETA).set(WRITE).set(READ).set(COUNT) },
				new Object[] { WRITE, new CumulativePermission().set(WRITE).set(READ).set(COUNT) },
				new Object[] { READ, new CumulativePermission().set(READ).set(COUNT) },
				new Object[] { COUNT, new CumulativePermission().set(COUNT) }).iterator();
	}

	@Test(dataProvider = "testGetCumulativePermissionProvider")
	public void testGetCumulativePermission(EntityTypePermission entityTypePermission,
			CumulativePermission expectedCumulativePermission)
	{
		assertEquals(EntityTypePermissionUtils.getCumulativePermission(entityTypePermission),
				expectedCumulativePermission);
	}
}