package org.molgenis.data.security;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.security.EntityTypePermission.*;

@SuppressWarnings("deprecation")
public class RepositorySecurityDecoratorTest extends AbstractMockitoTest
{
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
		initPermissionServiceMock(ADD_DATA, true);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.add(entity);
		verify(delegateRepository).add(entity);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:ADD_DATA entityTypeId:entityTypeId")
	public void testAddPermissionDenied()
	{
		initPermissionServiceMock(ADD_DATA, false);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.add(entity);
		verify(delegateRepository).add(entity);
	}

	@Test
	public void testAddStreamPermissionGranted()
	{
		initPermissionServiceMock(ADD_DATA, true);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.add(entityStream);
		verify(delegateRepository).add(entityStream);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:ADD_DATA entityTypeId:entityTypeId")
	public void testAddStreamPermissionDenied()
	{
		initPermissionServiceMock(ADD_DATA, false);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.add(entityStream);
		verify(delegateRepository).add(entityStream);
	}

	@Test
	public void testAggregatePermissionGranted()
	{
		initPermissionServiceMock(AGGREGATE_DATA, true);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repositorySecurityDecorator.aggregate(aggregateQuery);
		verify(delegateRepository).aggregate(aggregateQuery);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:AGGREGATE_DATA entityTypeId:entityTypeId")
	public void testAggregatePermissionDenied()
	{
		initPermissionServiceMock(AGGREGATE_DATA, false);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repositorySecurityDecorator.aggregate(aggregateQuery);
		verify(delegateRepository).aggregate(aggregateQuery);
	}

	@Test
	public void testCloseNoPermissionsNeeded() throws IOException
	{
		repositorySecurityDecorator.close();
		verify(delegateRepository).close();
		verifyZeroInteractions(permissionService);
	}

	@Test
	public void testCountPermissionGranted()
	{
		initPermissionServiceMock(COUNT_DATA, true);
		repositorySecurityDecorator.count();
		verify(delegateRepository).count();
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:COUNT_DATA entityTypeId:entityTypeId")
	public void testCountPermissionDenied()
	{
		initPermissionServiceMock(COUNT_DATA, false);
		repositorySecurityDecorator.count();
		verify(delegateRepository).count();
	}

	@Test
	public void testCountQueryPermissionGranted()
	{
		initPermissionServiceMock(COUNT_DATA, true);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.count(query);
		verify(delegateRepository).count(query);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:COUNT_DATA entityTypeId:entityTypeId")
	public void testCountQueryPermissionDenied()
	{
		initPermissionServiceMock(COUNT_DATA, false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.count(query);
		verify(delegateRepository).count(query);
	}

	@Test
	public void testDeletePermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.delete(entity);
		verify(delegateRepository).delete(entity);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:DELETE_DATA entityTypeId:entityTypeId")
	public void testDeletePermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.delete(entity);
		verify(delegateRepository).delete(entity);
	}

	@Test
	public void testDeleteStreamPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.delete(entityStream);
		verify(delegateRepository).delete(entityStream);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:DELETE_DATA entityTypeId:entityTypeId")
	public void testDeleteStreamPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.delete(entityStream);
		verify(delegateRepository).delete(entityStream);
	}

	@Test
	public void testDeleteAllPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
		repositorySecurityDecorator.deleteAll();
		verify(delegateRepository).deleteAll();
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:DELETE_DATA entityTypeId:entityTypeId")
	public void testDeleteAllPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
		repositorySecurityDecorator.deleteAll();
		verify(delegateRepository).deleteAll();
	}

	@Test
	public void testDeleteAllStreamPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
		Stream<Object> entityIdStream = Stream.empty();
		repositorySecurityDecorator.deleteAll(entityIdStream);
		verify(delegateRepository).deleteAll(entityIdStream);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:DELETE_DATA entityTypeId:entityTypeId")
	public void testDeleteAllStreamPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
		Stream<Object> entityIdStream = Stream.empty();
		repositorySecurityDecorator.deleteAll(entityIdStream);
		verify(delegateRepository).deleteAll(entityIdStream);
	}

	@Test
	public void testDeleteByIdPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
		Object entityId = mock(Object.class);
		repositorySecurityDecorator.deleteById(entityId);
		verify(delegateRepository).deleteById(entityId);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:DELETE_DATA entityTypeId:entityTypeId")
	public void testDeleteByIdPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
		Object entityId = mock(Object.class);
		repositorySecurityDecorator.deleteById(entityId);
		verify(delegateRepository).deleteById(entityId);
	}

	@Test
	public void testFindAllQueryPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findAll(query);
		verify(delegateRepository).findAll(query);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:READ_DATA entityTypeId:entityTypeId")
	public void testFindAllQueryPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findAll(query);
		verify(delegateRepository).findAll(query);
	}

	@Test
	public void testFindAllStreamPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
		Stream<Object> entityIdStream = Stream.empty();
		repositorySecurityDecorator.findAll(entityIdStream);
		verify(delegateRepository).findAll(entityIdStream);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:READ_DATA entityTypeId:entityTypeId")
	public void testFindAllStreamPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
		Stream<Object> entityIdStream = Stream.empty();
		repositorySecurityDecorator.findAll(entityIdStream);
		verify(delegateRepository).findAll(entityIdStream);
	}

	@Test
	public void testFindAllStreamFetchPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
		Stream<Object> entityIdStream = Stream.empty();
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findAll(entityIdStream, fetch);
		verify(delegateRepository).findAll(entityIdStream, fetch);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:READ_DATA entityTypeId:entityTypeId")
	public void testFindAllStreamFetchPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
		Stream<Object> entityIdStream = Stream.empty();
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findAll(entityIdStream, fetch);
		verify(delegateRepository).findAll(entityIdStream, fetch);
	}

	@Test
	public void testFindOneQueryPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findOne(query);
		verify(delegateRepository).findOne(query);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:READ_DATA entityTypeId:entityTypeId")
	public void testFindOneQueryPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		repositorySecurityDecorator.findOne(query);
		verify(delegateRepository).findOne(query);
	}

	@Test
	public void testFindOneByIdPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
		Object entityId = mock(Object.class);
		repositorySecurityDecorator.findOneById(entityId);
		verify(delegateRepository).findOneById(entityId);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:READ_DATA entityTypeId:entityTypeId")
	public void testFindOneByIdPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
		Object entityId = mock(Object.class);
		repositorySecurityDecorator.findOneById(entityId);
		verify(delegateRepository).findOneById(entityId);
	}

	@Test
	public void testFindOneByIdFetchPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
		Object entityId = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findOneById(entityId, fetch);
		verify(delegateRepository).findOneById(entityId, fetch);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:READ_DATA entityTypeId:entityTypeId")
	public void testFindOneByIdFetchPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
		Object entityId = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		repositorySecurityDecorator.findOneById(entityId, fetch);
		verify(delegateRepository).findOneById(entityId, fetch);
	}

	@Test
	public void testForEachBatchedPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
		Fetch fetch = mock(Fetch.class);
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 10;
		repositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize);
		verify(delegateRepository).forEachBatched(fetch, consumer, batchSize);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:READ_DATA entityTypeId:entityTypeId")
	public void testForEachBatchedPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
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
		initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
		repositorySecurityDecorator.iterator();
		verify(delegateRepository).iterator();
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:READ_DATA entityTypeId:entityTypeId")
	public void testIteratorPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
		repositorySecurityDecorator.iterator();
		verify(delegateRepository).iterator();
	}

	@Test
	public void testUpdatePermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.UPDATE_DATA, true);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.update(entity);
		verify(delegateRepository).update(entity);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:UPDATE_DATA entityTypeId:entityTypeId")
	public void testUpdatePermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.UPDATE_DATA, false);
		Entity entity = mock(Entity.class);
		repositorySecurityDecorator.update(entity);
		verify(delegateRepository).update(entity);
	}

	@Test
	public void testUpdateStreamPermissionGranted()
	{
		initPermissionServiceMock(EntityTypePermission.UPDATE_DATA, true);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.update(entityStream);
		verify(delegateRepository).update(entityStream);
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:UPDATE_DATA entityTypeId:entityTypeId")
	public void testUpdateStreamPermissionDenied()
	{
		initPermissionServiceMock(EntityTypePermission.UPDATE_DATA, false);
		Stream<Entity> entityStream = Stream.empty();
		repositorySecurityDecorator.update(entityStream);
		verify(delegateRepository).update(entityStream);
	}

	private void initPermissionServiceMock(EntityTypePermission permission, boolean hasPermission)
	{
		EntityType entityType = mock(EntityType.class);
		String entityTypeId = "entityTypeId";
		when(entityType.getId()).thenReturn(entityTypeId);
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), permission)).thenReturn(
				hasPermission);
	}
}