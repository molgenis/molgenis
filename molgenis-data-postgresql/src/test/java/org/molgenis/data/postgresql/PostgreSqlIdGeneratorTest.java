package org.molgenis.data.postgresql;

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

public class PostgreSqlIdGeneratorTest
{
	private PostgreSqlIdGenerator postgreSqlIdGenerator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		postgreSqlIdGenerator = new PostgreSqlIdGenerator(32);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testTableIdGenerator()
	{
		new PostgreSqlIdGenerator(5);
	}

	@DataProvider(name = "generateIdEntityTypeProvider")
	public static Iterator<Object[]> generateIdEntityTypeProvider()
	{
		List<Object[]> dataList = new ArrayList<>(4);
		dataList.add(new Object[] { "myEntity", "myEntity#061f7aef" });
		dataList.add(new Object[] { "my_Entity", "my_Entity#4581e195" });
		dataList.add(new Object[] { "my|En%ti-ty/", "myEntity#7152ce38" });
		dataList.add(new Object[] { "myEntitymyEntitymyEntitymyEntity", "myEntitymyEntitymyEntit#6e9381ec" });
		return dataList.iterator();
	}

	@Test(dataProvider = "generateIdEntityTypeProvider")
	public void testGenerateIdEntityType(String entityTypeId, String expectedId)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		String id = postgreSqlIdGenerator.generateId(entityType);
		assertEquals(id, expectedId);
	}

	@DataProvider(name = "generateIdAttributeTypeProvider")
	public static Iterator<Object[]> generateIdAttributeProvider()
	{
		List<Object[]> dataList = new ArrayList<>(4);
		dataList.add(new Object[] { "abcdef", "myAttr", "myAttr" });
		dataList.add(new Object[] { "fabcde", "myAttr", "myAttr" });
		dataList.add(new Object[] { "fabcde", "my_Attr", "my_Attr" });
		dataList.add(new Object[] { "fabcde", "m,y^At&t.r", "myAttr#9c62ca2c" });
		dataList.add(
				new Object[] { "fabcde", "myAttrmyAttrmyAttrmyAttrmyAttrmyAttr", "myAttrmyAttrmyAttrmyAtt#9c62ca2c" });
		return dataList.iterator();
	}

	@Test(dataProvider = "generateIdAttributeTypeProvider")
	public void testGenerateIdAttribute(String attrId, String attrName, String expectedId)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn(attrId);
		when(attr.getName()).thenReturn(attrName);
		String id = postgreSqlIdGenerator.generateId(attr);
		assertEquals(id, expectedId);
	}
}