package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Package;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MetaUtilsTest
{
	@Test
	public void isSystemPackageFalse()
	{
		Package package_ = mock(Package.class);
		when(package_.getId()).thenReturn("notSystem");
		assertFalse(MetaUtils.isSystemPackage(package_));
	}

	@Test
	public void isSystemPackageTrue()
	{
		Package package_ = mock(Package.class);
		when(package_.getId()).thenReturn(PACKAGE_SYSTEM);
		assertTrue(MetaUtils.isSystemPackage(package_));
	}

	@Test
	public void isSystemPackageTrueNested()
	{
		Package rootPackage_ = mock(Package.class);
		when(rootPackage_.getId()).thenReturn(PACKAGE_SYSTEM);

		Package package_ = mock(Package.class);
		when(package_.getId()).thenReturn("systemChild");
		when(package_.getRootPackage()).thenReturn(rootPackage_);
		assertTrue(MetaUtils.isSystemPackage(package_));
	}
}