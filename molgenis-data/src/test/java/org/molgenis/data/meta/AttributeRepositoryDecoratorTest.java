package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
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
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.CHILDREN;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;
import static org.molgenis.security.core.runas.SystemSecurityToken.USER_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class AttributeRepositoryDecoratorTest
{
	private AttributeRepositoryDecorator repo;
	@Mock
	private Repository<Attribute> decoratedRepo;
	@Mock
	private DataService dataService;
	@Mock
	private MetaDataService metadataService;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private MolgenisPermissionService permissionService;
	@Mock
	private Attribute attribute;
	@Mock
	private EntityType abstractEntityType;
	@Mock
	private EntityType concreteEntityType1;
	@Mock
	private EntityType concreteEntityType2;
	@Mock
	private RepositoryCollection backend1;
	@Mock
	private RepositoryCollection backend2;
	private String attributeId = "SDFSADFSDAF";
	@Captor
	private ArgumentCaptor<Consumer<List<Attribute>>> consumerCaptor;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		when(attribute.getEntity()).thenReturn(abstractEntityType);
		when(attribute.getName()).thenReturn("attributeName");
		when(dataService.getMeta()).thenReturn(metadataService);
		when(metadataService.getConcreteChildren(abstractEntityType))
				.thenReturn(Stream.of(concreteEntityType1, concreteEntityType2));
		when(metadataService.getBackend(concreteEntityType1)).thenReturn(backend1);
		when(metadataService.getBackend(concreteEntityType2)).thenReturn(backend2);
		when(attribute.getIdentifier()).thenReturn(attributeId);
		repo = new AttributeRepositoryDecorator(decoratedRepo, systemEntityTypeRegistry, dataService,
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
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType1Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		when(attr1.getEntity()).thenReturn(entityType1);
		when(decoratedRepo.spliterator()).thenReturn(asList(attr0, attr1).spliterator());
		when(permissionService.hasPermissionOnEntity(entityType0Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, COUNT)).thenReturn(true);
		assertEquals(repo.count(), 1L);
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
		@SuppressWarnings("unchecked")
		Query<Attribute> q = mock(Query.class);
		when(decoratedRepo.count(q)).thenReturn(count);
		assertEquals(repo.count(q), 123L);
	}

	@Test
	public void countQueryUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType1Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		when(attr1.getEntity()).thenReturn(entityType1);
		Query<Attribute> q = new QueryImpl<>();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<Attribute>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
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
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		@SuppressWarnings("unchecked")
		Query<Attribute> q = mock(Query.class);
		when(decoratedRepo.findAll(q)).thenReturn(Stream.of(attr0, attr1));
		assertEquals(repo.findAll(q).collect(toList()), asList(attr0, attr1));
	}

	@Test
	public void findAllQueryUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType1Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		when(attr1.getEntity()).thenReturn(entityType1);
		Query<Attribute> q = new QueryImpl<>();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<Attribute>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), singletonList(attr1));
		assertEquals(queryCaptor.getValue().getOffset(), 0);
		assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
	}

	@Test
	public void findAllQueryUserOffsetLimit() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType1Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		when(attr1.getEntity()).thenReturn(entityType1);
		@SuppressWarnings("unchecked")
		Query<Attribute> q = mock(Query.class);
		when(q.getOffset()).thenReturn(1);
		when(q.getPageSize()).thenReturn(1);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<Attribute>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), emptyList());
		assertEquals(queryCaptor.getValue().getOffset(), 0);
		assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
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
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		when(decoratedRepo.iterator()).thenReturn(asList(attr0, attr1).iterator());
		assertEquals(newArrayList(repo.iterator()), asList(attr0, attr1));
	}

	@Test
	public void iteratorUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType1Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		when(attr1.getEntity()).thenReturn(entityType1);
		when(decoratedRepo.spliterator()).thenReturn(asList(attr0, attr1).spliterator());
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(true);
		assertEquals(newArrayList(repo.iterator()), singletonList(attr1));
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
		@SuppressWarnings("unchecked")
		Consumer<List<Attribute>> consumer = mock(Consumer.class);
		repo.forEachBatched(fetch, consumer, 10);
		verify(decoratedRepo).forEachBatched(fetch, consumer, 10);
	}

	@Test
	public void forEachBatchedUser() throws Exception
	{
		setUserAuthentication();

		List<Attribute> attributes = newArrayList();
		Attribute attribute1 = mock(Attribute.class);
		Attribute attribute2 = mock(Attribute.class);
		Attribute attribute3 = mock(Attribute.class);
		Attribute attribute4 = mock(Attribute.class);

		EntityType entityType1 = mock(EntityType.class);
		EntityType entityType2 = mock(EntityType.class);
		EntityType entityType3 = mock(EntityType.class);
		EntityType entityType4 = mock(EntityType.class);

		when(attribute1.getEntity()).thenReturn(entityType1);
		when(attribute2.getEntity()).thenReturn(entityType2);
		when(attribute3.getEntity()).thenReturn(entityType3);
		when(attribute4.getEntity()).thenReturn(entityType4);

		when(entityType1.getFullyQualifiedName()).thenReturn("EntityType1");
		when(entityType2.getFullyQualifiedName()).thenReturn("EntityType2");
		when(entityType3.getFullyQualifiedName()).thenReturn("EntityType3");
		when(entityType4.getFullyQualifiedName()).thenReturn("EntityType4");

		repo.forEachBatched(attributes::addAll, 2);

		when(permissionService.hasPermissionOnEntity("EntityType1", READ)).thenReturn(true);
		when(permissionService.hasPermissionOnEntity("EntityType2", READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity("EntityType3", READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity("EntityType4", READ)).thenReturn(true);

		// Decorated repo returns two batches of two entityTypes
		verify(decoratedRepo).forEachBatched(eq(null), consumerCaptor.capture(), eq(2));
		consumerCaptor.getValue().accept(Lists.newArrayList(attribute1, attribute2));
		consumerCaptor.getValue().accept(Lists.newArrayList(attribute3, attribute4));

		assertEquals(attributes, newArrayList(attribute1, attribute4));
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
		Attribute attr0 = mock(Attribute.class);
		@SuppressWarnings("unchecked")
		Query<Attribute> q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(attr0);
		assertEquals(repo.findOne(q), attr0);
	}

	@Test
	public void findOneQueryUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		Query<Attribute> q = new QueryImpl<>();
		when(decoratedRepo.findOne(q)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOne(q), attr0);
	}

	@Test
	public void findOneQueryUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		Query<Attribute> q = new QueryImpl<>();
		when(decoratedRepo.findOne(q)).thenReturn(attr0);
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
		Attribute attr0 = mock(Attribute.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionAllowedAttrInCompoundWithOneAttr() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String attr0Name = "entity0attrCompoundattr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		String attrCompoundName = "entity0attrCompound";
		Attribute attrCompound = when(mock(Attribute.class).getName()).thenReturn(attrCompoundName).getMock();
		when(attrCompound.getEntity()).thenReturn(entityType0);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
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
		Attribute attr0 = mock(Attribute.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdFetchUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		Object id = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id, fetch), attr0);
	}

	@Test
	public void findOneByIdFetchUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		Object id = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(attr0);
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
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		Stream<Object> ids = Stream.of("0", "1");
		when(decoratedRepo.findAll(ids)).thenReturn(Stream.of(attr0, attr1));
		assertEquals(repo.findAll(ids).collect(toList()), asList(attr0, attr1));
	}

	@Test
	public void findAllIdsUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType1Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		when(attr1.getEntity()).thenReturn(entityType1);
		Stream<Object> ids = Stream.of(mock(Object.class), mock(Object.class));
		when(decoratedRepo.findAll(ids)).thenReturn(Stream.of(attr0, attr1));
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(ids).collect(toList()), singletonList(attr1));
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
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		Stream<Object> ids = Stream.of("0", "1");
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(Stream.of(attr0, attr1));
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(attr0, attr1));
	}

	@Test
	public void findAllIdsFetchUser() throws Exception
	{
		setUserAuthentication();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType0Name)
				.getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityType1Name)
				.getMock();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getEntity()).thenReturn(entityType0);
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		when(attr1.getEntity()).thenReturn(entityType1);
		Stream<Object> ids = Stream.of(mock(Object.class), mock(Object.class));
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(Stream.of(attr0, attr1));
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(ids, fetch).collect(toList()), singletonList(attr1));
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

	@Test
	public void delete()
	{
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityTypeRegistry.hasSystemAttribute(attrIdentifier)).thenReturn(false);

		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);

		@SuppressWarnings("unchecked")
		Query<Attribute> attrQ = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(attrQ);
		when(attrQ.eq(CHILDREN, attr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(null);

		repo.delete(attr);

		verify(decoratedRepo).delete(attr);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteCompoundAttribute()
	{
		// Compound parent attribute
		Attribute compound = when(mock(Attribute.class).getName()).thenReturn("compound").getMock();
		when(compound.getDataType()).thenReturn(AttributeType.COMPOUND);

		// Child
		Attribute child = when(mock(Attribute.class).getName()).thenReturn("child").getMock();
		when(compound.getChildren()).thenReturn(newArrayList(child));
		when(child.getParent()).thenReturn(mock(Attribute.class));
		MetaDataService mds = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(mds);
		when(mds.getRepository(AttributeMetadata.ATTRIBUTE_META_DATA)).thenReturn(mock(Repository.class));

		repo.delete(compound);

		//Test
		verify(child).setParent(null);
		verify(decoratedRepo).delete(compound);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting system entity attribute \\[attrName\\] is not allowed")
	public void deleteSystemAttribute()
	{
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityTypeRegistry.hasSystemAttribute(attrIdentifier)).thenReturn(true);
		repo.delete(attr);
	}

	@Test
	public void deleteStream()
	{
		AttributeRepositoryDecorator repoSpy = spy(repo);
		doNothing().when(repoSpy).delete(any(Attribute.class));
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		repoSpy.delete(Stream.of(attr0, attr1));
		verify(repoSpy).delete(attr0);
		verify(repoSpy).delete(attr1);
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

	@Test
	public void updateNonSystemAbstractEntity()
	{
		setSuAuthentication();

		Attribute currentAttribute = mock(Attribute.class);
		when(systemEntityTypeRegistry.getSystemAttribute(attributeId)).thenReturn(null);
		when(decoratedRepo.findOneById(attributeId)).thenReturn(currentAttribute);
		when(currentAttribute.getEntity()).thenReturn(abstractEntityType);

		repo.update(attribute);

		verify(decoratedRepo).update(attribute);
		verify(backend1).updateAttribute(concreteEntityType1, currentAttribute, attribute);
		verify(backend2).updateAttribute(concreteEntityType2, currentAttribute, attribute);
	}

	@Test(expectedExceptions = {
			MolgenisDataException.class }, expectedExceptionsMessageRegExp = "Updating system entity attribute \\[attributeName\\] is not allowed")
	public void updateSystemEntity()
	{
		setSuAuthentication();

		Attribute currentAttribute = mock(Attribute.class);
		when(systemEntityTypeRegistry.getSystemAttribute(attributeId)).thenReturn(currentAttribute);

		repo.update(attribute);

	}
}