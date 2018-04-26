package org.molgenis.data.populate;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityReferenceCreator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.*;

public class DefaultValuePopulatorTest
{
	private static Entity entity1;
	private static Entity entityA;
	private static Entity entityB;

	private DefaultValuePopulator defaultValuePopulator;

	@BeforeClass
	public static void setUpBeforeClass()
	{
		entity1 = mock(Entity.class);
		entityA = mock(Entity.class);
		entityB = mock(Entity.class);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		EntityReferenceCreator entityReferenceCreator = mock(EntityReferenceCreator.class);
		when(entityReferenceCreator.getReference(any(EntityType.class), eq(1))).thenReturn(entity1);
		when(entityReferenceCreator.getReference(any(EntityType.class), eq("a"))).thenReturn(entityA);
		when(entityReferenceCreator.getReference(any(EntityType.class), eq("b"))).thenReturn(entityB);
		when(entityReferenceCreator.getReferences(any(EntityType.class), eq(Arrays.asList("a", "b")))).thenReturn(
				asList(entityA, entityB));
		this.defaultValuePopulator = new DefaultValuePopulator(entityReferenceCreator);
	}

	@DataProvider(name = "testPopulateProvider")
	public static Iterator<Object[]> testPopulateProvider() throws ParseException
	{
		List<Object[]> populationData = new ArrayList<>(20);
		populationData.add(new Object[] { createEntity(BOOL, "true"), true });
		populationData.add(new Object[] { createEntity(BOOL, "false"), false });
		populationData.add(new Object[] { createEntity(CATEGORICAL, "1"), entity1 });
		populationData.add(new Object[] { createEntity(CATEGORICAL_MREF, "a,b"), asList(entityA, entityB) });
		populationData.add(new Object[] { createEntity(DATE, "2016-11-30"), LocalDate.parse("2016-11-30") });
		populationData.add(new Object[] { createEntity(DATE_TIME, "2016-10-10T12:00:10+0000"),
				Instant.parse("2016-10-10T12:00:10Z") });
		populationData.add(new Object[] { createEntity(DECIMAL, "1.23"), 1.23 });
		populationData.add(new Object[] { createEntity(EMAIL, "mail@molgenis.org"), "mail@molgenis.org" });
		populationData.add(new Object[] { createEntity(ENUM, "enum0"), "enum0" });
		populationData.add(new Object[] { createEntity(FILE, "1"), entity1 });
		populationData.add(new Object[] { createEntity(HTML, "<h1>text</h1>"), "<h1>text</h1>" });
		populationData.add(new Object[] { createEntity(HYPERLINK, "http://test.nl/"), "http://test.nl/" });
		populationData.add(new Object[] { createEntity(INT, "123"), 123 });
		populationData.add(new Object[] { createEntity(LONG, "1099511627776"), 1099511627776L });
		populationData.add(new Object[] { createEntity(MREF, "a,b"), asList(entityA, entityB) });
		populationData.add(new Object[] { createEntity(ONE_TO_MANY, "a,b"), asList(entityA, entityB) });
		populationData.add(new Object[] { createEntity(SCRIPT, "script"), "script" });
		populationData.add(new Object[] { createEntity(STRING, "str"), "str" });
		populationData.add(new Object[] { createEntity(TEXT, "text"), "text" });
		populationData.add(new Object[] { createEntity(XREF, "1"), entity1 });
		return populationData.iterator();
	}

	private static Entity createEntity(AttributeType attrType, String defaultValue)
	{
		EntityType entityType = mock(EntityType.class);
		Attribute attr = mock(Attribute.class);
		when(attr.getName()).thenReturn("attr");
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.hasDefaultValue()).thenReturn(true);
		when(attr.getDefaultValue()).thenReturn(defaultValue);
		when(entityType.getAllAttributes()).thenReturn(singleton(attr));
		if (attrType == CATEGORICAL || attrType == XREF || attrType == FILE)
		{
			Attribute intIdAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
			EntityType refEntityType = when(mock(EntityType.class).getIdAttribute()).thenReturn(intIdAttr).getMock();
			when(attr.getRefEntity()).thenReturn(refEntityType);
		}
		else if (attrType == CATEGORICAL_MREF || attrType == MREF || attrType == ONE_TO_MANY)
		{
			Attribute intStrAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
			EntityType refEntityType = when(mock(EntityType.class).getIdAttribute()).thenReturn(intStrAttr).getMock();
			when(attr.getRefEntity()).thenReturn(refEntityType);
		}

		Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
		when(entity.toString()).thenReturn(attrType.toString());
		return entity;
	}

	@Test(dataProvider = "testPopulateProvider")
	public void testPopulate(Entity entity, Object expectedValue) throws Exception
	{
		defaultValuePopulator.populate(entity);
		verify(entity).set("attr", expectedValue);
	}
}