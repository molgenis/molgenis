package org.molgenis.data.security.owned;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.EntityPermission.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = { RowLevelSecurityRepositoryDecoratorTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class RowLevelSecurityRepositoryDecoratorTest extends AbstractMockitoTestNGSpringContextTests
{
	private static final String USERNAME = "user";

	@Mock
	private Repository<Entity> delegateRepository;
	@Mock
	private UserPermissionEvaluator userPermissionEvaluator;
	@Mock
	private MutableAclService mutableAclService;
	private RowLevelSecurityRepositoryDecorator rowLevelSecurityRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		rowLevelSecurityRepositoryDecorator = new RowLevelSecurityRepositoryDecorator(delegateRepository,
				userPermissionEvaluator, mutableAclService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testRowLevelSecurityRepositoryDecorator()
	{
		new RowLevelSecurityRepositoryDecorator(null, null, null);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testAdd()
	{
		Entity entity = getEntityMock();
		MutableAcl acl = mock(MutableAcl.class);
		when(mutableAclService.createAcl(new EntityIdentity(entity))).thenReturn(acl);

		rowLevelSecurityRepositoryDecorator.add(entity);

		verify(acl).insertAce(0, new CumulativePermission().set(WRITE).set(READ).set(COUNT), new PrincipalSid(USERNAME),
				true);
		verify(delegateRepository).add(entity);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testAddStream()
	{
		Entity entity = getEntityMock();
		MutableAcl acl = mock(MutableAcl.class);
		when(mutableAclService.createAcl(new EntityIdentity(entity))).thenReturn(acl);

		rowLevelSecurityRepositoryDecorator.add(Stream.of(entity));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entity));
		verify(acl).insertAce(0, new CumulativePermission().set(WRITE).set(READ).set(COUNT), new PrincipalSid(USERNAME),
				true);
	}

	@Test
	public void testUpdate()
	{
		Entity entity = getEntityMock();
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), WRITE)).thenReturn(true);
		rowLevelSecurityRepositoryDecorator.update(entity);
		verify(delegateRepository).update(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[test\\] with id \\[entityId\\]")
	public void testUpdatePermissionDenied()
	{
		Entity entity = getEntityMock();
		EntityType entityType = entity.getEntityType();
		when(entityType.getLabel()).thenReturn("test");
		rowLevelSecurityRepositoryDecorator.update(entity);
		verify(delegateRepository, times(0)).update(entity);
	}

	@Test
	public void testUpdateStream()
	{
		Entity entity = getEntityMock();
		EntityType entityType = entity.getEntityType();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), WRITE)).thenReturn(true);
		rowLevelSecurityRepositoryDecorator.update(Stream.of(entity));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entity));
	}

	@Test
	public void testUpdateStreamPermissionDenied()
	{
		Entity entity = getEntityMock();
		EntityType entityType = entity.getEntityType();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		rowLevelSecurityRepositoryDecorator.update(Stream.of(entity));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), emptyList());
	}

	@Test
	public void testDelete()
	{
		Entity entity = getEntityMock();
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), WRITE)).thenReturn(true);
		rowLevelSecurityRepositoryDecorator.delete(entity);
		verify(delegateRepository).delete(entity);
		verify(mutableAclService).deleteAcl(new EntityIdentity(entity), true);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[test\\] with id \\[entityId\\]")
	public void testDeletePermissionDenied()
	{
		Entity entity = getEntityMock();
		EntityType entityType = entity.getEntityType();
		when(entityType.getLabel()).thenReturn("test");
		rowLevelSecurityRepositoryDecorator.delete(entity);
		verify(delegateRepository, times(0)).delete(entity);
	}

	@Test
	public void testDeleteStream()
	{
		Entity entity = getEntityMock();
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), WRITE)).thenReturn(true);
		rowLevelSecurityRepositoryDecorator.delete(Stream.of(entity));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).delete(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entity));
		verify(mutableAclService).deleteAcl(new EntityIdentity(entity), true);
	}

	@Test
	public void testDeleteStreamPermissionDenied()
	{
		Entity entity = getEntityMock();
		rowLevelSecurityRepositoryDecorator.delete(Stream.of(entity));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).delete(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), emptyList());
		verify(mutableAclService, times(0)).deleteAcl(new EntityIdentity(entity), true);
	}

	@Test
	public void testDeleteById()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		Object entityId = "entityId";
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entityTypeId, entityId), WRITE)).thenReturn(true);
		rowLevelSecurityRepositoryDecorator.deleteById(entityId);
		verify(delegateRepository).deleteById(entityId);
		verify(mutableAclService).deleteAcl(new EntityIdentity(entityTypeId, entityId), true);
	}

	@Test
	public void testDeleteByIdPermissionDenied()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		Object entityId = "entityId";
		rowLevelSecurityRepositoryDecorator.deleteById(entityId);
		verify(delegateRepository, times(0)).deleteById(entityId);
		verify(mutableAclService, times(0)).deleteAcl(new EntityIdentity(entityTypeId, entityId), true);
	}

	@Test
	public void testDeleteAll()
	{
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenAnswer(
				invocation -> Stream.of(entity));
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), WRITE)).thenReturn(true);
		rowLevelSecurityRepositoryDecorator.deleteAll();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).delete(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entity));
	}

	@Test
	public void testDeleteAllPermissionDenied()
	{
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenAnswer(
				invocation -> Stream.of(entity));
		rowLevelSecurityRepositoryDecorator.deleteAll();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).delete(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), emptyList());
	}

	@Test
	public void testDeleteAllStream()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		Object entityId = "entityId";
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entityTypeId, entityId), WRITE)).thenReturn(true);
		rowLevelSecurityRepositoryDecorator.deleteAll(Stream.of(entityId));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).deleteAll(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entityId));
		verify(mutableAclService).deleteAcl(new EntityIdentity(entityTypeId, entityId), true);
	}

	@Test
	public void testDeleteAllStreamPermissionDenied()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		Object entityId = "entityId";
		rowLevelSecurityRepositoryDecorator.deleteAll(Stream.of(entityId));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).deleteAll(entityStreamCaptor.capture());
		assertEquals(entityStreamCaptor.getValue().collect(toList()), emptyList());
		verify(mutableAclService, times(0)).deleteAcl(new EntityIdentity(entityTypeId, entityId), true);
	}

	@Test
	public void testFindOneById()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		Object entityId = "entityId";
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entityTypeId, entityId), READ)).thenReturn(true);
		Entity entity = mock(Entity.class);
		when(delegateRepository.findOneById(entityId)).thenReturn(entity);
		assertEquals(rowLevelSecurityRepositoryDecorator.findOneById(entityId), entity);
	}

	@Test
	public void testFindOneByIdPermissionDenied()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		Object entityId = "entityId";
		assertNull(rowLevelSecurityRepositoryDecorator.findOneById(entityId));
	}

	@Test
	public void testFindOneByIdFetch()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		Object entityId = "entityId";
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entityTypeId, entityId), READ)).thenReturn(true);
		Entity entity = mock(Entity.class);
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(entityId, fetch)).thenReturn(entity);
		assertEquals(rowLevelSecurityRepositoryDecorator.findOneById(entityId, fetch), entity);
	}

	@Test
	public void testFindOneByIdFetchPermissionDenied()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		Object entityId = "entityId";
		Fetch fetch = mock(Fetch.class);
		assertNull(rowLevelSecurityRepositoryDecorator.findOneById(entityId, fetch));
	}

	@Test
	public void testFindOne()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		Entity entity = getEntityMock();
		when(delegateRepository.findOne(query)).thenReturn(entity);
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
		assertEquals(entity, rowLevelSecurityRepositoryDecorator.findOne(query));
	}

	@Test
	public void testFindOnePermissionDenied()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		Entity entity = getEntityMock();
		when(delegateRepository.findOne(query)).thenReturn(entity);
		assertNull(rowLevelSecurityRepositoryDecorator.findOne(query));
	}

	@Test
	public void testFindAllQuery()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenAnswer(
				invocation -> Stream.of(entity));
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
		assertEquals(rowLevelSecurityRepositoryDecorator.findAll(query).collect(toList()), singletonList(entity));
	}

	@Test
	public void testFindAllQueryPermissionDenied()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenAnswer(
				invocation -> Stream.of(entity));
		assertEquals(rowLevelSecurityRepositoryDecorator.findAll(query).collect(toList()), emptyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindAllStream()
	{
		Object entityId = "entityId";
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(any(Stream.class))).thenAnswer(invocation -> Stream.of(entity));
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
		assertEquals(rowLevelSecurityRepositoryDecorator.findAll(Stream.of(entityId)).collect(toList()), singletonList(entity));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindAllStreamPermissionDenied()
	{
		Object entityId = "entityId";
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(any(Stream.class))).thenAnswer(invocation -> Stream.of(entity));
		assertEquals(rowLevelSecurityRepositoryDecorator.findAll(Stream.of(entityId)).collect(toList()), emptyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindAllStreamFetch()
	{
		Object entityId = "entityId";
		Entity entity = getEntityMock();
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findAll(any(Stream.class), eq(fetch))).thenAnswer(invocation -> Stream.of(entity));
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
		assertEquals(rowLevelSecurityRepositoryDecorator.findAll(Stream.of(entityId), fetch).collect(toList()),
				singletonList(entity));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindAllStreamFetchPermissionDenied()
	{
		Object entityId = "entityId";
		Entity entity = getEntityMock();
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findAll(any(Stream.class), eq(fetch))).thenAnswer(invocation -> Stream.of(entity));
		assertEquals(rowLevelSecurityRepositoryDecorator.findAll(Stream.of(entityId), fetch).collect(toList()),
				emptyList());
	}

	@Test
	public void testCount()
	{
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenAnswer(
				invocation -> Stream.of(entity));
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), COUNT)).thenReturn(true);
		assertEquals(rowLevelSecurityRepositoryDecorator.count(), 1L);
	}

	@Test
	public void testCountPermissionDenied()
	{
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenAnswer(
				invocation -> Stream.of(entity));
		assertEquals(rowLevelSecurityRepositoryDecorator.count(), 0L);
	}

	@Test
	public void testCountQuery()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenAnswer(
				invocation -> Stream.of(entity));
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), COUNT)).thenReturn(true);
		assertEquals(rowLevelSecurityRepositoryDecorator.count(query), 1L);
	}

	@Test
	public void testCountQueryPermissionDenied()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		Entity entity = getEntityMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenAnswer(
				invocation -> Stream.of(entity));
		assertEquals(rowLevelSecurityRepositoryDecorator.count(query), 0L);
	}

	@Test
	public void testIterator()
	{
		Entity entity = getEntityMock();
		when(delegateRepository.iterator()).thenReturn(singletonList(entity).iterator());
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
		assertEquals(newArrayList(rowLevelSecurityRepositoryDecorator.iterator()), singletonList(entity));
	}

	@Test
	public void testIteratorPermissionDenied()
	{
		Entity entity = getEntityMock();
		when(delegateRepository.iterator()).thenReturn(singletonList(entity).iterator());
		assertEquals(newArrayList(rowLevelSecurityRepositoryDecorator.iterator()), emptyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testForEachBatched()
	{
		Entity entity = getEntityMock();
		Fetch fetch = mock(Fetch.class);
		List<Entity> actualEntities = new ArrayList<>();
		doAnswer(invocation ->
		{
			((Consumer<List<Entity>>) invocation.getArgument(1)).accept(singletonList(entity));
			return null;
		}).when(delegateRepository).forEachBatched(eq(fetch), any(), eq(1000));
		when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
		rowLevelSecurityRepositoryDecorator.forEachBatched(fetch, actualEntities::addAll, 1000);
		assertEquals(actualEntities, singletonList(entity));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testForEachBatchedPermissionDenied()
	{
		Entity entity = getEntityMock();
		Fetch fetch = mock(Fetch.class);
		List<Entity> actualEntities = new ArrayList<>();
		doAnswer(invocation ->
		{
			((Consumer<List<Entity>>) invocation.getArgument(1)).accept(singletonList(entity));
			return null;
		}).when(delegateRepository).forEachBatched(eq(fetch), any(), eq(1000));
		rowLevelSecurityRepositoryDecorator.forEachBatched(fetch, actualEntities::addAll, 1000);
		assertEquals(actualEntities, emptyList());
	}

	@WithMockUser(username = USERNAME, roles = "SU")
	@Test
	public void testAggregate()
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResponse = mock(AggregateResult.class);
		when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResponse);
		assertEquals(rowLevelSecurityRepositoryDecorator.aggregate(aggregateQuery), aggregateResponse);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testAggregatePermissionDenied()
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		rowLevelSecurityRepositoryDecorator.aggregate(aggregateQuery);
	}

	private Entity getEntityMock()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId");
		Entity entity = mock(Entity.class);
		when(entity.getEntityType()).thenReturn(entityType);
		when(entity.getIdValue()).thenReturn("entityId");
		return entity;
	}

	static class Config
	{
	}
}