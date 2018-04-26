package org.molgenis.data;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class DataServiceImplTest
{
	private final List<String> entityTypeIds = asList("Entity1", "Entity2", "Entity3");
	private Repository<Entity> repo1;
	private Repository<Entity> repo2;
	private Repository<Entity> repoToRemove;
	private DataServiceImpl dataService;
	private MetaDataService metaDataService;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		dataService = new DataServiceImpl();
		repo1 = when(mock(Repository.class).getName()).thenReturn("Entity1").getMock();

		repo2 = mock(Repository.class);
		repo2 = when(mock(Repository.class).getName()).thenReturn("Entity2").getMock();

		repoToRemove = mock(Repository.class);
		repoToRemove = when(mock(Repository.class).getName()).thenReturn("Entity3").getMock();

		metaDataService = mock(MetaDataService.class);
		when(metaDataService.getRepository("Entity1")).thenReturn(repo1);
		when(metaDataService.getRepository("Entity2")).thenReturn(repo2);
		when(metaDataService.getRepository("Entity3")).thenReturn(repoToRemove);
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn("Entity1").getMock();
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn("Entity2").getMock();
		EntityType entityType3 = when(mock(EntityType.class).getId()).thenReturn("Entity3").getMock();

		when(metaDataService.getEntityTypes()).thenAnswer(
				invocation -> Stream.of(entityType1, entityType2, entityType3));
		dataService.setMetaDataService(metaDataService);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.add("Entity1", entities);
		verify(repo1, times(1)).add(entities);
	}

	@Test
	public void updateStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.update("Entity1", entities);
		verify(repo1, times(1)).update(entities);
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.delete("Entity1", entities);
		verify(repo1, times(1)).delete(entities);
	}

	@Test
	public void deleteAllStream()
	{
		Stream<Object> entityIds = Stream.empty();
		dataService.deleteAll("Entity1", entityIds);
		verify(repo1, times(1)).deleteAll(entityIds);
	}

	@Test
	public void getEntityNames()
	{
		assertEquals(dataService.getEntityTypeIds().collect(toList()), asList("Entity1", "Entity2", "Entity3"));
	}

	@Test
	public void getRepositoryByEntityName()
	{
		assertEquals(dataService.getRepository("Entity1"), repo1);
		assertEquals(dataService.getRepository("Entity2"), repo2);
	}

	@Test
	public void findOneStringObjectFetch()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(repo1.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(dataService.findOneById("Entity1", id, fetch), entity);
		verify(repo1, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchEntityNull()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		when(repo1.findOneById(id, fetch)).thenReturn(null);
		assertNull(dataService.findOneById("Entity1", id, fetch));
		verify(repo1, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchClass()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		Class<Entity> clazz = Entity.class;
		Entity entity = mock(Entity.class);
		when(repo1.findOneById(id, fetch)).thenReturn(entity);
		// how to check return value? converting iterable can't be mocked.
		dataService.findOneById("Entity1", id, fetch, clazz);
		verify(repo1, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchClassEntityNull()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		Class<Entity> clazz = Entity.class;
		when(repo1.findOneById(id, fetch)).thenReturn(null);
		assertNull(dataService.findOneById("Entity1", id, fetch, clazz));
		verify(repo1, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAllStringStream()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStringStreamClass()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Class<Entity> clazz = Entity.class;
		when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, clazz);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStringStreamFetch()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Fetch fetch = new Fetch();
		when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStringStreamFetchClass()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Class<Entity> clazz = Entity.class;
		Fetch fetch = new Fetch();
		when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch, clazz);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStreamString()
	{
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(new QueryImpl<>())).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1");
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStreamStringClass()
	{
		Class<Entity> clazz = Entity.class;
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(new QueryImpl<>())).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", clazz);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStreamStringQuery()
	{
		Entity entity0 = mock(Entity.class);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", query);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStreamStringQueryClass()
	{
		Class<Entity> clazz = Entity.class;
		Entity entity0 = mock(Entity.class);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", query, clazz);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}
}
