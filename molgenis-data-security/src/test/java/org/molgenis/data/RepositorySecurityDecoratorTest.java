package org.molgenis.data;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class RepositorySecurityDecoratorTest
{
	private String entityTypeId;
	private String entityId;
	private Repository<Entity> decoratedRepository;
	private RepositorySecurityDecorator repositorySecurityDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		entityTypeId = "entity";
		entityId = "entityID";
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getLabel()).thenReturn(entityTypeId);
		decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getName()).thenReturn(entityTypeId);
		when(decoratedRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn("entityID");
		repositorySecurityDecorator = new RepositorySecurityDecorator(decoratedRepository);
	}

	@Test
	public void addStream()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_WRITE_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Stream<Entity> entities = Stream.empty();
		when(decoratedRepository.add(entities)).thenReturn(123);
		assertEquals(repositorySecurityDecorator.add(entities), Integer.valueOf(123));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void addStreamNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Stream<Entity> entities = Stream.empty();
		try
		{
			repositorySecurityDecorator.add(entities);
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(decoratedRepository);
			throw e;
		}
	}

	@Test
	public void findAllPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Stream<Object> ids = Stream.of(0, 1);
		Fetch fetch = new Fetch();
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0, entity1);
		when(decoratedRepository.findAll(ids, fetch)).thenReturn(Stream.of(entity0, entity1));
		assertEquals(entities.collect(toList()), repositorySecurityDecorator.findAll(ids, fetch).collect(toList()));
		verify(decoratedRepository, times(1)).findAll(ids, fetch);
	}

	@Test
	public void deleteStream()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_WRITE_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Stream<Entity> entities = Stream.empty();
		repositorySecurityDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(entities);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteStreamNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Stream<Entity> entities = Stream.empty();
		try
		{
			repositorySecurityDecorator.delete(entities);
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(decoratedRepository);
			throw e;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_WRITE_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		repositorySecurityDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), singletonList(entity0));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateStreamNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Stream<Entity> entities = Stream.empty();
		try
		{
			repositorySecurityDecorator.update(entities);
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(decoratedRepository);
			throw e;
		}
	}

	@Test
	public void findAllStream()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = repositorySecurityDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void findAllStreamNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		repositorySecurityDecorator.findAll(entityIds);
	}

	@Test
	public void findAllStreamFetch()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = repositorySecurityDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void findAllStreamFetchNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		repositorySecurityDecorator.findAll(entityIds, fetch);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void findAllNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Stream<Object> ids = Stream.of(0, 1);
		Fetch fetch = new Fetch();
		Stream<Entity> entities = Stream.of(mock(Entity.class), mock(Entity.class));
		when(decoratedRepository.findAll(ids, fetch)).thenReturn(entities);
		repositorySecurityDecorator.findAll(ids, fetch);
	}

	@Test
	public void findOnePermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Object id = 0;
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepository.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(entity, decoratedRepository.findOneById(id, fetch));
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void findOneNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Object id = 0;
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepository.findOneById(id, fetch)).thenReturn(entity);
		repositorySecurityDecorator.findOneById(id, fetch);
	}

	@Test
	public void findAllAsStreamPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Entity entity0 = mock(Entity.class);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = repositorySecurityDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), singletonList(entity0));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void findAllAsStreamNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Entity entity0 = mock(Entity.class);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = repositorySecurityDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), singletonList(entity0));
	}

	@Test
	public void streamFetch()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_READ_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Fetch fetch = new Fetch();
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		repositorySecurityDecorator.forEachBatched(fetch, consumer, 1000);

		verify(decoratedRepository).forEachBatched(fetch, consumer, 1000);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void streamFetchNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Fetch fetch = new Fetch();
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		repositorySecurityDecorator.forEachBatched(fetch, consumer, 1000);

		verifyZeroInteractions(decoratedRepository);
	}

	@Test
	public void aggregate()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null,
				"ROLE_ENTITY_COUNT_" + entityId);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(repositorySecurityDecorator.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		assertEquals(aggregateResult, repositorySecurityDecorator.aggregate(aggregateQuery));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[COUNT\\] permission on entity type \\[entity\\] with id \\[entityID\\]")
	public void aggregateNoPermission()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repositorySecurityDecorator.aggregate(aggregateQuery);
	}
}