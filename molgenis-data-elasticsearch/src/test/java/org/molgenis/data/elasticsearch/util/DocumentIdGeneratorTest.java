package org.molgenis.data.elasticsearch.util;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class DocumentIdGeneratorTest
{
	private DocumentIdGenerator documentIdGenerator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		documentIdGenerator = new DocumentIdGenerator();
	}

	@DataProvider(name = "generateIdEntityTypeProvider")
	public static Iterator<Object[]> generateIdEntityTypeProvider()
	{
		List<Object[]> dataList = new ArrayList<>(4);
		dataList.add(new Object[] { "012345", "myEntity", "myEntity_0f6b6fb8" });
		dataList.add(new Object[] { "543210", "myEntity", "myEntity_54782194" });
		dataList.add(new Object[] { "012345", "_my|En%ti-ty/", "myEntity_0f6b6fb8" });
		dataList.add(new Object[] { "012345", "myEntitymyEntitymyEntitymyEntity", "myEntitymyEntitymyEntit_0f6b6fb8" });
		return dataList.iterator();
	}

	@Test(dataProvider = "generateIdEntityTypeProvider")
	public void testGenerateIdEntityType(String entityTypeId, String entityTypeName, String expectedId)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getIdValue()).thenReturn(entityTypeId);
		when(entityType.getSimpleName()).thenReturn(entityTypeName);
		String id = documentIdGenerator.generateId(entityType);
		assertEquals(id, expectedId);
	}

	@DataProvider(name = "generateIdAttributeTypeProvider")
	public static Iterator<Object[]> generateIdAttributeProvider()
	{
		List<Object[]> dataList = new ArrayList<>(4);
		dataList.add(new Object[] { "012345", "abcdef", "myAttr", "myAttr_9b8532fc" });
		dataList.add(new Object[] { "543210", "abcdef", "myAttr", "myAttr_7700a7f7" });
		dataList.add(new Object[] { "012345", "fabcde", "myAttr", "myAttr_e8de769b" });
		dataList.add(new Object[] { "012345", "fabcde", "_m,y^At&t.r", "myAttr_e8de769b" });
		dataList.add(new Object[] { "012345", "fabcde", "myAttrmyAttrmyAttrmyAttrmyAttrmyAttr",
				"myAttrmyAttrmyAttrmyAtt_e8de769b" });
		return dataList.iterator();
	}

	@Test(dataProvider = "generateIdAttributeTypeProvider")
	public void testGenerateIdAttribute(String entityTypeId, String attrId, String attrName, String expectedId)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getIdValue()).thenReturn(entityTypeId);

		Attribute attr = mock(Attribute.class);
		when(attr.getEntity()).thenReturn(entityType);
		when(attr.getIdentifier()).thenReturn(attrId);
		when(attr.getName()).thenReturn(attrName);
		String id = documentIdGenerator.generateId(attr);
		assertEquals(id, expectedId);
	}
}