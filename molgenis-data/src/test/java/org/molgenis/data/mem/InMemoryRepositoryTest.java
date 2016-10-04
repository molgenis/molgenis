package org.molgenis.data.mem;

import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class InMemoryRepositoryTest
{
	private EntityMetaData entityMeta;
	private InMemoryRepository inMemoryRepository;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		Attribute labelAttr = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
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
	public void findOneObjectFetch() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMeta);
		try
		{
			Object id = Integer.valueOf(0);
			Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
			inMemoryRepository.add(entity);
			Fetch fetch = new Fetch();
			assertEquals(inMemoryRepository.findOneById(id, fetch), entity);
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
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMeta);
		try
		{
			Object id = Integer.valueOf(0);
			Fetch fetch = new Fetch();
			assertNull(inMemoryRepository.findOneById(id, fetch));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void findAllStream() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
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
			List<Entity> entities = inMemoryRepository.findAll(Stream.of(id0, id1, "bogus"))
					.collect(Collectors.toList());
			assertEquals(Lists.newArrayList(entities), Arrays.asList(entity0, entity1));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void findAllStreamFetch() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
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
			List<Entity> entities = inMemoryRepository.findAll(Stream.of(id0, id1, "bogus"), fetch)
					.collect(Collectors.toList());
			assertEquals(Lists.newArrayList(entities), Arrays.asList(entity0, entity1));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void findAllAsStream() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
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
			List<Entity> entities = inMemoryRepository.findAll(new QueryImpl<Entity>()).collect(Collectors.toList());
			assertEquals(Lists.newArrayList(entities), Arrays.asList(entity0, entity1));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void findAllAsStreamSingleEqualsQuery() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMeta);
		try
		{
			Object id0 = Integer.valueOf(0);
			Entity entity0 = when(mock(Entity.class).get("attr")).thenReturn("a").getMock();
			when(entity0.get("id")).thenReturn(id0);
			Object id1 = Integer.valueOf(1);
			Entity entity1 = when(mock(Entity.class).get("attr")).thenReturn("a").getMock();
			when(entity1.get("id")).thenReturn(id1);
			Object id2 = Integer.valueOf(2);
			Entity entity2 = when(mock(Entity.class).get("attr")).thenReturn("b").getMock();
			when(entity2.get("id")).thenReturn(id2);
			inMemoryRepository.add(entity0);
			inMemoryRepository.add(entity1);
			inMemoryRepository.add(entity2);

			System.out.println(entity0.get(idAttrName));

			List<Entity> entities = inMemoryRepository.findAll(new QueryImpl<Entity>().eq("attr", "a"))
					.filter(Objects::nonNull).collect(Collectors.toList());
			assertEquals(Lists.newArrayList(entities), Arrays.asList(entity0, entity1));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void findAllAsStreamQueryWithContent() throws IOException
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMeta);
		try
		{
			inMemoryRepository.findAll(new QueryImpl<Entity>().eq("attr", "val").and().eq("attr2", "val"))
					.collect(Collectors.toList());
		}
		finally
		{
			inMemoryRepository.close();
		}
	}

	@Test
	public void streamFetch() throws IOException
	{
		String idAttrName = "id";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();
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

			Consumer<List<Entity>> consumer = mock(Consumer.class);
			inMemoryRepository.forEachBatched(fetch, consumer, 1000);
			verify(consumer).accept(Arrays.asList(entity0, entity1));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}
}
