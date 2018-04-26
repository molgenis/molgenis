package org.molgenis.data.support;

import com.google.common.collect.Lists;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.testng.Assert.*;

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
	public void buildFullNamePackage()
	{
		Package package_ = when(mock(Package.class).getId()).thenReturn("my_first_package").getMock();
		assertEquals(EntityTypeUtils.buildFullName(package_, "simpleName"), "my_first_package_simpleName");
	}

	@Test
	public void buildFullNamePackageDefault()
	{
		Package defaultPackage = when(mock(Package.class).getId()).thenReturn(PACKAGE_DEFAULT).getMock();
		assertEquals(EntityTypeUtils.buildFullName(defaultPackage, "simpleName"), PACKAGE_DEFAULT + "_simpleName");
	}

	@Test
	public void buildFullNameNoPackage()
	{
		assertEquals(EntityTypeUtils.buildFullName(null, "simpleName"), "simpleName");
	}

	@Test
	public void isSystemEntityIfInSystemPackage()
	{
		EntityType entity = mock(EntityType.class);
		Package entityPackage = mock(Package.class);
		when(entity.getPackage()).thenReturn(entityPackage);
		when(entityPackage.getId()).thenReturn("sys");
		assertTrue(EntityTypeUtils.isSystemEntity(entity));
	}

	@Test
	public void isSystemEntityIfInSystemSubPackage()
	{
		EntityType entity = mock(EntityType.class);
		Package entityPackage = mock(Package.class);
		when(entity.getPackage()).thenReturn(entityPackage);
		when(entityPackage.getId()).thenReturn("not-system");
		when(entity.getId()).thenReturn("sys_foo_bar_Entity");
		assertTrue(EntityTypeUtils.isSystemEntity(entity));
	}

	@Test
	public void isSystemEntityNotASystemIfNotInSystemPackage()
	{
		EntityType entity = mock(EntityType.class);
		Package entityPackage = mock(Package.class);
		when(entity.getPackage()).thenReturn(entityPackage);
		when(entityPackage.getId()).thenReturn("not-system");
		when(entity.getId()).thenReturn("foo_bar_Entity");
		assertFalse(EntityTypeUtils.isSystemEntity(entity));
	}

	@Test
	public void isSystemEntityNotASystemEntityIfNotInPackage()
	{
		EntityType entity = mock(EntityType.class);
		when(entity.getPackage()).thenReturn(null);
		assertFalse(EntityTypeUtils.isSystemEntity(entity));
	}

	@Test
	public void testCreateFetchForReindexingIndexingDepth0()
	{
		EntityType entityType = createMockEntityType();
		when(entityType.getIndexingDepth()).thenReturn(0);
		Fetch expectedFetch = new Fetch().field("MyEntityTypeAttr").field("MyEntityTypeRefAttr");
		assertEquals(EntityTypeUtils.createFetchForReindexing(entityType), expectedFetch);
	}

	@Test
	public void testCreateFetchForReindexingIndexingDepth1()
	{
		EntityType entityType = createMockEntityType();
		when(entityType.getIndexingDepth()).thenReturn(1);
		Fetch expectedFetch = new Fetch().field("MyEntityTypeAttr")
										 .field("MyEntityTypeRefAttr", new Fetch().field("MyRefEntityTypeAttr")
																				  .field("MyRefEntityTypeRefAttr"));
		assertEquals(EntityTypeUtils.createFetchForReindexing(entityType), expectedFetch);
	}

	@Test
	public void testCreateFetchForReindexingIndexingDepth2()
	{
		EntityType entityType = createMockEntityType();
		when(entityType.getIndexingDepth()).thenReturn(2);
		Fetch expectedFetch = new Fetch().field("MyEntityTypeAttr")
										 .field("MyEntityTypeRefAttr", new Fetch().field("MyRefEntityTypeAttr")
																				  .field("MyRefEntityTypeRefAttr",
																						  new Fetch().field(
																								  "MyRefRefEntityTypeAttr")
																									 .field("MyRefRefEntityTypeRefAttr")));
		assertEquals(EntityTypeUtils.createFetchForReindexing(entityType), expectedFetch);
	}

	private EntityType createMockEntityType()
	{
		EntityType refRefEntityType = mock(EntityType.class);
		Attribute refRefEntityTypeAttr = when(mock(Attribute.class).getName()).thenReturn("MyRefRefEntityTypeAttr")
																			  .getMock();
		Attribute refRefEntityTypeRefAttr = when(mock(Attribute.class).getName()).thenReturn(
				"MyRefRefEntityTypeRefAttr").getMock();
		when(refRefEntityTypeRefAttr.getRefEntity()).thenReturn(refRefEntityType);
		when(refRefEntityType.getAtomicAttributes()).thenReturn(asList(refRefEntityTypeAttr, refRefEntityTypeRefAttr));

		EntityType refEntityType = mock(EntityType.class);
		Attribute refEntityTypeAttr = when(mock(Attribute.class).getName()).thenReturn("MyRefEntityTypeAttr").getMock();
		Attribute refEntityTypeRefAttr = when(mock(Attribute.class).getName()).thenReturn("MyRefEntityTypeRefAttr")
																			  .getMock();
		when(refEntityTypeRefAttr.getRefEntity()).thenReturn(refRefEntityType);
		when(refEntityType.getAtomicAttributes()).thenReturn(asList(refEntityTypeAttr, refEntityTypeRefAttr));

		EntityType entityType = mock(EntityType.class);
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("MyEntityTypeAttr").getMock();
		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("MyEntityTypeRefAttr").getMock();
		when(refAttr.getRefEntity()).thenReturn(refEntityType);
		when(entityType.getAtomicAttributes()).thenReturn(asList(attr, refAttr));

		return entityType;
	}
}
