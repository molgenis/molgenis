package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.PackageImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class DefaultEntityMetaDataTest
{
	@Test
	public void getName()
	{
		PackageImpl package_ = new PackageImpl("packageName");
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity", package_);
		assertEquals(entityMeta.getName(), "packageName" + Package.PACKAGE_SEPARATOR + "entity");

		PackageImpl parentPackage = new PackageImpl("parent");
		package_.setParent(parentPackage);
		assertEquals(entityMeta.getName(),
				"parent" + Package.PACKAGE_SEPARATOR + "packageName" + Package.PACKAGE_SEPARATOR + "entity");

		PackageImpl otherPackage = new PackageImpl("otherPackageName");
		entityMeta.setPackage(otherPackage);
		assertEquals(entityMeta.getName(), "otherPackageName" + Package.PACKAGE_SEPARATOR + "entity");

		entityMeta.setPackage(null);
		assertEquals(entityMeta.getName(), "entity");
	}

	@Test
	public void defaultEntityMetaDataStringEntityMetaData()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttributeMetaData(attr0);
		entityMeta.addAttributeMetaData(attr1);

		DefaultEntityMetaData baseEntityMetaData = new DefaultEntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttributeMetaData(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEntityMetaEquals(new DefaultEntityMetaData(entityMeta), entityMeta);
	}

	@Test
	public void testCopyConstructorPreservesIdAttribute()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("name");
		emd.addAttribute("id", ROLE_ID);

		DefaultEntityMetaData emdCopy = new DefaultEntityMetaData(emd);
		Assert.assertEquals(emdCopy.getIdAttribute().getName(), "id");
	}

	@Test
	public void testCopyConstructorPreservesName()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("name");
		emd.setPackage(new PackageImpl("test_package"));

		DefaultEntityMetaData emdCopy = new DefaultEntityMetaData(emd);
		assertEquals(emdCopy.getName(), "test_package_name");
		assertEquals(emdCopy.getSimpleName(), "name");
	}

	// regression test for https://github.com/molgenis/molgenis/issues/3665
	@Test
	public void testExtendsEntityMetaDataMissingIdAttribute()
	{
		DefaultEntityMetaData extendsEntityMeta = new DefaultEntityMetaData("entity");
		extendsEntityMeta.addAttribute("attr");

		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		entityMeta.setExtends(extendsEntityMeta);
		DefaultAttributeMetaData idAttr = entityMeta.addAttribute("id", ROLE_ID);
		assertEquals(entityMeta.getIdAttribute(), idAttr);
	}

	@Test
	public void DefaultEntityMetaDataEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.setAbstract(true);
		entityMetaData.setDescription("description");
		entityMetaData.setLabel("label");
		entityMetaData.addAttribute("labelAttribute", ROLE_LABEL).setDescription("label attribute");
		entityMetaData.addAttribute("id", ROLE_ID).setDescription("id attribute");
		assertEquals(new DefaultEntityMetaData(entityMetaData), entityMetaData);
	}

	@Test
	public void getAttributes()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttributeMetaData(attr0);
		entityMeta.addAttributeMetaData(attr1);

		assertEquals(Lists.newArrayList(entityMeta.getAttributes()), Arrays.asList(attr0, attr1));
	}

	@Test
	public void getAttributesExtends()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttributeMetaData(attr0);
		entityMeta.addAttributeMetaData(attr1);

		DefaultEntityMetaData baseEntityMetaData = new DefaultEntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttributeMetaData(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEquals(Lists.newArrayList(entityMeta.getAttributes()), Arrays.asList(baseAttr0, attr0, attr1));
	}

	@Test
	public void getOwnAttributes()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttributeMetaData(attr0);
		entityMeta.addAttributeMetaData(attr1);

		assertEquals(Lists.newArrayList(entityMeta.getOwnAttributes()), Arrays.asList(attr0, attr1));
	}

	@Test
	public void getOwnAttributesExtends()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttributeMetaData(attr0);
		entityMeta.addAttributeMetaData(attr1);

		DefaultEntityMetaData baseEntityMetaData = new DefaultEntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttributeMetaData(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEquals(Lists.newArrayList(entityMeta.getOwnAttributes()), Arrays.asList(attr0, attr1));
	}

	@Test
	public void hasAttributeWithExpressionTrue()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData attrWithExpression = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1")
				.getMock();
		when(attrWithExpression.getDataType()).thenReturn(STRING);
		when(attrWithExpression.getExpression()).thenReturn("expression");
		entityMeta.addAttributeMetaData(attr);
		entityMeta.addAttributeMetaData(attrWithExpression);
		assertTrue(entityMeta.hasAttributeWithExpression());
	}

	@Test
	public void hasAttributeWithExpressionFalse()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		entityMeta.addAttributeMetaData(attr);
		assertFalse(entityMeta.hasAttributeWithExpression());
	}

	@Test
	public void getAtomicAttributes()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		entityMeta.addAttributeMetaData(attr0);
		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(attr0));
	}

	@Test
	public void getAtomicAttributesCompound()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttributeMetaData(attr0);
		entityMeta.addAttributeMetaData(attr1);

		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(attr0, attr1a, attr1b));
	}

	@Test
	public void getAtomicAttributesExtends()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		entityMeta.addAttributeMetaData(attr0);

		DefaultEntityMetaData baseEntityMetaData = new DefaultEntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttributeMetaData(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(baseAttr0, attr0));
	}

	@Test
	public void getOwnAtomicAttributes()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		entityMeta.addAttributeMetaData(attr0);
		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0));
	}

	@Test
	public void getOwnAtomicAttributesCompound()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttributeMetaData(attr0);
		entityMeta.addAttributeMetaData(attr1);

		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0, attr1a, attr1b));
	}

	@Test
	public void getOwnAtomicAttributesExtends()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		entityMeta.addAttributeMetaData(attr0);

		DefaultEntityMetaData baseEntityMetaData = new DefaultEntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttributeMetaData(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0));
	}

	@Test
	public void addAttributeMetaDataIdAttr()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		DefaultAttributeMetaData idAttr = entityMeta.addAttribute("idAttr", ROLE_ID);
		assertEquals(entityMeta.getIdAttribute(), idAttr);
	}

	@Test
	public void addAttributeMetaDataLabelAttr()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		DefaultAttributeMetaData labelAttr = entityMeta.addAttribute("labelAttr", ROLE_LABEL);
		assertEquals(entityMeta.getLabelAttribute(), labelAttr);
	}

	@Test
	public void addAttributeMetaDataLookupAttr()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		String lookupAttrName = "lookupAttr";
		DefaultAttributeMetaData lookupAttr = entityMeta.addAttribute(lookupAttrName, ROLE_LOOKUP);
		assertEquals(entityMeta.getLookupAttribute(lookupAttrName), lookupAttr);
	}

	private void assertEntityMetaEquals(EntityMetaData actualEntityMeta, EntityMetaData expectedEntityMeta)
	{
		assertEquals(actualEntityMeta.getSimpleName(), expectedEntityMeta.getSimpleName());
		assertEquals(actualEntityMeta.getEntityClass(), expectedEntityMeta.getEntityClass());
		assertEquals(actualEntityMeta.getPackage(), expectedEntityMeta.getPackage());
		assertEquals(actualEntityMeta.getLabel(), expectedEntityMeta.getLabel());
		assertEquals(actualEntityMeta.isAbstract(), expectedEntityMeta.isAbstract());
		assertEquals(actualEntityMeta.getDescription(), expectedEntityMeta.getDescription());
		EntityMetaData actualExtends = actualEntityMeta.getExtends();
		EntityMetaData expectedExtends = expectedEntityMeta.getExtends();
		if (actualExtends != null && expectedExtends != null)
		{
			assertEntityMetaEquals(actualExtends, expectedExtends);
		}
		assertEquals(actualEntityMeta.getBackend(), expectedEntityMeta.getBackend());
		assertEquals(Lists.newArrayList(actualEntityMeta.getAtomicAttributes()),
				Lists.newArrayList(expectedEntityMeta.getAtomicAttributes()));
		assertEquals(Lists.newArrayList(actualEntityMeta.getOwnAtomicAttributes()),
				Lists.newArrayList(expectedEntityMeta.getOwnAtomicAttributes()));
		assertEquals(Lists.newArrayList(actualEntityMeta.getOwnAttributes()),
				Lists.newArrayList(expectedEntityMeta.getOwnAttributes()));
		assertEquals(actualEntityMeta.getIdAttribute(), expectedEntityMeta.getIdAttribute());
		assertEquals(actualEntityMeta.getLabelAttribute(), expectedEntityMeta.getLabelAttribute());
	}
}
