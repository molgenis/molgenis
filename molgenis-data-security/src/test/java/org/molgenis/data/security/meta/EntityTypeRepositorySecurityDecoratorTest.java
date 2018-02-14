package org.molgenis.data.security.meta;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.molgenis.security.core.Permission.COUNT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = { EntityTypeRepositorySecurityDecoratorTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class EntityTypeRepositorySecurityDecoratorTest extends AbstractMockitoTestNGSpringContextTests
{
	private static final String USERNAME = "user";

	@Mock
	private Repository<EntityType> delegateRepository;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private PermissionService permissionService;
	@Mock
	private MutableAclService mutableAclService;

	private EntityTypeRepositorySecurityDecorator repo;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repo = new EntityTypeRepositorySecurityDecorator(delegateRepository, systemEntityTypeRegistry,
				permissionService, mutableAclService);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void count()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		when(delegateRepository.spliterator()).thenReturn(asList(entityType0, entityType1).spliterator());
		doReturn(false).when(permissionService).hasPermissionOnEntityType(entityType0Name, COUNT);
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType1Name, COUNT);
		assertEquals(repo.count(), 1L);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void add()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		repo.add(entityType);
		verify(delegateRepository).add(entityType);
		verify(mutableAclService).createAcl(new EntityTypeIdentity(entityTypeId));
	}

	@Test
	public void query()
	{
		assertEquals(repo.query().getRepository(), repo);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void countQuery()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		Query<EntityType> q = new QueryImpl<>();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityType0, entityType1));
		doReturn(false).when(permissionService).hasPermissionOnEntityType(entityType0Name, COUNT);
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType1Name, COUNT);
		assertEquals(repo.count(q), 1L);
		assertEquals(queryCaptor.getValue().getOffset(), 0);
		assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SU")
	@Test
	public void countQuerySu()
	{
		countQuerySuOrSystem();
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SU")
	@Test
	public void countQuerySystem()
	{
		countQuerySuOrSystem();
	}

	private void countQuerySuOrSystem()
	{
		long count = 123L;
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(delegateRepository.count(q)).thenReturn(count);
		assertEquals(repo.count(q), 123L);
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SU")
	@Test
	public void findAllQuerySu()
	{
		findAllQuerySuOrSystem();
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SYSTEM")
	@Test
	public void findAllQuerySystem()
	{
		findAllQuerySuOrSystem();
	}

	private void findAllQuerySuOrSystem()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(delegateRepository.findAll(q)).thenReturn(Stream.of(entityType0, entityType1));
		assertEquals(repo.findAll(q).collect(toList()), asList(entityType0, entityType1));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllQueryUser()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(
				Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType0Name, COUNT);
		doReturn(false).when(permissionService).hasPermissionOnEntityType(entityType1Name, COUNT);
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType2Name, COUNT);
		assertEquals(repo.findAll(q).collect(toList()), asList(entityType0, entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllQueryUserOffsetLimit()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(q.getOffset()).thenReturn(1);
		when(q.getPageSize()).thenReturn(1);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(
				Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType0Name, COUNT);
		doReturn(false).when(permissionService).hasPermissionOnEntityType(entityType1Name, COUNT);
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType2Name, COUNT);
		assertEquals(repo.findAll(q).collect(toList()), singletonList(entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SU")
	@Test
	public void iteratorSu()
	{
		iteratorSuOrSystem();
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SYSTEM")
	@Test
	public void iteratorSystem()
	{
		iteratorSuOrSystem();
	}

	private void iteratorSuOrSystem()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		when(delegateRepository.iterator()).thenReturn(asList(entityType0, entityType1).iterator());
		assertEquals(newArrayList(repo.iterator()), asList(entityType0, entityType1));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void iteratorUser()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		when(delegateRepository.spliterator()).thenReturn(asList(entityType0, entityType1, entityType2).spliterator());
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType0Name, COUNT);
		doReturn(false).when(permissionService).hasPermissionOnEntityType(entityType1Name, COUNT);
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType2Name, COUNT);
		assertEquals(newArrayList(repo.iterator()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQuerySu()
	{
		findOneQuerySuOrSystem();
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQuerySystem()
	{
		findOneQuerySuOrSystem();
	}

	private void findOneQuerySuOrSystem()
	{
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		repo.findOne(q);
		verify(delegateRepository).findOne(q);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQueryUserPermissionAllowed()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(delegateRepository.findOne(q)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(true);
		assertEquals(repo.findOne(q), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQueryUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(delegateRepository.findOne(q)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(false);
		assertNull(repo.findOne(q));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdSu()
	{
		findOneByIdSuOrSystem();
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdSystem()
	{
		findOneByIdSuOrSystem();
	}

	private void findOneByIdSuOrSystem()
	{
		Object id = "0";
		repo.findOneById(id);
		verify(delegateRepository).findOneById(id);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdUserPermissionAllowed()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		when(delegateRepository.findOneById(id)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(true);
		assertEquals(repo.findOneById(id), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		when(delegateRepository.findOneById(id)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(false);
		assertNull(repo.findOneById(id));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchSu()
	{
		findOneByIdFetchSuOrSystem();
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchSystem()
	{
		findOneByIdFetchSuOrSystem();
	}

	private void findOneByIdFetchSuOrSystem()
	{
		Object id = "0";
		Fetch fetch = mock(Fetch.class);
		repo.findOneById(id, fetch);
		verify(delegateRepository).findOneById(id, fetch);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchUserPermissionAllowed()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(id, fetch)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(true);
		assertEquals(repo.findOneById(id, fetch), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(id, fetch)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(false);
		assertNull(repo.findOneById(id, fetch));
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SU")
	@Test
	public void findAllIdsSu()
	{
		findAllIdsSuOrSystem();
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SYSTEM")
	@Test
	public void findAllIdsSystem()
	{
		findAllIdsSuOrSystem();
	}

	private void findAllIdsSuOrSystem()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		Stream<Object> ids = Stream.of("0", "1");
		when(delegateRepository.findAll(ids)).thenReturn(Stream.of(entityType0, entityType1));
		assertEquals(repo.findAll(ids).collect(toList()), asList(entityType0, entityType1));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllIdsUser()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		Stream<Object> ids = Stream.of("0", "1");
		when(delegateRepository.findAll(ids)).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType0Name, COUNT);
		doReturn(false).when(permissionService).hasPermissionOnEntityType(entityType1Name, COUNT);
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType2Name, COUNT);
		assertEquals(repo.findAll(ids).collect(toList()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SU")
	@Test
	public void findAllIdsFetchSu()
	{
		findAllIdsFetchSuOrSystem();
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SYSTEM")
	@Test
	public void findAllIdsFetchSystem()
	{
		findAllIdsFetchSuOrSystem();
	}

	private void findAllIdsFetchSuOrSystem()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		Stream<Object> ids = Stream.of("0", "1");
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findAll(ids, fetch)).thenReturn(Stream.of(entityType0, entityType1));
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(entityType0, entityType1));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllIdsFetchUser()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		Stream<Object> ids = Stream.of("0", "1");
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findAll(ids, fetch)).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType0Name, COUNT);
		doReturn(false).when(permissionService).hasPermissionOnEntityType(entityType1Name, COUNT);
		doReturn(true).when(permissionService).hasPermissionOnEntityType(entityType2Name, COUNT);
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SU")
	@Test
	public void aggregateSu()
	{
		aggregateSuOrSystem();
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SYSTEM")
	@Test
	public void aggregateSystem()
	{
		aggregateSuOrSystem();
	}

	private void aggregateSuOrSystem()
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		assertEquals(repo.aggregate(aggregateQuery), aggregateResult);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void aggregateUser()
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repo.aggregate(aggregateQuery);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void delete()
	{
		String entityTypeId = "entityTypeId";
		when(permissionService.hasPermissionOnEntityType(entityTypeId, Permission.WRITEMETA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId").getMock();
		repo.delete(entityType);
		verify(mutableAclService).deleteAcl(new EntityTypeIdentity(entityTypeId), true);
		verify(delegateRepository).delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITEMETA\\] permission on entity type \\[Entity type\\] with id \\[entityTypeId\\]")
	public void deleteNotAllowed()
	{
		String entityTypeId = "entityTypeId";
		when(permissionService.hasPermissionOnEntityType(entityTypeId, Permission.WRITEMETA)).thenReturn(false);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId").getMock();
		when(entityType.getLabel()).thenReturn("Entity type").getMock();
		repo.delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting system entity meta data \\[entityTypeId\\] is not allowed")
	public void deleteSystemEntityType()
	{
		String entityTypeId = "entityTypeId";
		when(systemEntityTypeRegistry.hasSystemEntityType("entityTypeId")).thenReturn(true);
		when(permissionService.hasPermissionOnEntityType(entityTypeId, Permission.WRITEMETA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId").getMock();
		repo.delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void update()
	{
		String entityTypeId = "entityTypeId";
		when(permissionService.hasPermissionOnEntityType(entityTypeId, Permission.WRITEMETA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		repo.update(entityType);
		verify(delegateRepository).update(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITEMETA\\] permission on entity type \\[Entity type\\] with id \\[entityTypeId\\]")
	public void updateNotAllowed()
	{
		String entityTypeId = "entityTypeId";
		when(permissionService.hasPermissionOnEntityType(entityTypeId, Permission.WRITEMETA)).thenReturn(false);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		when(entityType.getLabel()).thenReturn("Entity type").getMock();
		repo.update(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Updating system entity meta data \\[Entity type\\] is not allowed")
	public void updateSystemEntityType()
	{
		String entityTypeId = "entityTypeId";
		when(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)).thenReturn(true);
		when(permissionService.hasPermissionOnEntityType(entityTypeId, Permission.WRITEMETA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		when(entityType.getLabel()).thenReturn("Entity type").getMock();
		repo.update(entityType);
	}

	static class Config
	{

	}
}