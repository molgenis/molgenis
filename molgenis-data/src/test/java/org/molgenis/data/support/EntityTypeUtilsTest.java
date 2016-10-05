package org.molgenis.data.support;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.Package;
import org.testng.annotations.Test;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.testng.Assert.assertEquals;

public class EntityTypeUtilsTest
{
	@Test
	public void getAttributeNames()
	{
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		assertEquals(newArrayList(EntityTypeUtils.getAttributeNames(asList(attr0, attr1))),
				asList("attr0", "attr1"));
	}

	@Test
	public void buildFullName()
	{
		Package defaultPackage = when(mock(Package.class).getName()).thenReturn(PACKAGE_DEFAULT).getMock();
		assertEquals(EntityTypeUtils.buildFullName(defaultPackage, "simpleName"), "simpleName");

		assertEquals(EntityTypeUtils.buildFullName(null, "simpleName"), "simpleName");

		Package package_1 = when(mock(Package.class).getName()).thenReturn("base").getMock();
		assertEquals(EntityTypeUtils.buildFullName(package_1, "simpleName"), "simpleName");

		Package package_2 = when(mock(Package.class).getName()).thenReturn("my_first_package").getMock();
		assertEquals(EntityTypeUtils.buildFullName(package_2, "simpleName"), "my_first_package_simpleName");
	}
}
