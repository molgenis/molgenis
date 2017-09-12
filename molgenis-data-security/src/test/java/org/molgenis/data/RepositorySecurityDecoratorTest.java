package org.molgenis.data;

import org.mockito.Mock;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.PermissionService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;
import static org.mockito.Mockito.*;
import static org.molgenis.security.core.Permission.*;
import static org.testng.Assert.assertEquals;

public class RepositorySecurityDecoratorTest extends AbstractMockitoTest
{
	private static final String ENTITY_TYPE_ID = "EntityType";
	private static final String EXCEPTION_MESSAGE_READ = "No 'READ' permission on entity type 'Entity type' with id 'EntityType'";
	private static final String EXCEPTION_MESSAGE_COUNT = "No 'COUNT' permission on entity type 'Entity type' with id 'EntityType'";
	private static final String EXCEPTION_MESSAGE_WRITE = "No 'WRITE' permission on entity type 'Entity type' with id 'EntityType'";

	@Mock
	private Repository<Entity> decoratedRepository;
	@Mock
	private PermissionService permissionService;

	private RepositorySecurityDecorator repositorySecurityDecorator;

	@Mock
	private EntityType entityType;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn("Entity type");
		when(decoratedRepository.getEntityType()).thenReturn(entityType);
		repositorySecurityDecorator = new RepositorySecurityDecorator(decoratedRepository, permissionService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testRepositorySecurityDecorator()
	{
		new RepositorySecurityDecorator(null, null);
	}

	@Test
	public void testDelegate()
	{
		assertEquals(repositorySecurityDecorator.delegate(), decoratedRepository);
	}

	@Test
	public void testIteratorPermissionAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(true);
		Iterator<Entity> entityIterator = emptyIterator();
		when(decoratedRepository.iterator()).thenReturn(entityIterator);
		assertEquals(repositorySecurityDecorator.iterator(), entityIterator);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_READ)
	public void testIteratorPermissionDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		repositorySecurityDecorator.iterator();
	}

	@Test
	public void testCloseAllowed() throws IOException
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		repositorySecurityDecorator.close();
	}

	@Test
	public void testCloseDenied() throws IOException
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		repositorySecurityDecorator.close();
	}

	@Test
	public void testForEachBatchedFetchPermissionAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(true);
		Fetch fetch = mock(Fetch.class);
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 123;
		repositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize);
		verify(decoratedRepository).forEachBatched(fetch, consumer, batchSize);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_READ)
	public void testForEachBatchedFetchPermissionDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		Fetch fetch = mock(Fetch.class);
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 123;
		repositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize);
	}

	@Test
	public void testGetCapabilitiesAllowed()
	{
		testGetCapabilitiesAllowedOrDenied(true);
	}

	@Test
	public void testGetCapabilitiesDenied()
	{
		testGetCapabilitiesAllowedOrDenied(false);
	}

	private void testGetCapabilitiesAllowedOrDenied(boolean value)
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(value);
		@SuppressWarnings("unchecked")
		Set<RepositoryCapability> capabilities = mock(Set.class);
		when(decoratedRepository.getCapabilities()).thenReturn(capabilities);
		assertEquals(repositorySecurityDecorator.getCapabilities(), capabilities);
	}

	@Test
	public void testGetQueryOperatorsAllowed()
	{
		testGetQueryOperatorsAllowedOrDenied(true);
	}

	@Test
	public void testGetQueryOperatorsDenied()
	{
		testGetQueryOperatorsAllowedOrDenied(false);
	}

	private void testGetQueryOperatorsAllowedOrDenied(boolean value)
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(value);
		@SuppressWarnings("unchecked")
		Set<QueryRule.Operator> queryOperators = mock(Set.class);
		when(decoratedRepository.getQueryOperators()).thenReturn(queryOperators);
		assertEquals(repositorySecurityDecorator.getQueryOperators(), queryOperators);
	}

	@Test
	public void testGetName()
	{
		when(decoratedRepository.getName()).thenReturn(ENTITY_TYPE_ID);
		assertEquals(repositorySecurityDecorator.getName(), ENTITY_TYPE_ID);
	}

	@Test
	public void testGetEntityType()
	{
		assertEquals(repositorySecurityDecorator.getEntityType(), entityType);
	}

	@Test
	public void testCountAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, COUNT)).thenReturn(true);
		when(decoratedRepository.count()).thenReturn(123L);
		assertEquals(repositorySecurityDecorator.count(), 123L);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_COUNT)
	public void testCountDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, COUNT)).thenReturn(false);
		repositorySecurityDecorator.count();
	}

	@Test
	public void testCountQueryAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, COUNT)).thenReturn(true);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.count(query)).thenReturn(123L);
		assertEquals(repositorySecurityDecorator.count(query), 123L);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_COUNT)
	public void testCountQueryDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, COUNT)).thenReturn(false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.count(query);
	}

	@Test
	public void testQuery()
	{
		assertEquals(repositorySecurityDecorator.query(), new QueryImpl<>(repositorySecurityDecorator));
	}

	@Test
	public void testFindAllQueryAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(true);
		Stream<Entity> entityStream = Stream.empty();
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(entityStream);
		assertEquals(repositorySecurityDecorator.findAll(query), entityStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_READ)
	public void testFindAllQueryDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findAll(query);
	}

	@Test
	public void testFindOneQueryAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(true);
		Entity entity = mock(Entity.class);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.findOne(query)).thenReturn(entity);
		assertEquals(repositorySecurityDecorator.findOne(query), entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_READ)
	public void testFindOneQueryDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findOne(query);
	}

	@Test
	public void testFindOneByIdAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(true);
		Entity entity = mock(Entity.class);
		Object id = "id";
		when(decoratedRepository.findOneById(id)).thenReturn(entity);
		assertEquals(repositorySecurityDecorator.findOneById(id), entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_READ)
	public void testFindOneByIdDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		Object id = "id";
		repositorySecurityDecorator.findOneById(id);
	}

	@Test
	public void testFindOneByIdFetchAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(true);
		Entity entity = mock(Entity.class);
		Object id = "id";
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepository.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(repositorySecurityDecorator.findOneById(id, fetch), entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_READ)
	public void testFindOneByIdFetchDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		Object id = "id";
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findOneById(id, fetch);
	}

	@Test
	public void testFindAllStreamAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(true);
		Stream<Object> idStream = Stream.empty();
		Stream<Entity> entityStream = Stream.empty();
		when(decoratedRepository.findAll(idStream)).thenReturn(entityStream);
		assertEquals(repositorySecurityDecorator.findAll(idStream), entityStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_READ)
	public void testFindAllStreamDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		Stream<Object> idStream = Stream.empty();
		repositorySecurityDecorator.findAll(idStream);
	}

	@Test
	public void testFindAllStreamFetchAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(true);
		Stream<Object> idStream = Stream.empty();
		Fetch fetch = mock(Fetch.class);
		Stream<Entity> entityStream = Stream.empty();
		when(decoratedRepository.findAll(idStream, fetch)).thenReturn(entityStream);
		assertEquals(repositorySecurityDecorator.findAll(idStream, fetch), entityStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_READ)
	public void testFindAllStreamFetchDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, READ)).thenReturn(false);
		Stream<Object> idStream = Stream.empty();
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findAll(idStream, fetch);
	}

	@Test
	public void testAggregateAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, COUNT)).thenReturn(true);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(decoratedRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		assertEquals(repositorySecurityDecorator.aggregate(aggregateQuery), aggregateResult);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_COUNT)
	public void testAggregateDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(false);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repositorySecurityDecorator.aggregate(aggregateQuery);
	}

	@Test
	public void testUpdateEntityAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.update(entity);
		verify(decoratedRepository).update(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testUpdateEntityDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(false);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.update(entity);
	}

	@Test
	public void testUpdateStreamAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.update(entityStream);
		verify(decoratedRepository).update(entityStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testUpdateStreamDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, COUNT)).thenReturn(false);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.update(entityStream);
	}

	@Test
	public void testDeleteEntityAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.delete(entity);
		verify(decoratedRepository).delete(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testDeleteEntityDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(false);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.delete(entity);
	}

	@Test
	public void testDeleteStreamAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.delete(entityStream);
		verify(decoratedRepository).delete(entityStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testDeleteStreamDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(false);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.delete(entityStream);
	}

	@Test
	public void testDeleteByIdAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		Object id = "id";
		repositorySecurityDecorator.deleteById(id);
		verify(decoratedRepository).deleteById(id);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testDeleteByIdDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(false);
		Object id = "id";
		repositorySecurityDecorator.deleteById(id);
	}

	@Test
	public void testDeleteAllStreamAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		Stream<Object> idStream = Stream.empty();
		repositorySecurityDecorator.deleteAll(idStream);
		verify(decoratedRepository).deleteAll(idStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testDeleteAllStreamDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(false);
		Stream<Object> idStream = Stream.empty();
		repositorySecurityDecorator.deleteAll(idStream);
	}

	@Test
	public void testDeleteAllAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		repositorySecurityDecorator.deleteAll();
		verify(decoratedRepository).deleteAll();
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testDeleteAllDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(false);
		repositorySecurityDecorator.deleteAll();
	}

	@Test
	public void testAddEntityAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.add(entity);
		verify(decoratedRepository).add(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testAddEntityDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(false);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.add(entity);
	}

	@Test
	public void testAddStreamAllowed()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, WRITE)).thenReturn(true);
		Stream<Entity> entityStream = Stream.empty();
		when(decoratedRepository.add(entityStream)).thenReturn(0);
		assertEquals(repositorySecurityDecorator.add(entityStream), Integer.valueOf(0));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_WRITE)
	public void testAddStreamDenied()
	{
		when(permissionService.hasPermissionOnEntityType(entityType, COUNT)).thenReturn(false);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.add(entityStream);
	}
}