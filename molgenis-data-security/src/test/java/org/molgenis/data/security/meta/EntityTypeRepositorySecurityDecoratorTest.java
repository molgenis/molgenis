package org.molgenis.data.security.meta;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = { EntityTypeRepositorySecurityDecoratorTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class EntityTypeRepositorySecurityDecoratorTest extends AbstractMockitoTestNGSpringContextTests
{
	private static final String USERNAME = "user";

	@Mock
	private Repository delegateRepository;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private UserPermissionEvaluator permissionService;
	@Mock
	private MutableAclService mutableAclService;
	@Mock
	private MutableAclClassService mutableAclClassService;
	private EntityTypeRepositorySecurityDecorator repo;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repo = new EntityTypeRepositorySecurityDecorator(delegateRepository, systemEntityTypeRegistry,
				permissionService, mutableAclService, mutableAclClassService);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void count()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenReturn(
				asList(entityType0, entityType1).stream());
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.COUNT);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.COUNT);
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
		Query q = new QueryImpl<>();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityType0, entityType1));
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.COUNT);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.COUNT);
		assertEquals(repo.count(q), 1L);
		assertEquals(queryCaptor.getValue().getOffset(), 0);
		assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
	}

	private void findAllQuerySuOrSystem()
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		@SuppressWarnings("unchecked")
		Query q = mock(Query.class);
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
		Query q = mock(Query.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(
				Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.COUNT);
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.COUNT);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType2Name), EntityTypePermission.COUNT);
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
		Query q = mock(Query.class);
		when(q.getOffset()).thenReturn(1);
		when(q.getPageSize()).thenReturn(1);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(
				Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.COUNT);
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.COUNT);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType2Name), EntityTypePermission.COUNT);
		assertEquals(repo.findAll(q).collect(toList()), singletonList(entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
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
		when(delegateRepository.iterator()).thenReturn(asList(entityType0, entityType1, entityType2).iterator());
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.COUNT);
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.COUNT);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType2Name), EntityTypePermission.COUNT);
		assertEquals(newArrayList(repo.iterator()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQueryUserPermissionAllowed()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		@SuppressWarnings("unchecked")
		Query q = mock(Query.class);
		when(delegateRepository.findOne(q)).thenReturn(entityType0);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.COUNT)).thenReturn(true);
		assertEquals(repo.findOne(q), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQueryUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		@SuppressWarnings("unchecked")
		Query q = mock(Query.class);
		when(delegateRepository.findOne(q)).thenReturn(entityType0);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.COUNT)).thenReturn(false);
		assertNull(repo.findOne(q));
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
		EntityType entityType0 = mock(EntityType.class);
		when(delegateRepository.findOneById("entity0")).thenReturn(entityType0);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.COUNT)).thenReturn(true);
		assertEquals(repo.findOneById("entity0"), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		when(permissionService.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.COUNT)).thenReturn(false);
		assertNull(repo.findOneById(entityType0Name));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchUserPermissionAllowed()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = mock(EntityType.class);
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(entityType0Name, fetch)).thenReturn(entityType0);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.COUNT)).thenReturn(true);
		assertEquals(repo.findOneById(entityType0Name, fetch), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		Fetch fetch = mock(Fetch.class);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.COUNT)).thenReturn(false);
		assertNull(repo.findOneById(entityType0Name, fetch));
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
		Stream<Object> ids = Stream.of("entity0", "entity1");
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findAll(ids, fetch)).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.COUNT);
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.COUNT);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(entityType2Name), EntityTypePermission.COUNT);
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
	@Test(expectedExceptions = UnsupportedOperationException.class)
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
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.WRITEMETA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId").getMock();
		repo.delete(entityType);
		verify(mutableAclService).deleteAcl(new EntityTypeIdentity(entityTypeId), true);
		verify(mutableAclClassService).deleteAclClass("entity-" + entityTypeId);
		verify(delegateRepository).delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITEMETA\\] permission on entity type \\[Entity type\\] with id \\[entityTypeId\\]")
	public void deleteNotAllowed()
	{
		String entityTypeId = "entityTypeId";
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.WRITEMETA)).thenReturn(false);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId").getMock();
		when(entityType.getIdValue()).thenReturn("entityTypeId");
		when(entityType.getLabel()).thenReturn("Entity type").getMock();
		when(entityType.getEntityType()).thenReturn(entityType);
		repo.delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "No \\[WRITEMETA\\] permission on EntityType \\[entityTypeId\\]")
	public void deleteSystemEntityType()
	{
		String entityTypeId = "entityTypeId";
		when(systemEntityTypeRegistry.hasSystemEntityType("entityTypeId")).thenReturn(true);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.WRITEMETA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId").getMock();
		repo.delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void update()
	{
		String entityTypeId = "entityTypeId";
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.WRITEMETA)).thenReturn(true);

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
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.WRITEMETA)).thenReturn(false);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		when(entityType.getIdValue()).thenReturn(entityTypeId).getMock();
		when(entityType.getLabel()).thenReturn("Entity type").getMock();
		when(entityType.getEntityType()).thenReturn(entityType);
		repo.update(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "No \\[WRITEMETA\\] permission on EntityType \\[entityTypeId\\]")
	public void updateSystemEntityType()
	{
		String entityTypeId = "entityTypeId";
		when(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)).thenReturn(true);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.WRITEMETA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		repo.update(entityType);
	}

	static class Config
	{

	}
}