package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Fetch;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PartialEntityTest
{
	private PartialEntity partialEntity;
	private Entity originalEntity;
	private Entity decoratedEntity;
	private Fetch fetch;
	private EntityManager entityManager;
	private DefaultEntityMetaData meta;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		meta = new DefaultEntityMetaData("entity");
		meta.addAttribute("id", ROLE_ID);

		originalEntity = mock(Entity.class);

		decoratedEntity = mock(Entity.class);
		when(decoratedEntity.getEntityMetaData()).thenReturn(meta);
		when(decoratedEntity.getIdValue()).thenReturn("id");

		fetch = new Fetch().field("id");
		entityManager = mock(EntityManager.class);
		when(entityManager.getReference(meta, "id")).thenReturn(originalEntity);
		partialEntity = new PartialEntity(decoratedEntity, fetch, entityManager);
	}

	@Test
	public void get()
	{
		partialEntity.get("id");
		verify(decoratedEntity, times(1)).get("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getNotInFetch()
	{
		partialEntity.get("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getBoolean()
	{
		partialEntity.getBoolean("id");
		verify(decoratedEntity, times(1)).getBoolean("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getBooleanNotInFetch()
	{
		partialEntity.getBoolean("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getDate()
	{
		partialEntity.getDate("id");
		verify(decoratedEntity, times(1)).getDate("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getDateNotInFetch()
	{
		partialEntity.getDate("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getDouble()
	{
		partialEntity.getDouble("id");
		verify(decoratedEntity, times(1)).getDouble("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getDoubleNotInFetch()
	{
		partialEntity.getDouble("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getEntitiesString()
	{
		partialEntity.getEntities("id");
		verify(decoratedEntity, times(1)).getEntities("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getEntitiesStringNotInFetch()
	{
		partialEntity.getEntities("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getEntitiesStringClassE()
	{
		partialEntity.getEntities("id", Entity.class);
		verify(decoratedEntity, times(1)).getEntities("id", Entity.class);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getEntitiesStringClassENotInFetch()
	{
		partialEntity.getEntities("label", Entity.class);
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getEntityString()
	{
		partialEntity.getEntity("id");
		verify(decoratedEntity, times(1)).getEntity("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getEntityStringNotInFetch()
	{
		partialEntity.getEntity("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getEntityStringClassE()
	{
		partialEntity.getEntity("id", Entity.class);
		verify(decoratedEntity, times(1)).getEntity("id", Entity.class);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getEntityStringClassENotInFetch()
	{
		partialEntity.getEntity("label", Entity.class);
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getInt()
	{
		partialEntity.getInt("id");
		verify(decoratedEntity, times(1)).getInt("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getIntNotInFetch()
	{
		partialEntity.getInt("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getIntList()
	{
		partialEntity.getIntList("id");
		verify(decoratedEntity, times(1)).getIntList("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getIntListNotInFetch()
	{
		partialEntity.getIntList("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getList()
	{
		partialEntity.getList("id");
		verify(decoratedEntity, times(1)).getList("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getListNotInFetch()
	{
		partialEntity.getList("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getLong()
	{
		partialEntity.getLong("id");
		verify(decoratedEntity, times(1)).getLong("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getLongNotInFetch()
	{
		partialEntity.getLong("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getString()
	{
		partialEntity.getString("id");
		verify(decoratedEntity, times(1)).getString("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getStringNotInFetch()
	{
		partialEntity.getString("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getTimestamp()
	{
		partialEntity.getTimestamp("id");
		verify(decoratedEntity, times(1)).getTimestamp("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getTimestampNotInFetch()
	{
		partialEntity.getTimestamp("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void getUtilDate()
	{
		partialEntity.getUtilDate("id");
		verify(decoratedEntity, times(1)).getUtilDate("id");
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getUtilDateNotInFetch()
	{
		partialEntity.getUtilDate("label");
		verify(entityManager, times(1)).getReference(meta, "id");
	}

	@Test
	public void setStringObject()
	{
		Object obj = mock(Object.class);
		partialEntity.set("test", obj);
		verify(decoratedEntity, times(1)).set("test", obj);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void setEntity()
	{
		Entity e = mock(Entity.class);
		partialEntity.set(e);
		verify(decoratedEntity, times(1)).set(e);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getAttributeNames()
	{
		partialEntity.getAttributeNames();
		verify(decoratedEntity, times(1)).getAttributeNames();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getEntityMetaData()
	{
		partialEntity.getEntityMetaData();
		verify(decoratedEntity, times(1)).getEntityMetaData();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getIdValue()
	{
		partialEntity.getIdValue();
		verify(decoratedEntity, times(1)).getIdValue();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getLabelValue()
	{
		partialEntity.getLabelValue();
		verify(decoratedEntity, times(1)).getLabelValue();
		verifyZeroInteractions(entityManager);
	}
}
