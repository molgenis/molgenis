package org.molgenis.data.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;
import static org.molgenis.security.core.runas.SystemSecurityToken.USER_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EntityMetaDataRepositoryDecoratorTest
{
	private EntityMetaDataRepositoryDecorator repo;
	private Repository<EntityMetaData> decoratedRepo;
	private DataService dataService;
	private SystemEntityMetaDataRegistry systemEntityMetaRegistry;
	private MolgenisPermissionService permissionService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		dataService = mock(DataService.class);
		systemEntityMetaRegistry = mock(SystemEntityMetaDataRegistry.class);
		permissionService = mock(MolgenisPermissionService.class);
		repo = new EntityMetaDataRepositoryDecorator(decoratedRepo, dataService, systemEntityMetaRegistry,
				permissionService);
	}

	@Test
	public void getCapabilities() throws Exception
	{
		Set<RepositoryCapability> decoratedRepoCapabilities = singleton(WRITABLE);
		when(decoratedRepo.getCapabilities()).thenReturn(decoratedRepoCapabilities);
		assertEquals(repo.getCapabilities(), decoratedRepoCapabilities);
	}

	@Test
	public void close() throws Exception
	{
		repo.close();
		verify(decoratedRepo, times(1)).close();
	}

	@Test
	public void getName() throws Exception
	{
		String name = "repoName";
		when(decoratedRepo.getName()).thenReturn(name);
		assertEquals(repo.getName(), name);
	}

	@Test
	public void getQueryOperators() throws Exception
	{
		Set<Operator> decoratedqueryOperators = singleton(EQUALS);
		when(decoratedRepo.getQueryOperators()).thenReturn(decoratedqueryOperators);
		assertEquals(repo.getQueryOperators(), decoratedqueryOperators);
	}

	@Test
	public void getEntityMetaData() throws Exception
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		assertEquals(repo.getEntityMetaData(), entityMeta);
	}

	@Test
	public void countSu() throws Exception
	{
		setSuAuthentication();
		countSuOrSystem();
	}

	@Test
	public void countSystem() throws Exception
	{
		setSystemAuthentication();
		countSuOrSystem();
	}

	private void countSuOrSystem() throws Exception
	{
		long count = 123L;
		when(decoratedRepo.count()).thenReturn(count);
		assertEquals(repo.count(), 123L);
	}

	@Test
	public void countUser() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		when(decoratedRepo.spliterator()).thenReturn(asList(entityMeta0, entityMeta1).spliterator());
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, COUNT)).thenReturn(true);
		assertEquals(repo.count(), 1L);
	}

	@Test
	public void query() throws Exception
	{
		Query q = mock(Query.class);
		when(decoratedRepo.query()).thenReturn(q);
		assertEquals(repo.query(), q);
	}

	@Test
	public void countQuerySu() throws Exception
	{
		setSuAuthentication();
		countQuerySuOrSystem();
	}

	@Test
	public void countQuerySystem() throws Exception
	{
		setSystemAuthentication();
		countQuerySuOrSystem();
	}

	private void countQuerySuOrSystem() throws Exception
	{
		long count = 123L;
		Query q = mock(Query.class);
		when(decoratedRepo.count(q)).thenReturn(count);
		assertEquals(repo.count(q), 123L);
	}

	@Test
	public void countQueryUser() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		Query<EntityMetaData> q = new QueryImpl<>();
		ArgumentCaptor<Query<EntityMetaData>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityMeta0, entityMeta1));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, COUNT)).thenReturn(true);
		assertEquals(repo.count(q), 1L);
		assertEquals(queryCaptor.getValue().getOffset(), 0);
		assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
	}

	@Test
	public void findAllQuerySu() throws Exception
	{
		setSuAuthentication();
		findAllQuerySuOrSystem();
	}

	@Test
	public void findAllQuerySystem() throws Exception
	{
		setSystemAuthentication();
		findAllQuerySuOrSystem();
	}

	private void findAllQuerySuOrSystem() throws Exception
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		Query q = mock(Query.class);
		when(decoratedRepo.findAll(q)).thenReturn(Stream.of(entityMeta0, entityMeta1));
		assertEquals(repo.findAll(q).collect(toList()), asList(entityMeta0, entityMeta1));
	}

	@Test
	public void findAllQueryUser() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		String entityMeta2Name = "entity2";
		EntityMetaData entityMeta2 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta2Name).getMock();
		Query<EntityMetaData> q = mock(Query.class);
		ArgumentCaptor<Query<EntityMetaData>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityMeta0, entityMeta1, entityMeta2));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta2Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), asList(entityMeta0, entityMeta2));
		Query<EntityMetaData> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@Test
	public void findAllQueryUserOffsetLimit() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		String entityMeta2Name = "entity2";
		EntityMetaData entityMeta2 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta2Name).getMock();
		Query<EntityMetaData> q = mock(Query.class);
		when(q.getOffset()).thenReturn(1);
		when(q.getPageSize()).thenReturn(1);
		ArgumentCaptor<Query<EntityMetaData>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityMeta0, entityMeta1, entityMeta2));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta2Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), singletonList(entityMeta2));
		Query<EntityMetaData> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@Test
	public void iteratorSu() throws Exception
	{
		setSuAuthentication();
		iteratorSuOrSystem();
	}

	@Test
	public void iteratorSystem() throws Exception
	{
		setSystemAuthentication();
		iteratorSuOrSystem();
	}

	private void iteratorSuOrSystem() throws Exception
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		when(decoratedRepo.iterator()).thenReturn(asList(entityMeta0, entityMeta1).iterator());
		assertEquals(newArrayList(repo.iterator()), asList(entityMeta0, entityMeta1));
	}

	@Test
	public void iteratorUser() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		String entityMeta2Name = "entity2";
		EntityMetaData entityMeta2 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta2Name).getMock();
		when(decoratedRepo.spliterator()).thenReturn(asList(entityMeta0, entityMeta1, entityMeta2).spliterator());
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta2Name, READ)).thenReturn(true);
		assertEquals(newArrayList(repo.iterator()), asList(entityMeta0, entityMeta2));
	}

	@Test
	public void forEachBatchedSu() throws Exception
	{
		setSuAuthentication();
		forEachBatchedSuOrSystem();
	}

	@Test
	public void forEachBatchedSystem() throws Exception
	{
		setSystemAuthentication();
		forEachBatchedSuOrSystem();
	}

	private void forEachBatchedSuOrSystem() throws Exception
	{
		Fetch fetch = mock(Fetch.class);
		Consumer<List<EntityMetaData>> consumer = mock(Consumer.class);
		repo.forEachBatched(fetch, consumer, 10);
		verify(decoratedRepo).forEachBatched(fetch, consumer, 10);
	}

	// TODO implement forEachBatchedUser unit test, but how?
	//	@Test
	//	public void forEachBatchedUser() throws Exception
	//	{
	//
	//	}

	@Test
	public void findOneQuerySu() throws Exception
	{
		setSuAuthentication();
		findOneQuerySuOrSystem();
	}

	@Test
	public void findOneQuerySystem() throws Exception
	{
		setSystemAuthentication();
		findOneQuerySuOrSystem();
	}

	private void findOneQuerySuOrSystem() throws Exception
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		Query q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(entityMeta0);
		assertEquals(repo.findOne(q), entityMeta0);
	}

	@Test
	public void findOneQueryUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(entityMeta0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		assertEquals(repo.findOne(q), entityMeta0);
	}

	@Test
	public void findOneQueryUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(entityMeta0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(false);
		assertNull(repo.findOne(q));
	}

	@Test
	public void findOneByIdSu() throws Exception
	{
		setSuAuthentication();
		findOneByIdSuOrSystem();
	}

	@Test
	public void findOneByIdSystem() throws Exception
	{
		setSystemAuthentication();
		findOneByIdSuOrSystem();
	}

	private void findOneByIdSuOrSystem() throws Exception
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(entityMeta0);
		assertEquals(repo.findOneById(id), entityMeta0);
	}

	@Test
	public void findOneByIdUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(entityMeta0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id), entityMeta0);
	}

	@Test
	public void findOneByIdUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(entityMeta0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(false);
		assertNull(repo.findOneById(id));
	}

	@Test
	public void findOneByIdFetchSu() throws Exception
	{
		setSuAuthentication();
		findOneByIdFetchSuOrSystem();
	}

	@Test
	public void findOneByIdFetchSystem() throws Exception
	{
		setSystemAuthentication();
		findOneByIdFetchSuOrSystem();
	}

	private void findOneByIdFetchSuOrSystem() throws Exception
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(entityMeta0);
		assertEquals(repo.findOneById(id), entityMeta0);
	}

	@Test
	public void findOneByIdFetchUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Object id = "0";
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(entityMeta0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id, fetch), entityMeta0);
	}

	@Test
	public void findOneByIdFetchUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Object id = "0";
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(entityMeta0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(false);
		assertNull(repo.findOneById(id, fetch));
	}

	@Test
	public void findAllIdsSu() throws Exception
	{
		setSuAuthentication();
		findAllIdsSuOrSystem();
	}

	@Test
	public void findAllIdsSystem() throws Exception
	{
		setSystemAuthentication();
		findAllIdsSuOrSystem();
	}

	private void findAllIdsSuOrSystem() throws Exception
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		Stream<Object> ids = Stream.of("0", "1");
		when(decoratedRepo.findAll(ids)).thenReturn(Stream.of(entityMeta0, entityMeta1));
		assertEquals(repo.findAll(ids).collect(toList()), asList(entityMeta0, entityMeta1));
	}

	@Test
	public void findAllIdsUser() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		String entityMeta2Name = "entity2";
		EntityMetaData entityMeta2 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta2Name).getMock();
		Stream<Object> ids = Stream.of("0", "1");
		when(decoratedRepo.findAll(ids)).thenReturn(Stream.of(entityMeta0, entityMeta1, entityMeta2));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta2Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(ids).collect(toList()), asList(entityMeta0, entityMeta2));
	}

	@Test
	public void findAllIdsFetchSu() throws Exception
	{
		setSuAuthentication();
		findAllIdsFetchSuOrSystem();
	}

	@Test
	public void findAllIdsFetchSystem() throws Exception
	{
		setSystemAuthentication();
		findAllIdsFetchSuOrSystem();
	}

	private void findAllIdsFetchSuOrSystem() throws Exception
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		Stream<Object> ids = Stream.of("0", "1");
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(Stream.of(entityMeta0, entityMeta1));
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(entityMeta0, entityMeta1));
	}

	@Test
	public void findAllIdsFetchUser() throws Exception
	{
		setUserAuthentication();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		String entityMeta2Name = "entity2";
		EntityMetaData entityMeta2 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta2Name).getMock();
		Stream<Object> ids = Stream.of("0", "1");
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(Stream.of(entityMeta0, entityMeta1, entityMeta2));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta2Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(entityMeta0, entityMeta2));
	}

	@Test
	public void aggregateSu() throws Exception
	{
		setSuAuthentication();
		aggregateSuOrSystem();
	}

	@Test
	public void aggregateSystem() throws Exception
	{
		setSystemAuthentication();
		aggregateSuOrSystem();
	}

	@Test
	public void aggregateSuOrSystem() throws Exception
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(decoratedRepo.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		assertEquals(repo.aggregate(aggregateQuery), aggregateResult);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void aggregateUser() throws Exception
	{
		setUserAuthentication();
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repo.aggregate(aggregateQuery);
	}

	private static void setSuAuthentication()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("su", null, AUTHORITY_SU);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private static void setSystemAuthentication()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(USER_SYSTEM, null, ROLE_SYSTEM);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private static void setUserAuthentication()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", null, "ROLE_USER");
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}