package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
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
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.auth.AuthorityMetaData.ROLE;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;
import static org.molgenis.security.core.runas.SystemSecurityToken.USER_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EntityTypeRepositoryDecoratorTest
{
	private final String entityName1 = "EntityType1";
	private final String entityName2 = "EntityType2";
	private final String entityName3 = "EntityType3";
	private final String entityName4 = "EntityType4";
	private EntityTypeRepositoryDecorator repo;
	@Mock
	private Repository<EntityType> decoratedRepo;
	@Mock
	private DataService dataService;
	@Mock
	private MetaDataService metaDataService;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private MolgenisPermissionService permissionService;
	@Captor
	private ArgumentCaptor<Consumer<List<EntityType>>> consumerCaptor;
	@Mock
	private EntityType entityType1;
	@Mock
	private EntityType entityType2;
	@Mock
	private EntityType entityType3;
	@Mock
	private EntityType entityType4;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(entityType1.getName()).thenReturn(entityName1);
		when(entityType2.getName()).thenReturn(entityName2);
		when(entityType3.getName()).thenReturn(entityName3);
		when(entityType4.getName()).thenReturn(entityName4);
		repo = new EntityTypeRepositoryDecorator(decoratedRepo, dataService, systemEntityTypeRegistry,
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
	public void getEntityType() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		when(decoratedRepo.getEntityType()).thenReturn(entityType);
		assertEquals(repo.getEntityType(), entityType);
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
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		when(decoratedRepo.spliterator()).thenReturn(asList(entityType0, entityType1).spliterator());
		when(permissionService.hasPermissionOnEntity(entityType0Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, COUNT)).thenReturn(true);
		assertEquals(repo.count(), 1L);
	}

	@Test
	public void addWithKnownBackend()
	{
		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("anonymous", null, "ROLE_SU"));
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		when(entityType.getAttributes()).thenReturn(emptyList());
		String backendName = "knownBackend";
		when(entityType.getBackend()).thenReturn(backendName);
		MetaDataService metaDataService = mock(MetaDataService.class);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getBackend(entityType)).thenReturn(repoCollection);
		when(dataService.getMeta()).thenReturn(metaDataService);
		repo.add(entityType);
		verify(decoratedRepo).add(entityType);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Unknown backend \\[unknownBackend\\]")
	public void addWithUnknownBackend()
	{
		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("anonymous", null, "ROLE_SU"));
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		when(entityType.getAttributes()).thenReturn(emptyList());
		String backendName = "unknownBackend";
		when(entityType.getBackend()).thenReturn(backendName);
		MetaDataService metaDataService = mock(MetaDataService.class);
		when(metaDataService.getBackend(backendName)).thenReturn(null);
		when(dataService.getMeta()).thenReturn(metaDataService);
		repo.add(entityType);
		verify(decoratedRepo).add(entityType);
	}

	@Test
	public void query() throws Exception
	{
		assertEquals(repo.query().getRepository(), repo);
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
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		Query<EntityType> q = new QueryImpl<>();
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityType0, entityType1));
		when(permissionService.hasPermissionOnEntity(entityType0Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, COUNT)).thenReturn(true);
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
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		Query q = mock(Query.class);
		when(decoratedRepo.findAll(q)).thenReturn(Stream.of(entityType0, entityType1));
		assertEquals(repo.findAll(q).collect(toList()), asList(entityType0, entityType1));
	}

	@Test
	public void findAllQueryUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getName()).thenReturn(entityType2Name).getMock();
		Query<EntityType> q = mock(Query.class);
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType2Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), asList(entityType0, entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@Test
	public void findAllQueryUserOffsetLimit() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getName()).thenReturn(entityType2Name).getMock();
		Query<EntityType> q = mock(Query.class);
		when(q.getOffset()).thenReturn(1);
		when(q.getPageSize()).thenReturn(1);
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType2Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), singletonList(entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
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
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		when(decoratedRepo.iterator()).thenReturn(asList(entityType0, entityType1).iterator());
		assertEquals(newArrayList(repo.iterator()), asList(entityType0, entityType1));
	}

	@Test
	public void iteratorUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getName()).thenReturn(entityType2Name).getMock();
		when(decoratedRepo.spliterator()).thenReturn(asList(entityType0, entityType1, entityType2).spliterator());
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType2Name, READ)).thenReturn(true);
		assertEquals(newArrayList(repo.iterator()), asList(entityType0, entityType2));
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
		Consumer<List<EntityType>> consumer = mock(Consumer.class);
		repo.forEachBatched(fetch, consumer, 10);
		verify(decoratedRepo).forEachBatched(fetch, consumer, 10);
	}

	@Test
	public void forEachBatchedUser() throws Exception
	{
		setUserAuthentication();

		List<Entity> entities = newArrayList();
		repo.forEachBatched(entities::addAll, 2);

		when(permissionService.hasPermissionOnEntity(entityName1, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityName2, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityName3, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityName4, READ)).thenReturn(true);

		// Decorated repo returns two batches of two entityTypes
		verify(decoratedRepo).forEachBatched(eq(null), consumerCaptor.capture(), eq(2));
		consumerCaptor.getValue().accept(Lists.newArrayList(entityType1, entityType2));
		consumerCaptor.getValue().accept(Lists.newArrayList(entityType3, entityType4));

		assertEquals(entities, newArrayList(entityType1, entityType4));
	}

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
		EntityType entityType0 = mock(EntityType.class);
		Query q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(entityType0);
		assertEquals(repo.findOne(q), entityType0);
	}

	@Test
	public void findOneQueryUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOne(q), entityType0);
	}

	@Test
	public void findOneQueryUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(false);
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
		EntityType entityType0 = mock(EntityType.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(entityType0);
		assertEquals(repo.findOneById(id), entityType0);
	}

	@Test
	public void findOneByIdUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id), entityType0);
	}

	@Test
	public void findOneByIdUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(false);
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
		EntityType entityType0 = mock(EntityType.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(entityType0);
		assertEquals(repo.findOneById(id), entityType0);
	}

	@Test
	public void findOneByIdFetchUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id, fetch), entityType0);
	}

	@Test
	public void findOneByIdFetchUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(false);
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
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		Stream<Object> ids = Stream.of("0", "1");
		when(decoratedRepo.findAll(ids)).thenReturn(Stream.of(entityType0, entityType1));
		assertEquals(repo.findAll(ids).collect(toList()), asList(entityType0, entityType1));
	}

	@Test
	public void findAllIdsUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getName()).thenReturn(entityType2Name).getMock();
		Stream<Object> ids = Stream.of("0", "1");
		when(decoratedRepo.findAll(ids)).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType2Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(ids).collect(toList()), asList(entityType0, entityType2));
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
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		Stream<Object> ids = Stream.of("0", "1");
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(Stream.of(entityType0, entityType1));
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(entityType0, entityType1));
	}

	@Test
	public void findAllIdsFetchUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getName()).thenReturn(entityType2Name).getMock();
		Stream<Object> ids = Stream.of("0", "1");
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType2Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(entityType0, entityType2));
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

	private void aggregateSuOrSystem() throws Exception
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

	@Test
	public void deleteSu()
	{
		setSuAuthentication();
		deleteSuOrSystem();
	}

	@Test
	public void deleteSystem()
	{
		setSystemAuthentication();
		deleteSuOrSystem();
	}

	private void deleteSuOrSystem()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getChildren()).thenReturn(emptyList());
		Attribute attrCompound = mock(Attribute.class);
		Attribute attr1a = mock(Attribute.class);
		when(attr1a.getChildren()).thenReturn(emptyList());
		Attribute attr1b = mock(Attribute.class);
		when(attr1b.getChildren()).thenReturn(emptyList());
		when(attrCompound.getChildren()).thenReturn(newArrayList(attr1a, attr1b));
		when(entityType.getOwnAttributes()).thenReturn(newArrayList(attr0, attrCompound));

		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getBackend(backendName)).thenReturn(repoCollection);
		//noinspection unchecked
		Query<UserAuthority> userAuthorityQ = mock(Query.class);
		when(userAuthorityQ.in(ROLE,
				newArrayList("ROLE_ENTITY_READ_entity", "ROLE_ENTITY_WRITE_entity", "ROLE_ENTITY_COUNT_entity",
						"ROLE_ENTITY_NONE_entity", "ROLE_ENTITY_WRITEMETA_entity"))).thenReturn(userAuthorityQ);
		UserAuthority userAuthority = mock(UserAuthority.class);
		when(userAuthorityQ.findAll()).thenReturn(singletonList(userAuthority).stream());
		when(dataService.query(USER_AUTHORITY, UserAuthority.class)).thenReturn(userAuthorityQ);

		//noinspection unchecked
		Query<GroupAuthority> groupAuthorityQ = mock(Query.class);
		when(groupAuthorityQ.in(ROLE,
				newArrayList("ROLE_ENTITY_READ_entity", "ROLE_ENTITY_WRITE_entity", "ROLE_ENTITY_COUNT_entity",
						"ROLE_ENTITY_NONE_entity", "ROLE_ENTITY_WRITEMETA_entity"))).thenReturn(groupAuthorityQ);
		GroupAuthority groupAuthority = mock(GroupAuthority.class);
		when(groupAuthorityQ.findAll()).thenReturn(singletonList(groupAuthority).stream());
		when(dataService.query(GROUP_AUTHORITY, GroupAuthority.class)).thenReturn(groupAuthorityQ);

		repo.delete(entityType);

		verify(decoratedRepo).delete(entityType);
		verify(repoCollection).deleteRepository(entityType);

		//noinspection unchecked
		ArgumentCaptor<Stream<UserAuthority>> userAuthorityCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(USER_AUTHORITY), userAuthorityCaptor.capture());
		assertEquals(userAuthorityCaptor.getValue().collect(toList()), singletonList(userAuthority));

		//noinspection unchecked
		ArgumentCaptor<Stream<GroupAuthority>> groupAuthorityCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(GROUP_AUTHORITY), groupAuthorityCaptor.capture());
		assertEquals(groupAuthorityCaptor.getValue().collect(toList()), singletonList(groupAuthority));

		//noinspection unchecked
		ArgumentCaptor<Stream<Attribute>> attrCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrCaptor.capture());
		assertEquals(attrCaptor.getValue().collect(toList()), newArrayList(attr0, attrCompound, attr1a, attr1b));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITEMETA\\] permission on entity \\[entity\\]")
	public void deleteUser()
	{
		setUserAuthentication();
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		repo.delete(entityType);
	}

	@Test
	public void deleteAbstract()
	{
		setSystemAuthentication();
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.isAbstract()).thenReturn(true);
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getChildren()).thenReturn(emptyList());
		when(entityType.getOwnAttributes()).thenReturn(singletonList(attr0));

		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getBackend(backendName)).thenReturn(repoCollection);
		//noinspection unchecked
		Query<UserAuthority> userAuthorityQ = mock(Query.class);
		when(userAuthorityQ.in(ROLE,
				newArrayList("ROLE_ENTITY_READ_entity", "ROLE_ENTITY_WRITE_entity", "ROLE_ENTITY_COUNT_entity",
						"ROLE_ENTITY_NONE_entity", "ROLE_ENTITY_WRITEMETA_entity"))).thenReturn(userAuthorityQ);
		UserAuthority userAuthority = mock(UserAuthority.class);
		when(userAuthorityQ.findAll()).thenReturn(singletonList(userAuthority).stream());
		when(dataService.query(USER_AUTHORITY, UserAuthority.class)).thenReturn(userAuthorityQ);

		//noinspection unchecked
		Query<GroupAuthority> groupAuthorityQ = mock(Query.class);
		when(groupAuthorityQ.in(ROLE,
				newArrayList("ROLE_ENTITY_READ_entity", "ROLE_ENTITY_WRITE_entity", "ROLE_ENTITY_COUNT_entity",
						"ROLE_ENTITY_NONE_entity", "ROLE_ENTITY_WRITEMETA_entity"))).thenReturn(groupAuthorityQ);
		GroupAuthority groupAuthority = mock(GroupAuthority.class);
		when(groupAuthorityQ.findAll()).thenReturn(singletonList(groupAuthority).stream());
		when(dataService.query(GROUP_AUTHORITY, GroupAuthority.class)).thenReturn(groupAuthorityQ);

		repo.delete(entityType);

		verify(decoratedRepo).delete(entityType);
		verify(repoCollection, times(0)).deleteRepository(entityType); // entity is abstract

		//noinspection unchecked
		ArgumentCaptor<Stream<UserAuthority>> userAuthorityCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(USER_AUTHORITY), userAuthorityCaptor.capture());
		assertEquals(userAuthorityCaptor.getValue().collect(toList()), singletonList(userAuthority));

		//noinspection unchecked
		ArgumentCaptor<Stream<GroupAuthority>> groupAuthorityCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(GROUP_AUTHORITY), groupAuthorityCaptor.capture());
		assertEquals(groupAuthorityCaptor.getValue().collect(toList()), singletonList(groupAuthority));

		//noinspection unchecked
		ArgumentCaptor<Stream<Attribute>> attrCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrCaptor.capture());
		assertEquals(attrCaptor.getValue().collect(toList()), singletonList(attr0));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting system entity meta data \\[entity\\] is not allowed")
	public void deleteSystemEntity()
	{
		setSystemAuthentication();
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		when(entityType.isAbstract()).thenReturn(true);
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getChildren()).thenReturn(emptyList());
		when(entityType.getOwnAttributes()).thenReturn(singletonList(attr0));
		when(systemEntityTypeRegistry.hasSystemEntityType(entityName)).thenReturn(true);

		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getBackend(backendName)).thenReturn(repoCollection);
		//noinspection unchecked
		Query<UserAuthority> userAuthorityQ = mock(Query.class);
		when(userAuthorityQ.in(ROLE,
				newArrayList("ROLE_ENTITY_READ_entity", "ROLE_ENTITY_WRITE_entity", "ROLE_ENTITY_COUNT_entity",
						"ROLE_ENTITY_NONE_entity", "ROLE_ENTITY_WRITEMETA_entity"))).thenReturn(userAuthorityQ);
		UserAuthority userAuthority = mock(UserAuthority.class);
		when(userAuthorityQ.findAll()).thenReturn(singletonList(userAuthority).stream());
		when(dataService.query(USER_AUTHORITY, UserAuthority.class)).thenReturn(userAuthorityQ);

		//noinspection unchecked
		Query<GroupAuthority> groupAuthorityQ = mock(Query.class);
		when(groupAuthorityQ.in(ROLE,
				newArrayList("ROLE_ENTITY_READ_entity", "ROLE_ENTITY_WRITE_entity", "ROLE_ENTITY_COUNT_entity",
						"ROLE_ENTITY_NONE_entity", "ROLE_ENTITY_WRITEMETA_entity"))).thenReturn(groupAuthorityQ);
		GroupAuthority groupAuthority = mock(GroupAuthority.class);
		when(groupAuthorityQ.findAll()).thenReturn(singletonList(groupAuthority).stream());
		when(dataService.query(GROUP_AUTHORITY, GroupAuthority.class)).thenReturn(groupAuthorityQ);

		repo.delete(entityType);

		verify(decoratedRepo).delete(entityType);
		verify(repoCollection, times(0)).deleteRepository(entityType); // entity is abstract

		//noinspection unchecked
		ArgumentCaptor<Stream<UserAuthority>> userAuthorityCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(USER_AUTHORITY), userAuthorityCaptor.capture());
		assertEquals(userAuthorityCaptor.getValue().collect(toList()), singletonList(userAuthority));

		//noinspection unchecked
		ArgumentCaptor<Stream<GroupAuthority>> groupAuthorityCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(GROUP_AUTHORITY), groupAuthorityCaptor.capture());
		assertEquals(groupAuthorityCaptor.getValue().collect(toList()), singletonList(groupAuthority));

		//noinspection unchecked
		ArgumentCaptor<Stream<Attribute>> attrCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrCaptor.capture());
		assertEquals(attrCaptor.getValue().collect(toList()), singletonList(attr0));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Updating system entity meta data \\[EntityType1\\] is not allowed")
	public void updateSystemEntityType()
	{
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityTypeRegistry.getSystemEntityType(entityName1)).thenReturn(systemEntityType);

		setSuAuthentication();
		repo.update(entityType1);
	}

	@Test
	public void update()
	{
		when(entityType1.getIdValue()).thenReturn(entityName1);
		when(entityType2.getIdValue()).thenReturn(entityName2);
		when(entityType3.getIdValue()).thenReturn(entityName3);
		when(entityType4.getIdValue()).thenReturn(entityName4);

		EntityType currentEntityType = mock(EntityType.class);
		EntityType currentEntityType2 = mock(EntityType.class);
		EntityType currentEntityType3 = mock(EntityType.class);
		when(systemEntityTypeRegistry.getSystemEntityType(entityName1)).thenReturn(null);
		when(decoratedRepo.findOneById(entityName1)).thenReturn(currentEntityType);
		when(decoratedRepo.findOneById(entityName2)).thenReturn(currentEntityType2);
		when(decoratedRepo.findOneById(entityName3)).thenReturn(currentEntityType3);

		Attribute attributeStays = mock(Attribute.class);
		when(attributeStays.getName()).thenReturn("attributeStays");
		Attribute attributeRemoved = mock(Attribute.class);
		when(attributeRemoved.getName()).thenReturn("attributeRemoved");
		Attribute attributeAdded = mock(Attribute.class);
		when(attributeAdded.getName()).thenReturn("attributeAdded");

		when(currentEntityType.getOwnAllAttributes()).thenReturn(Lists.newArrayList(attributeStays, attributeRemoved));
		when(entityType1.getOwnAllAttributes()).thenReturn(Lists.newArrayList(attributeStays, attributeAdded));
		when(metaDataService.getConcreteChildren(entityType1)).thenReturn(Stream.of(entityType2, entityType3));
		RepositoryCollection backend2 = mock(RepositoryCollection.class);
		RepositoryCollection backend3 = mock(RepositoryCollection.class);
		when(metaDataService.getBackend(entityType2)).thenReturn(backend2);
		when(metaDataService.getBackend(entityType3)).thenReturn(backend3);

		setSuAuthentication();
		repo.update(entityType1);

		// verify that attributes got added and deleted in concrete extending entities
		verify(backend2).addAttribute(currentEntityType2, attributeAdded);
		verify(backend2).deleteAttribute(currentEntityType2, attributeRemoved);
		verify(backend3).addAttribute(currentEntityType3, attributeAdded);
		verify(backend3).deleteAttribute(currentEntityType3, attributeRemoved);
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