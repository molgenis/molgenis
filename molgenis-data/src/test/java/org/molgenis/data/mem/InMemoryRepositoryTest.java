package org.molgenis.data.mem;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class InMemoryRepositoryTest
{
	private EntityMetaData entityMeta;
	private InMemoryRepository inMemoryRepository;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		AttributeMetaData labelAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("label").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAttribute("label")).thenReturn(labelAttr);
		inMemoryRepository = new InMemoryRepository(entityMeta);
	}

	@Test
	public void addStream()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.get("id")).thenReturn("id0");
		when(entity0.getString("id")).thenReturn("id0");
		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.get("id")).thenReturn("id1");
		when(entity1.getString("id")).thenReturn("id1");
		Stream<Entity> entities = Stream.of(entity0, entity1);
		assertEquals(inMemoryRepository.add(entities), Integer.valueOf(2));
	}

	@Test
	public void deleteStream()
	{
		// add two
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.get("id")).thenReturn("id0");
		when(entity0.getString("id")).thenReturn("id0");
		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.get("id")).thenReturn("id1");
		when(entity1.getString("id")).thenReturn("id1");
		Stream<Entity> entities = Stream.of(entity0, entity1);
		inMemoryRepository.add(entities);

		// delete one
		inMemoryRepository.delete(Stream.of(entity0));

		// get all
		assertEquals(Lists.newArrayList(inMemoryRepository.iterator()), Arrays.asList(entity1));
	}

	@Test
	public void updateStream()
	{
		// add two
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.get("id")).thenReturn("id0");
		when(entity0.getString("id")).thenReturn("id0");
		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.get("id")).thenReturn("id1");
		when(entity1.getString("id")).thenReturn("id1");
		Stream<Entity> entities = Stream.of(entity0, entity1);
		inMemoryRepository.add(entities);

		// update two
		entity0.set("label", "label0");
		entity1.set("label", "label1");
		inMemoryRepository.update(Stream.of(entity0));

		// get all
		assertEquals(Lists.newArrayList(inMemoryRepository.iterator()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAll() throws IOException
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute("id").setIdAttribute(true);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMetaData);
		try
		{
			Entity entity = new MapEntity(entityMetaData);
			entity.set("id", "0");
			inMemoryRepository.add(entity);
			assertEquals(inMemoryRepository.findAll(new QueryImpl()), Arrays.asList(entity));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void findOneObjectFetch() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMeta);
		try
		{
			Object id = Integer.valueOf(0);
			Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
			inMemoryRepository.add(entity);
			Fetch fetch = new Fetch();
			assertEquals(inMemoryRepository.findOne(id, fetch), entity);
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void findOneObjectFetchEntityNull() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMeta);
		try
		{
			Object id = Integer.valueOf(0);
			Fetch fetch = new Fetch();
			assertNull(inMemoryRepository.findOne(id, fetch));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void findAllIterable() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMeta);
		try
		{
			Object id0 = Integer.valueOf(0);
			Entity entity0 = when(mock(Entity.class).get(idAttrName)).thenReturn(id0).getMock();
			Object id1 = Integer.valueOf(1);
			Entity entity1 = when(mock(Entity.class).get(idAttrName)).thenReturn(id1).getMock();
			inMemoryRepository.add(entity0);
			inMemoryRepository.add(entity1);
			Iterable<Entity> entities = inMemoryRepository.findAll(Arrays.asList(id0, id1));
			assertEquals(Lists.newArrayList(entities), Arrays.asList(entity0, entity1));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void findAllIterableFetch() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMeta);
		try
		{
			Object id0 = Integer.valueOf(0);
			Entity entity0 = when(mock(Entity.class).get(idAttrName)).thenReturn(id0).getMock();
			Object id1 = Integer.valueOf(1);
			Entity entity1 = when(mock(Entity.class).get(idAttrName)).thenReturn(id1).getMock();
			inMemoryRepository.add(entity0);
			inMemoryRepository.add(entity1);
			Fetch fetch = new Fetch();
			Iterable<Entity> entities = inMemoryRepository.findAll(Arrays.asList(id0, id1), fetch);
			assertEquals(Lists.newArrayList(entities), Arrays.asList(entity0, entity1));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}
}
