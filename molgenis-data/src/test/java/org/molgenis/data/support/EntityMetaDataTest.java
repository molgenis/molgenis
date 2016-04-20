package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.Package;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class EntityMetaDataTest
{
	@Test
	public void getName()
	{
		Package package_ = new Package("packageName");
		EntityMetaData entityMeta = new EntityMetaData("entity", package_);
		assertEquals(entityMeta.getName(), "packageName" + Package.PACKAGE_SEPARATOR + "entity");

		Package parentPackage = new Package("parent");
		package_.setParent(parentPackage);
		assertEquals(entityMeta.getName(),
				"parent" + Package.PACKAGE_SEPARATOR + "packageName" + Package.PACKAGE_SEPARATOR + "entity");

		Package otherPackage = new Package("otherPackageName");
		entityMeta.setPackage(otherPackage);
		assertEquals(entityMeta.getName(), "otherPackageName" + Package.PACKAGE_SEPARATOR + "entity");

		entityMeta.setPackage(null);
		assertEquals(entityMeta.getName(), "entity");
	}

	@Test
	public void EntityMetaDataStringEntityMetaData()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttribute(attr0);
		entityMeta.addAttribute(attr1);

		EntityMetaData baseEntityMetaData = new EntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttribute(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEntityMetaEquals(new EntityMetaData(entityMeta), entityMeta);
	}

	@Test
	public void testCopyConstructorPreservesIdAttribute()
	{
		EntityMetaData emd = new EntityMetaData("name");
		emd.addAttribute("id", ROLE_ID);

		EntityMetaData emdCopy = new EntityMetaData(emd);
		Assert.assertEquals(emdCopy.getIdAttribute().getName(), "id");
	}

	@Test
	public void testCopyConstructorPreservesName()
	{
		EntityMetaData emd = new EntityMetaData("name");
		emd.setPackage(new Package("test_package"));

		EntityMetaData emdCopy = new EntityMetaData(emd);
		assertEquals(emdCopy.getName(), "test_package_name");
		assertEquals(emdCopy.getSimpleName(), "name");
	}

	// regression test for https://github.com/molgenis/molgenis/issues/3665
	@Test
	public void testExtendsEntityMetaDataMissingIdAttribute()
	{
		EntityMetaData extendsEntityMeta = new EntityMetaData("entity");
		extendsEntityMeta.addAttribute("attr");

		EntityMetaData entityMeta = new EntityMetaData("entity");
		entityMeta.setExtends(extendsEntityMeta);
		AttributeMetaData idAttr = entityMeta.addAttribute("id", ROLE_ID);
		assertEquals(entityMeta.getIdAttribute(), idAttr);
	}

	@Test
	public void EntityMetaDataEntityMetaData()
	{
		EntityMetaData entityMetaData = new EntityMetaData("entity");
		entityMetaData.setAbstract(true);
		entityMetaData.setDescription("description");
		entityMetaData.setLabel("label");
		entityMetaData.addAttribute("labelAttribute", ROLE_LABEL).setDescription("label attribute");
		entityMetaData.addAttribute("id", ROLE_ID).setDescription("id attribute");
		assertEquals(new EntityMetaData(entityMetaData), entityMetaData);
	}

	@Test
	public void getAttributes()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttribute(attr0);
		entityMeta.addAttribute(attr1);

		assertEquals(Lists.newArrayList(entityMeta.getAttributes()), Arrays.asList(attr0, attr1));
	}

	@Test
	public void getAttributesExtends()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttribute(attr0);
		entityMeta.addAttribute(attr1);

		EntityMetaData baseEntityMetaData = new EntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttribute(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEquals(Lists.newArrayList(entityMeta.getAttributes()), Arrays.asList(baseAttr0, attr0, attr1));
	}

	@Test
	public void getOwnAttributes()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttribute(attr0);
		entityMeta.addAttribute(attr1);

		assertEquals(Lists.newArrayList(entityMeta.getOwnAttributes()), Arrays.asList(attr0, attr1));
	}

	@Test
	public void getOwnAttributesExtends()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttribute(attr0);
		entityMeta.addAttribute(attr1);

		EntityMetaData baseEntityMetaData = new EntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttribute(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEquals(Lists.newArrayList(entityMeta.getOwnAttributes()), Arrays.asList(attr0, attr1));
	}

	@Test
	public void hasAttributeWithExpressionTrue()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData attrWithExpression = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1")
				.getMock();
		when(attrWithExpression.getDataType()).thenReturn(STRING);
		when(attrWithExpression.getExpression()).thenReturn("expression");
		entityMeta.addAttribute(attr);
		entityMeta.addAttribute(attrWithExpression);
		assertTrue(entityMeta.hasAttributeWithExpression());
	}

	@Test
	public void hasAttributeWithExpressionFalse()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		entityMeta.addAttribute(attr);
		assertFalse(entityMeta.hasAttributeWithExpression());
	}

	@Test
	public void getAtomicAttributes()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		entityMeta.addAttribute(attr0);
		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(attr0));
	}

	@Test
	public void getAtomicAttributesCompound()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttribute(attr0);
		entityMeta.addAttribute(attr1);

		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(attr0, attr1a, attr1b));
	}

	@Test
	public void getAtomicAttributesExtends()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		entityMeta.addAttribute(attr0);

		EntityMetaData baseEntityMetaData = new EntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttribute(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEquals(Lists.newArrayList(entityMeta.getAtomicAttributes()), Arrays.asList(baseAttr0, attr0));
	}

	@Test
	public void getOwnAtomicAttributes()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		entityMeta.addAttribute(attr0);
		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0));
	}

	@Test
	public void getOwnAtomicAttributesCompound()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData attr1a = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1a").getMock();
		when(attr1a.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1b = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1b").getMock();
		when(attr1b.getDataType()).thenReturn(STRING);
		when(attr1.getAttributeParts()).thenReturn(Arrays.asList(attr1a, attr1b));
		entityMeta.addAttribute(attr0);
		entityMeta.addAttribute(attr1);

		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0, attr1a, attr1b));
	}

	@Test
	public void getOwnAtomicAttributesExtends()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		entityMeta.addAttribute(attr0);

		EntityMetaData baseEntityMetaData = new EntityMetaData("baseEntity");
		AttributeMetaData baseAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("baseAttr0").getMock();
		when(baseAttr0.getDataType()).thenReturn(STRING);
		baseEntityMetaData.addAttribute(baseAttr0);

		entityMeta.setExtends(baseEntityMetaData);

		assertEquals(Lists.newArrayList(entityMeta.getOwnAtomicAttributes()), Arrays.asList(attr0));
	}

	@Test
	public void addAttributeIdAttr()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData idAttr = entityMeta.addAttribute("idAttr", ROLE_ID);
		assertEquals(entityMeta.getIdAttribute(), idAttr);
	}

	@Test
	public void addAttributeLabelAttr()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		AttributeMetaData labelAttr = entityMeta.addAttribute("labelAttr", ROLE_LABEL);
		assertEquals(entityMeta.getLabelAttribute(), labelAttr);
	}

	@Test
	public void addAttributeLookupAttr()
	{
		EntityMetaData entityMeta = new EntityMetaData("entity");
		String lookupAttrName = "lookupAttr";
		AttributeMetaData lookupAttr = entityMeta.addAttribute(lookupAttrName, ROLE_LOOKUP);
		assertEquals(entityMeta.getLookupAttribute(lookupAttrName), lookupAttr);
	}

	private void assertEntityMetaEquals(EntityMetaData actualEntityMeta, EntityMetaData expectedEntityMeta)
	{
		assertEquals(actualEntityMeta.getSimpleName(), expectedEntityMeta.getSimpleName());
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
