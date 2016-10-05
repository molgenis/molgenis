package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Array;
import java.sql.ResultSet;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import java.sql.ResultSet;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
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
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getAtomicAttributes()).thenReturn(singleton(oneToManyAttr));
		ResultSet rs = mock(ResultSet.class);
		Array oneToManyArray = mock(Array.class);
		when(oneToManyArray.getArray()).thenReturn(new String[][] { { "1", "id0" }, { "0", "id1" } });
		when(rs.getArray(oneToManyAttrName)).thenReturn(oneToManyArray);
		int rowNum = 0;

		Entity entity = mock(Entity.class);
		Fetch fetch = null;
		when(entityManager.create(entityMeta, fetch)).thenReturn(entity);
		Entity refEntity1 = mock(Entity.class);
		Entity refEntity0 = mock(Entity.class);
		when(entityManager.getReferences(refEntityMeta, newArrayList("id1", "id0")))
				.thenReturn(newArrayList(refEntity1, refEntity0));
		assertEquals(postgreSqlEntityFactory.createRowMapper(entityMeta, null).mapRow(rs, rowNum), entity);
		verify(entity).set(oneToManyAttrName, newArrayList(refEntity1, refEntity0));
	}

	@Test
	public void createRowMapperXref() throws Exception
	{
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);

		String xrefAttr = "xrefAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getName()).thenReturn(xrefAttr);
		when(oneToManyAttr.getDataType()).thenReturn(XREF);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getAtomicAttributes()).thenReturn(singleton(oneToManyAttr));
		ResultSet rs = mock(ResultSet.class);
		when(rs.getString(xrefAttr)).thenReturn("id0");
		int rowNum = 0;

		Entity entity = mock(Entity.class);
		Fetch fetch = null;
		//noinspection ConstantConditions
		when(entityManager.create(entityMeta, fetch)).thenReturn(entity);
		Entity refEntity = mock(Entity.class);
		when(entityManager.getReference(refEntityMeta, "id0")).thenReturn(refEntity);
		assertEquals(postgreSqlEntityFactory.createRowMapper(entityMeta, null).mapRow(rs, rowNum), entity);
		verify(entity).set(xrefAttr, refEntity);
	}
}