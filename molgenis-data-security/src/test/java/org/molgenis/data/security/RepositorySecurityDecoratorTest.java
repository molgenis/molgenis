package org.molgenis.data.security;

import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@SuppressWarnings("deprecation")
public class RepositorySecurityDecoratorTest extends AbstractMockitoTest
{
	private static final String MESSAGE_NO_COUNT_PERMISSION = "No \\[COUNT\\] permission on entity type \\[Entity type\\] with id \\[entityTypeId\\]";
	private static final String MESSAGE_NO_READ_PERMISSION = "No \\[READ\\] permission on entity type \\[Entity type\\] with id \\[entityTypeId\\]";
	private static final String MESSAGE_NO_WRITE_PERMISSION = "No \\[WRITE\\] permission on entity type \\[Entity type\\] with id \\[entityTypeId\\]";
	private static final String MESSAGE_NO_CREATE_PERMISSION = "No \\[CREATE\\] permission on entity type \\[Entity type\\] with id \\[entityTypeId\\]";

	@Mock
	private Repository<Entity> delegateRepository;
	@Mock
	private UserPermissionEvaluator permissionService;

	private RepositorySecurityDecorator repositorySecurityDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repositorySecurityDecorator = new RepositorySecurityDecorator(delegateRepository, permissionService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testRepositorySecurityDecorator()
	{
		new RepositorySecurityDecorator(null, null);
	}

	@Test
	public void testAddPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.CREATE, true);
		when(delegateRepository.getName()).thenReturn("test");
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.add(entity);
		verify(delegateRepository).add(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_CREATE_PERMISSION)
	public void testAddPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.CREATE, false);
		Entity entity = mock(Entity.class);
		when(delegateRepository.getName()).thenReturn("test");
		repositorySecurityDecorator.add(entity);
		verify(delegateRepository).add(entity);
	}

	@Test
	public void testAddStreamPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.CREATE, true);
		when(delegateRepository.getName()).thenReturn("test");
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.add(entityStream);
		verify(delegateRepository).add(entityStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_CREATE_PERMISSION)
	public void testAddStreamPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.CREATE, false);
		when(delegateRepository.getName()).thenReturn("test");
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.add(entityStream);
		verify(delegateRepository).add(entityStream);
	}

	@Test
	public void testAggregatePermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.COUNT, true);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repositorySecurityDecorator.aggregate(aggregateQuery);
		verify(delegateRepository).aggregate(aggregateQuery);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_COUNT_PERMISSION)
	public void testAggregatePermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.COUNT, false);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repositorySecurityDecorator.aggregate(aggregateQuery);
		verify(delegateRepository).aggregate(aggregateQuery);
	}

	@Test
	public void testClosePermissionGranted() throws IOException
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, true);
		repositorySecurityDecorator.close();
		verify(delegateRepository).close();
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_WRITE_PERMISSION)
	public void testClosePermissionDenied() throws IOException
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, false);
		repositorySecurityDecorator.close();
	}

	@Test
	public void testCountPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.COUNT, true);
		repositorySecurityDecorator.count();
		verify(delegateRepository).count();
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_COUNT_PERMISSION)
	public void testCountPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.COUNT, false);
		repositorySecurityDecorator.count();
		verify(delegateRepository).count();
	}

	@Test
	public void testCountQueryPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.COUNT, true);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.count(query);
		verify(delegateRepository).count(query);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_COUNT_PERMISSION)
	public void testCountQueryPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.COUNT, false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.count(query);
		verify(delegateRepository).count(query);
	}

	@Test
	public void testDeletePermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, true);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.delete(entity);
		verify(delegateRepository).delete(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_WRITE_PERMISSION)
	public void testDeletePermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, false);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.delete(entity);
		verify(delegateRepository).delete(entity);
	}

	@Test
	public void testDeleteStreamPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, true);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.delete(entityStream);
		verify(delegateRepository).delete(entityStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_WRITE_PERMISSION)
	public void testDeleteStreamPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, false);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.delete(entityStream);
		verify(delegateRepository).delete(entityStream);
	}

	@Test
	public void testDeleteAllPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, true);
		repositorySecurityDecorator.deleteAll();
		verify(delegateRepository).deleteAll();
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_WRITE_PERMISSION)
	public void testDeleteAllPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, false);
		repositorySecurityDecorator.deleteAll();
		verify(delegateRepository).deleteAll();
	}

	@Test
	public void testDeleteAllStreamPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, true);
		Stream<Object> entityIdStream = Stream.empty();
		repositorySecurityDecorator.deleteAll(entityIdStream);
		verify(delegateRepository).deleteAll(entityIdStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_WRITE_PERMISSION)
	public void testDeleteAllStreamPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, false);
		Stream<Object> entityIdStream = Stream.empty();
		repositorySecurityDecorator.deleteAll(entityIdStream);
		verify(delegateRepository).deleteAll(entityIdStream);
	}

	@Test
	public void testDeleteByIdPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, true);
		Object entityId = mock(Object.class);
		repositorySecurityDecorator.deleteById(entityId);
		verify(delegateRepository).deleteById(entityId);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_WRITE_PERMISSION)
	public void testDeleteByIdPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, false);
		Object entityId = mock(Object.class);
		repositorySecurityDecorator.deleteById(entityId);
		verify(delegateRepository).deleteById(entityId);
	}

	@Test
	public void testFindAllQueryPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.READ, true);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findAll(query);
		verify(delegateRepository).findAll(query);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_READ_PERMISSION)
	public void testFindAllQueryPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.READ, false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findAll(query);
		verify(delegateRepository).findAll(query);
	}

	@Test
	public void testFindAllStreamPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.READ, true);
		Stream<Object> entityIdStream = Stream.empty();
		repositorySecurityDecorator.findAll(entityIdStream);
		verify(delegateRepository).findAll(entityIdStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_READ_PERMISSION)
	public void testFindAllStreamPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.READ, false);
		Stream<Object> entityIdStream = Stream.empty();
		repositorySecurityDecorator.findAll(entityIdStream);
		verify(delegateRepository).findAll(entityIdStream);
	}

	@Test
	public void testFindAllStreamFetchPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.READ, true);
		Stream<Object> entityIdStream = Stream.empty();
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findAll(entityIdStream, fetch);
		verify(delegateRepository).findAll(entityIdStream, fetch);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_READ_PERMISSION)
	public void testFindAllStreamFetchPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.READ, false);
		Stream<Object> entityIdStream = Stream.empty();
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findAll(entityIdStream, fetch);
		verify(delegateRepository).findAll(entityIdStream, fetch);
	}

	@Test
	public void testFindOneQueryPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.READ, true);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findOne(query);
		verify(delegateRepository).findOne(query);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_READ_PERMISSION)
	public void testFindOneQueryPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.READ, false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findOne(query);
		verify(delegateRepository).findOne(query);
	}

	@Test
	public void testFindOneByIdPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.READ, true);
		Object entityId = mock(Object.class);
		repositorySecurityDecorator.findOneById(entityId);
		verify(delegateRepository).findOneById(entityId);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_READ_PERMISSION)
	public void testFindOneByIdPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.READ, false);
		Object entityId = mock(Object.class);
		repositorySecurityDecorator.findOneById(entityId);
		verify(delegateRepository).findOneById(entityId);
	}

	@Test
	public void testFindOneByIdFetchPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.READ, true);
		Object entityId = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findOneById(entityId, fetch);
		verify(delegateRepository).findOneById(entityId, fetch);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_READ_PERMISSION)
	public void testFindOneByIdFetchPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.READ, false);
		Object entityId = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findOneById(entityId, fetch);
		verify(delegateRepository).findOneById(entityId, fetch);
	}

	@Test
	public void testForEachBatchedPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.READ, true);
		Fetch fetch = mock(Fetch.class);
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 10;
		repositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize);
		verify(delegateRepository).forEachBatched(fetch, consumer, batchSize);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_READ_PERMISSION)
	public void testForEachBatchedPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.READ, false);
		Fetch fetch = mock(Fetch.class);
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 10;
		repositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize);
		verify(delegateRepository).forEachBatched(fetch, consumer, batchSize);
	}

	@Test
	public void testIteratorPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.READ, true);
		repositorySecurityDecorator.iterator();
		verify(delegateRepository).iterator();
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_READ_PERMISSION)
	public void testIteratorPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.READ, false);
		repositorySecurityDecorator.iterator();
		verify(delegateRepository).iterator();
	}

	@Test
	public void testUpdatePermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, true);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.update(entity);
		verify(delegateRepository).update(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_WRITE_PERMISSION)
	public void testUpdatePermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, false);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.update(entity);
		verify(delegateRepository).update(entity);
	}

	@Test
	public void testUpdateStreamPermissionGranted()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, true);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.update(entityStream);
		verify(delegateRepository).update(entityStream);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = MESSAGE_NO_WRITE_PERMISSION)
	public void testUpdateStreamPermissionDenied()
	{
		initPermissionServiceMock(RepositoryPermission.WRITE, false);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.update(entityStream);
		verify(delegateRepository).update(entityStream);
	}

	private void initPermissionServiceMock(RepositoryPermission permission, boolean hasPermission)
	{
		EntityType entityType = mock(EntityType.class);
		String entityTypeId = "entityTypeId";
		when(entityType.getId()).thenReturn(entityTypeId);
		if (!hasPermission)
		{
			String entityTypeLabel = "Entity type";
			when(entityType.getLabel()).thenReturn(entityTypeLabel);
		}
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(permissionService.hasPermission(new RepositoryIdentity(entityTypeId), permission)).thenReturn(
				hasPermission);
	}
}