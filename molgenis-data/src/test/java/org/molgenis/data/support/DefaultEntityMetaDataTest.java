package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DefaultEntityMetaDataTest
{
	@Test
	public void testCopyConstructorPreservesIdAttribute()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("name");
		emd.addAttribute("id").setIdAttribute(true);

		DefaultEntityMetaData emdCopy = new DefaultEntityMetaData(emd);
		Assert.assertEquals(emdCopy.getIdAttribute().getName(), "id");
	}

	// regression test for https://github.com/molgenis/molgenis/issues/3665
	@Test
	public void testExtendsEntityMetaDataMissingIdAttribute()
	{
		DefaultEntityMetaData extendsEntityMeta = new DefaultEntityMetaData("entity");
		extendsEntityMeta.addAttribute("attr");

		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		entityMeta.setExtends(extendsEntityMeta);
		DefaultAttributeMetaData idAttr = entityMeta.addAttribute("id").setIdAttribute(true);
		assertEquals(idAttr, entityMeta.getIdAttribute());
	}

	@Test
	public void DefaultEntityMetaDataEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.setAbstract(true);
		entityMetaData.setDescription("description");
		entityMetaData.setIdAttribute("id");
		entityMetaData.setLabel("label");
		entityMetaData.setLabelAttribute("labelAttribute");
		entityMetaData.addAttribute("labelAttribute").setDescription("label attribute");
		entityMetaData.addAttribute("id").setDescription("id attribute");
		assertEquals(new DefaultEntityMetaData(entityMetaData), entityMetaData);
	}

	@Test
	public void getAttributesExtends()
	{
		// Verify that in case EntityMetaData extends other EntityMetaData only the attributes of this EntityMetaData
		// are returned.
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		DefaultAttributeMetaData attr = entityMetaData.addAttribute("attr");
		DefaultEntityMetaData baseEntityMetaData = new DefaultEntityMetaData("baseEntity");
		DefaultAttributeMetaData baseAttr = baseEntityMetaData.addAttribute("baseAttr");
		entityMetaData.setExtends(baseEntityMetaData);
		List<AttributeMetaData> attrs = entityMetaData.getAttributes();
		assertEquals(2, attrs.size());
		assertTrue(attrs.contains(attr));
		assertTrue(attrs.contains(baseAttr));
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
}
