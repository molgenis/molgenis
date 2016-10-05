package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
	public void createRowMapperXref() throws Exception
	{
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);

		String xrefAttr = "xrefAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getName()).thenReturn(xrefAttr);
		when(oneToManyAttr.getDataType()).thenReturn(XREF);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityType);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getAtomicAttributes()).thenReturn(singleton(oneToManyAttr));
		ResultSet rs = mock(ResultSet.class);
		when(rs.getString(xrefAttr)).thenReturn("id0");
		int rowNum = 0;

		Entity entity = mock(Entity.class);
		Fetch fetch = null;
		//noinspection ConstantConditions
		when(entityManager.create(entityType, fetch)).thenReturn(entity);
		Entity refEntity = mock(Entity.class);
		when(entityManager.getReference(refEntityType, "id0")).thenReturn(refEntity);
		assertEquals(postgreSqlEntityFactory.createRowMapper(entityType, null).mapRow(rs, rowNum), entity);
		verify(entity).set(xrefAttr, refEntity);
	}
}