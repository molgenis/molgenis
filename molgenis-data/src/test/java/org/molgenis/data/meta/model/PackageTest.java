package org.molgenis.data.meta.model;

import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

public class PackageTest
{
	@Test
	public void getRootPackageNoParent() throws Exception
	{
		PackageMetadata packageMetadata = mock(PackageMetadata.class);
		Package package_ = new Package(packageMetadata);
		assertEquals(package_.getRootPackage(), package_);
	}

	@Test
	public void getRootPackageParent() throws Exception
	{
		PackageMetadata packageMetadata = mock(PackageMetadata.class);
		Attribute parentAttr = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
		when(packageMetadata.getAttribute(PackageMetadata.PARENT)).thenReturn(parentAttr);
		Package grandParentPackage = new Package(packageMetadata);
		Package parentParent = new Package(packageMetadata);
		parentParent.setParent(grandParentPackage);
		Package package_ = new Package(packageMetadata);
		package_.setParent(parentParent);
		assertEquals(package_.getRootPackage(), grandParentPackage);
	}
}