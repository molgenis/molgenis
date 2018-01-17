package org.molgenis.data.security.meta;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.auth.GroupAuthority;
import org.molgenis.data.security.auth.UserAuthority;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.data.security.exception.SystemMetadataAggregationException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.PermissionService;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.AuthorityMetaData.ROLE;
import static org.molgenis.data.security.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.security.core.Permission.COUNT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = { EntityTypeRepositorySecurityDecoratorTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class EntityTypeRepositorySecurityDecoratorTest extends AbstractMockitoTestNGSpringContextTests
{
	private static final String USERNAME = "user";
	private static final String USERNAME_SYSTEM = "SYSTEM";
	private static final String ROLE_SU = "SU";
	private static final String ROLE_SYSTEM = "SYSTEM";
	private static final String ROLE_WRITEMETA_ENTITY_TYPE = "ENTITY_WRITEMETA_entity";

	private final String entityTypeId1 = "EntityType1";
	private final String entityTypeId2 = "EntityType2";
	private final String entityTypeId3 = "EntityType3";
	private final String entityTypeId4 = "EntityType4";
	private EntityTypeRepositorySecurityDecorator repo;
	@Mock
	private Repository<EntityType> delegateRepository;
	@Mock
	private DataService dataService;
	@Mock
	private MetaDataService metaDataService;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private PermissionService permissionService;
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

	public EntityTypeRepositorySecurityDecoratorTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(entityType1.getId()).thenReturn(entityTypeId1);
		when(entityType1.getLabel()).thenReturn(entityTypeId1);
		when(entityType2.getId()).thenReturn(entityTypeId2);
		when(entityType2.getLabel()).thenReturn(entityTypeId1);
		when(entityType3.getId()).thenReturn(entityTypeId3);
		when(entityType3.getLabel()).thenReturn(entityTypeId1);
		when(entityType4.getId()).thenReturn(entityTypeId4);
		when(entityType4.getLabel()).thenReturn(entityTypeId1);

		when(delegateRepository.getEntityType()).thenReturn(entityType1);

		repo = new EntityTypeRepositorySecurityDecorator(delegateRepository, systemEntityTypeRegistry,
				permissionService, dataService);
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void countSu() throws Exception
	{
		countSuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void countSystem() throws Exception
	{
		countSuOrSystem();
	}

	private void countSuOrSystem() throws Exception
	{
		long count = 123L;
		when(delegateRepository.count()).thenReturn(count);
		assertEquals(repo.count(), 123L);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void countUser() throws Exception
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		when(delegateRepository.spliterator()).thenReturn(asList(entityType0, entityType1).spliterator());
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityType1Name, COUNT)).thenReturn(true);
		assertEquals(repo.count(), 1L);
	}

	@Test
	public void addWithKnownBackend()
	{
		SecurityContextHolder.getContext()
							 .setAuthentication(new TestingAuthenticationToken("anonymous", null, "ROLE_SU"));
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getAttributes()).thenReturn(emptyList());
		String backendName = "knownBackend";
		when(entityType.getBackend()).thenReturn(backendName);
		MetaDataService metaDataService = mock(MetaDataService.class);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getBackend(entityType)).thenReturn(repoCollection);
		when(dataService.getMeta()).thenReturn(metaDataService);
		repo.add(entityType);
		verify(delegateRepository).add(entityType);
	}

	@Test
	public void query() throws Exception
	{
		assertEquals(repo.query().getRepository(), repo);
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void countQuerySu() throws Exception
	{
		countQuerySuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void countQuerySystem() throws Exception
	{
		countQuerySuOrSystem();
	}

	private void countQuerySuOrSystem() throws Exception
	{
		long count = 123L;
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(delegateRepository.count(q)).thenReturn(count);
		assertEquals(repo.count(q), 123L);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void countQueryUser() throws Exception
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		Query<EntityType> q = new QueryImpl<>();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityType0, entityType1));
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityType1Name, COUNT)).thenReturn(true);
		assertEquals(repo.count(q), 1L);
		assertEquals(queryCaptor.getValue().getOffset(), 0);
		assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void findAllQuerySu() throws Exception
	{
		findAllQuerySuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void findAllQuerySystem() throws Exception
	{
		findAllQuerySuOrSystem();
	}

	private void findAllQuerySuOrSystem() throws Exception
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
	public void findAllQueryUser() throws Exception
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
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(true);
		when(permissionService.hasPermissionOnEntityType(entityType1Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityType2Name, COUNT)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), asList(entityType0, entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllQueryUserOffsetLimit() throws Exception
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
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(true);
		when(permissionService.hasPermissionOnEntityType(entityType1Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityType2Name, COUNT)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), singletonList(entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void iteratorSu() throws Exception
	{
		iteratorSuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void iteratorSystem() throws Exception
	{
		iteratorSuOrSystem();
	}

	private void iteratorSuOrSystem() throws Exception
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		when(delegateRepository.iterator()).thenReturn(asList(entityType0, entityType1).iterator());
		assertEquals(newArrayList(repo.iterator()), asList(entityType0, entityType1));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void iteratorUser() throws Exception
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		when(delegateRepository.spliterator()).thenReturn(asList(entityType0, entityType1, entityType2).spliterator());
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(true);
		when(permissionService.hasPermissionOnEntityType(entityType1Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityType2Name, COUNT)).thenReturn(true);
		assertEquals(newArrayList(repo.iterator()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void forEachBatchedSu() throws Exception
	{
		forEachBatchedSuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void forEachBatchedSystem() throws Exception
	{
		forEachBatchedSuOrSystem();
	}

	private void forEachBatchedSuOrSystem() throws Exception
	{
		Fetch fetch = mock(Fetch.class);
		@SuppressWarnings("unchecked")
		Consumer<List<EntityType>> consumer = mock(Consumer.class);
		repo.forEachBatched(fetch, consumer, 10);
		verify(delegateRepository).forEachBatched(fetch, consumer, 10);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void forEachBatchedUser() throws Exception
	{
		List<Entity> entities = newArrayList();
		repo.forEachBatched(entities::addAll, 2);

		when(permissionService.hasPermissionOnEntityType(entityTypeId1, COUNT)).thenReturn(true);
		when(permissionService.hasPermissionOnEntityType(entityTypeId2, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityTypeId3, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityTypeId4, COUNT)).thenReturn(true);

		// Decorated repo returns two batches of two entityTypes
		verify(delegateRepository).forEachBatched(eq(null), consumerCaptor.capture(), eq(2));
		consumerCaptor.getValue().accept(Lists.newArrayList(entityType1, entityType2));
		consumerCaptor.getValue().accept(Lists.newArrayList(entityType3, entityType4));

		assertEquals(entities, newArrayList(entityType1, entityType4));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void findOneQuerySu() throws Exception
	{
		findOneQuerySuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void findOneQuerySystem() throws Exception
	{
		findOneQuerySuOrSystem();
	}

	private void findOneQuerySuOrSystem() throws Exception
	{
		EntityType entityType0 = mock(EntityType.class);
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(delegateRepository.findOne(q)).thenReturn(entityType0);
		assertEquals(repo.findOne(q), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQueryUserPermissionAllowed() throws Exception
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
	public void findOneQueryUserPermissionDenied() throws Exception
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(delegateRepository.findOne(q)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(false);
		assertNull(repo.findOne(q));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void findOneByIdSu() throws Exception
	{
		findOneByIdSuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void findOneByIdSystem() throws Exception
	{
		findOneByIdSuOrSystem();
	}

	private void findOneByIdSuOrSystem() throws Exception
	{
		EntityType entityType0 = mock(EntityType.class);
		Object id = "0";
		when(delegateRepository.findOneById(id)).thenReturn(entityType0);
		assertEquals(repo.findOneById(id), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdUserPermissionAllowed() throws Exception
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
	public void findOneByIdUserPermissionDenied() throws Exception
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		when(delegateRepository.findOneById(id)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(false);
		assertNull(repo.findOneById(id));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void findOneByIdFetchSu() throws Exception
	{
		findOneByIdFetchSuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void findOneByIdFetchSystem() throws Exception
	{
		findOneByIdFetchSuOrSystem();
	}

	private void findOneByIdFetchSuOrSystem() throws Exception
	{
		EntityType entityType0 = mock(EntityType.class);
		Object id = "0";
		when(delegateRepository.findOneById(id)).thenReturn(entityType0);
		assertEquals(repo.findOneById(id), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchUserPermissionAllowed() throws Exception
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
	public void findOneByIdFetchUserPermissionDenied() throws Exception
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		Object id = "0";
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(id, fetch)).thenReturn(entityType0);
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(false);
		assertNull(repo.findOneById(id, fetch));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void findAllIdsSu() throws Exception
	{
		findAllIdsSuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void findAllIdsSystem() throws Exception
	{
		findAllIdsSuOrSystem();
	}

	private void findAllIdsSuOrSystem() throws Exception
	{
		EntityType entityType0 = mock(EntityType.class);
		EntityType entityType1 = mock(EntityType.class);
		Stream<Object> ids = Stream.of("0", "1");
		when(delegateRepository.findAll(ids)).thenReturn(Stream.of(entityType0, entityType1));
		assertEquals(repo.findAll(ids).collect(toList()), asList(entityType0, entityType1));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllIdsUser() throws Exception
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		Stream<Object> ids = Stream.of("0", "1");
		when(delegateRepository.findAll(ids)).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(true);
		when(permissionService.hasPermissionOnEntityType(entityType1Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityType2Name, COUNT)).thenReturn(true);
		assertEquals(repo.findAll(ids).collect(toList()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void findAllIdsFetchSu() throws Exception
	{
		findAllIdsFetchSuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void findAllIdsFetchSystem() throws Exception
	{
		findAllIdsFetchSuOrSystem();
	}

	private void findAllIdsFetchSuOrSystem() throws Exception
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
	public void findAllIdsFetchUser() throws Exception
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
		when(permissionService.hasPermissionOnEntityType(entityType0Name, COUNT)).thenReturn(true);
		when(permissionService.hasPermissionOnEntityType(entityType1Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntityType(entityType2Name, COUNT)).thenReturn(true);
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void aggregateSu() throws Exception
	{
		aggregateSuOrSystem();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void aggregateSystem() throws Exception
	{
		aggregateSuOrSystem();
	}

	private void aggregateSuOrSystem() throws Exception
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		assertEquals(repo.aggregate(aggregateQuery), aggregateResult);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = SystemMetadataAggregationException.class)
	public void aggregateUser() throws Exception
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repo.aggregate(aggregateQuery);
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void deleteSu()
	{
		delete();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void deleteSystem()
	{
		delete();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_WRITEMETA_ENTITY_TYPE })
	@Test
	public void deleteUser()
	{
		delete();
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "id:entity permission:WRITEMETA")
	public void deleteUserNotAllowed()
	{
		delete();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test(expectedExceptions = EditSystemEntityTypeException.class, expectedExceptionsMessageRegExp = "operation:DELETE entityType:entity")
	public void deleteSystemEntityType()
	{
		when(systemEntityTypeRegistry.hasSystemEntityType("entity")).thenReturn(true);
		delete();
	}

	private void delete()
	{
		@SuppressWarnings("unchecked")
		Query<UserAuthority> userAuthorityQ = mock(Query.class);
		when(userAuthorityQ.in(ROLE,
				newArrayList("ROLE_ENTITY_READ_entity", "ROLE_ENTITY_WRITE_entity", "ROLE_ENTITY_COUNT_entity",
						"ROLE_ENTITY_NONE_entity", "ROLE_ENTITY_WRITEMETA_entity"))).thenReturn(userAuthorityQ);
		UserAuthority userAuthority = mock(UserAuthority.class);
		when(userAuthorityQ.findAll()).thenReturn(singletonList(userAuthority).stream());
		when(dataService.query(USER_AUTHORITY, UserAuthority.class)).thenReturn(userAuthorityQ);
		@SuppressWarnings("unchecked")
		Query<GroupAuthority> groupAuthorityQ = mock(Query.class);
		when(groupAuthorityQ.in(ROLE,
				newArrayList("ROLE_ENTITY_READ_entity", "ROLE_ENTITY_WRITE_entity", "ROLE_ENTITY_COUNT_entity",
						"ROLE_ENTITY_NONE_entity", "ROLE_ENTITY_WRITEMETA_entity"))).thenReturn(groupAuthorityQ);
		GroupAuthority groupAuthority = mock(GroupAuthority.class);
		when(groupAuthorityQ.findAll()).thenReturn(singletonList(groupAuthority).stream());
		when(dataService.query(GROUP_AUTHORITY, GroupAuthority.class)).thenReturn(groupAuthorityQ);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entity").getMock();
		when(entityType.getLabel()).thenReturn("entity").getMock();
		repo.delete(entityType);
		verify(delegateRepository).delete(entityType);
		verifyNoMoreInteractions(delegateRepository);
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void updateSu()
	{
		update();
	}

	@WithMockUser(username = USERNAME_SYSTEM, roles = { ROLE_SYSTEM })
	@Test
	public void updateSystem()
	{
		update();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_WRITEMETA_ENTITY_TYPE })
	@Test
	public void updateUser()
	{
		update();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test(expectedExceptions = EditSystemEntityTypeException.class, expectedExceptionsMessageRegExp = "operation:UPDATE entityType:entity")
	public void updateSystemEntityType()
	{
		when(systemEntityTypeRegistry.hasSystemEntityType("entity")).thenReturn(true);
		update();
	}

	private void update()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entity").getMock();
		when(entityType.getLabel()).thenReturn("entity").getMock();
		repo.update(entityType);
		verify(delegateRepository).update(entityType);
		verifyNoMoreInteractions(delegateRepository);
	}

	static class Config
	{

	}
}