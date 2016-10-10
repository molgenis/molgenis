package org.molgenis.data.support;

import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.Package;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.testng.Assert.assertEquals;

public class EntityTypeUtilsTest
{
	@DataProvider(name = "isReferenceTypeAttrProvider")
	public static Iterator<Object[]> isReferenceTypeAttrProvider()
	{
		List<Object[]> dataList = Lists.newArrayList();
		for (AttributeType attrType : AttributeType.values())
		{
			Attribute attr = mock(Attribute.class);
			when(attr.getDataType()).thenReturn(attrType);
			when(attr.toString()).thenReturn("attr_" + attrType.toString());

			boolean isRefAttr =
					attrType == CATEGORICAL || attrType == CATEGORICAL_MREF || attrType == FILE || attrType == MREF
							|| attrType == ONE_TO_MANY || attrType == XREF;
			dataList.add(new Object[] { attr, isRefAttr });
		}
		return dataList.iterator();
	}

	@Test(dataProvider = "isReferenceTypeAttrProvider")
	public void isReferenceTypeAttr(Attribute attr, boolean isRefAttr)
	{
		assertEquals(EntityTypeUtils.isReferenceType(attr), isRefAttr);
	}

	@DataProvider(name = "isReferenceTypeAttrTypeProvider")
	public static Iterator<Object[]> isReferenceTypeAttrTypeProvider()
	{
		List<Object[]> dataList = Lists.newArrayList();
		for (AttributeType attrType : AttributeType.values())
		{
			boolean isRefAttr =
					attrType == CATEGORICAL || attrType == CATEGORICAL_MREF || attrType == FILE || attrType == MREF
							|| attrType == ONE_TO_MANY || attrType == XREF;
			dataList.add(new Object[] { attrType, isRefAttr });
		}
		return dataList.iterator();
	}

	@Test(dataProvider = "isReferenceTypeAttrTypeProvider")
	public void isReferenceTypeAttrType(AttributeType attrType, boolean isRefAttrType)
	{
		assertEquals(EntityTypeUtils.isReferenceType(attrType), isRefAttrType);
	}

	@DataProvider(name = "isMultipleReferenceTypeProvider")
	public static Iterator<Object[]> isMultipleReferenceTypeProvider()
	{
		List<Object[]> dataList = Lists.newArrayList();
		for (AttributeType attrType : AttributeType.values())
		{
			Attribute attr = mock(Attribute.class);
			when(attr.getDataType()).thenReturn(attrType);
			when(attr.toString()).thenReturn("attr_" + attrType.toString());

			boolean isMultipleRefAttr = attrType == CATEGORICAL_MREF || attrType == MREF || attrType == ONE_TO_MANY;
			dataList.add(new Object[] { attr, isMultipleRefAttr });
		}
		return dataList.iterator();
	}

	@Test(dataProvider = "isMultipleReferenceTypeProvider")
	public void isMultipleReferenceType(Attribute attr, boolean isMultipleRefAttr)
	{
		assertEquals(EntityTypeUtils.isMultipleReferenceType(attr), isMultipleRefAttr);
	}

	@Test
	public void getAttributeNames()
	{
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		assertEquals(newArrayList(EntityTypeUtils.getAttributeNames(asList(attr0, attr1))), asList("attr0", "attr1"));
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
