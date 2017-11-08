package org.molgenis.data.meta;

import org.testng.annotations.Test;

public class PackageRepositoryDecoratorTest
{
	@Test(expectedExceptions = NullPointerException.class)
	public void testPackageRepositoryDecorator() throws Exception
	{
		new PackageRepositoryDecorator(null, null);
	}
}