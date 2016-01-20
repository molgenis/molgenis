package org.molgenis.data.elasticsearch;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbstractElasticsearchRepositoryTest
{
	private EntityMetaData entityMeta;
	private SearchService searchService;
	private AbstractElasticsearchRepository repository;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMeta = mock(EntityMetaData.class);
		searchService = mock(SearchService.class);
		repository = new AbstractElasticsearchRepository(searchService)
		{
			@Override
			public void rebuildIndex()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public EntityMetaData getEntityMetaData()
			{
				return entityMeta;
			}
		};
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(searchService.index(entities, entityMeta, IndexingMode.ADD)).thenReturn(123l);
		assertEquals(repository.add(entities), Integer.valueOf(123));
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		repository.delete(entities);
		verify(searchService, times(1)).delete(entities, entityMeta);
		verify(searchService, times(1)).refresh(entityMeta);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		when(searchService.index(captor.capture(), eq(entityMeta), eq(IndexingMode.UPDATE))).thenReturn(1l);
		repository.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
		verify(searchService, times(1)).refresh(entityMeta);
	}

	@Test
	public void findOne() throws IOException
	{
		try
		{
			Entity entity = mock(Entity.class);
			Object id = Integer.valueOf(0);
			Fetch fetch = new Fetch();
			when(searchService.get(id, entityMeta, fetch)).thenReturn(entity);
			assertEquals(repository.findOne(id, fetch), entity);
		}
		finally
		{
			repository.close();
		}
	}

	@Test
	public void findOneQuery() throws IOException
	{
		try
		{
			Entity entity = mock(Entity.class);
			Query q = mock(Query.class);
			when(searchService.search(q, entityMeta)).thenReturn(Arrays.asList(entity));
			assertEquals(repository.findOne(q), entity);
		}
		finally
		{
			repository.close();
		}
	}

	@Test
	public void findOneQueryNoResults() throws IOException
	{
		try
		{
			Query q = mock(Query.class);
			when(searchService.search(q, entityMeta)).thenReturn(Collections.emptyList());
			assertNull(repository.findOne(q));
		}
		finally
		{
			repository.close();
		}
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(searchService.get(entityIds, entityMeta)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = repository.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(searchService.get(entityIds, entityMeta, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = repository.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = mock(Entity.class);
		Query query = mock(Query.class);
		when(searchService.searchAsStream(query, entityMeta)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = repository.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}
}
