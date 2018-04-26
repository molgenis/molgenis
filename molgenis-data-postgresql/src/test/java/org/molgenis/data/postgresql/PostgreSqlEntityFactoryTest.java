package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Array;
import java.sql.ResultSet;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlEntityFactoryTest
{
	private PostgreSqlEntityFactory postgreSqlEntityFactory;
	private EntityManager entityManager;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityManager = mock(EntityManager.class);
		postgreSqlEntityFactory = new PostgreSqlEntityFactory(entityManager);
	}

	@Test
	public void createRowMapperOneToMany() throws Exception
	{
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityType refEntityMeta = mock(EntityType.class);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);

		String oneToManyAttrName = "oneToManyAttr";
		Attribute oneToManyAttr = mock(Attribute.class);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getAtomicAttributes()).thenReturn(singleton(oneToManyAttr));
		ResultSet rs = mock(ResultSet.class);
		Array oneToManyArray = mock(Array.class);
		when(oneToManyArray.getArray()).thenReturn(new String[] { "id0", "id1" });
		when(rs.getArray(oneToManyAttrName)).thenReturn(oneToManyArray);
		int rowNum = 0;

		Entity entity = mock(Entity.class);
		when(entityManager.createFetch(entityType, null)).thenReturn(entity);
		Entity refEntity1 = mock(Entity.class);
		Entity refEntity0 = mock(Entity.class);
		when(entityManager.getReferences(refEntityMeta, newArrayList("id0", "id1"))).thenReturn(
				newArrayList(refEntity0, refEntity1));
		assertEquals(postgreSqlEntityFactory.createRowMapper(entityType, null).mapRow(rs, rowNum), entity);
		verify(entity).set(oneToManyAttrName, newArrayList(refEntity0, refEntity1));
	}

	@Test
	public void createRowMapperOneToManyIntegerIds() throws Exception
	{
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(AttributeType.INT);

		EntityType refEntityMeta = mock(EntityType.class);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);

		String oneToManyAttrName = "oneToManyAttr";
		Attribute oneToManyAttr = mock(Attribute.class);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getAtomicAttributes()).thenReturn(singleton(oneToManyAttr));
		ResultSet rs = mock(ResultSet.class);
		Array oneToManyArray = mock(Array.class);
		when(oneToManyArray.getArray()).thenReturn(new Integer[] { 0, 1 });
		when(rs.getArray(oneToManyAttrName)).thenReturn(oneToManyArray);
		int rowNum = 0;

		Entity entity = mock(Entity.class);
		when(entityManager.createFetch(entityType, null)).thenReturn(entity);
		Entity refEntity1 = mock(Entity.class);
		Entity refEntity0 = mock(Entity.class);
		when(entityManager.getReferences(refEntityMeta, newArrayList(0, 1))).thenReturn(
				newArrayList(refEntity0, refEntity1));
		assertEquals(postgreSqlEntityFactory.createRowMapper(entityType, null).mapRow(rs, rowNum), entity);
		verify(entity).set(oneToManyAttrName, newArrayList(refEntity0, refEntity1));
	}

	@Test
	public void createRowMapperXref() throws Exception
	{
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);

		String xrefAttr = "xrefAttr";
		Attribute oneToManyAttr = mock(Attribute.class);
		when(oneToManyAttr.getName()).thenReturn(xrefAttr);
		when(oneToManyAttr.getDataType()).thenReturn(XREF);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityType);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getAtomicAttributes()).thenReturn(singleton(oneToManyAttr));
		ResultSet rs = mock(ResultSet.class);
		when(rs.getString(xrefAttr)).thenReturn("id0");
		int rowNum = 0;

		Entity entity = mock(Entity.class);
		when(entityManager.createFetch(entityType, null)).thenReturn(entity);
		Entity refEntity = mock(Entity.class);
		when(entityManager.getReference(refEntityType, "id0")).thenReturn(refEntity);
		assertEquals(postgreSqlEntityFactory.createRowMapper(entityType, null).mapRow(rs, rowNum), entity);
		verify(entity).set(xrefAttr, refEntity);
	}
}