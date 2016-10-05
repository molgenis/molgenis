package org.molgenis.data.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetaData;
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
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.PARTS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_META_DATA;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;
import static org.molgenis.security.core.runas.SystemSecurityToken.USER_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class AttributeMetaDataRepositoryDecoratorTest
{
	private AttributeMetaDataRepositoryDecorator repo;
	private Repository<AttributeMetaData> decoratedRepo;
	private DataService dataService;
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	private MolgenisPermissionService permissionService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		dataService = mock(DataService.class);
		systemEntityTypeRegistry = mock(SystemEntityTypeRegistry.class);
		permissionService = mock(MolgenisPermissionService.class);
		repo = new AttributeMetaDataRepositoryDecorator(decoratedRepo, systemEntityTypeRegistry, dataService,
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
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr1Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		Query<EntityType> qEntityType0 = mock(Query.class);
		Query<EntityType> qEntityType1 = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType0);
		when(qEntityType.eq(ATTRIBUTES, attr1)).thenReturn(qEntityType1);
		when(qEntityType0.findAll()).thenReturn(Stream.of(entityType0));
		when(qEntityType1.findAll()).thenReturn(Stream.of(entityType1));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		Query<AttributeMetaData> qAttr0 = mock(Query.class);
		Query<AttributeMetaData> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
		when(decoratedRepo.spliterator()).thenReturn(asList(attr0, attr1).spliterator());
		when(permissionService.hasPermissionOnEntity(entityType0Name, COUNT)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityType1Name, COUNT)).thenReturn(true);
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
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr1Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		Query<EntityType> qEntityType0 = mock(Query.class);
		Query<EntityType> qEntityType1 = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType0);
		when(qEntityType.eq(ATTRIBUTES, attr1)).thenReturn(qEntityType1);
		when(qEntityType0.findAll()).thenReturn(Stream.of(entityType0));
		when(qEntityType1.findAll()).thenReturn(Stream.of(entityType1));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		Query<AttributeMetaData> qAttr0 = mock(Query.class);
		Query<AttributeMetaData> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
		Query<AttributeMetaData> q = new QueryImpl<>();
		ArgumentCaptor<Query<AttributeMetaData>> queryCaptor = forClass((Class) Query.class);
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		Query q = mock(Query.class);
		when(decoratedRepo.findAll(q)).thenReturn(Stream.of(attr0, attr1));
		assertEquals(repo.findAll(q).collect(toList()), asList(attr0, attr1));
	}

	@Test
	public void findAllQueryUser() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr1Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		Query<EntityType> qEntityType0 = mock(Query.class);
		Query<EntityType> qEntityType1 = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType0);
		when(qEntityType.eq(ATTRIBUTES, attr1)).thenReturn(qEntityType1);
		when(qEntityType0.findAll()).thenReturn(Stream.of(entityType0));
		when(qEntityType1.findAll()).thenReturn(Stream.of(entityType1));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		Query<AttributeMetaData> qAttr0 = mock(Query.class);
		Query<AttributeMetaData> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
		Query<AttributeMetaData> q = new QueryImpl<>();
		ArgumentCaptor<Query<AttributeMetaData>> queryCaptor = forClass((Class) Query.class);
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
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr1Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		Query<EntityType> qEntityType0 = mock(Query.class);
		Query<EntityType> qEntityType1 = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType0);
		when(qEntityType.eq(ATTRIBUTES, attr1)).thenReturn(qEntityType1);
		when(qEntityType0.findAll()).thenReturn(Stream.of(entityType0));
		when(qEntityType1.findAll()).thenReturn(Stream.of(entityType1));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		Query<AttributeMetaData> qAttr0 = mock(Query.class);
		Query<AttributeMetaData> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
		Query<AttributeMetaData> q = mock(Query.class);
		when(q.getOffset()).thenReturn(1);
		when(q.getPageSize()).thenReturn(1);
		ArgumentCaptor<Query<AttributeMetaData>> queryCaptor = forClass((Class) Query.class);
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		when(decoratedRepo.iterator()).thenReturn(asList(attr0, attr1).iterator());
		assertEquals(newArrayList(repo.iterator()), asList(attr0, attr1));
	}

	@Test
	public void iteratorUser() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr1Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		Query<EntityType> qEntityType0 = mock(Query.class);
		Query<EntityType> qEntityType1 = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType0);
		when(qEntityType.eq(ATTRIBUTES, attr1)).thenReturn(qEntityType1);
		when(qEntityType0.findAll()).thenReturn(Stream.of(entityType0));
		when(qEntityType1.findAll()).thenReturn(Stream.of(entityType1));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		Query<AttributeMetaData> qAttr0 = mock(Query.class);
		Query<AttributeMetaData> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
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
		Consumer<List<AttributeMetaData>> consumer = mock(Consumer.class);
		repo.forEachBatched(fetch, consumer, 10);
		verify(decoratedRepo).forEachBatched(fetch, consumer, 10);
	}

	//	// TODO implement forEachBatchedUser unit test, but how?
	//	//	@Test
	//	//	public void forEachBatchedUser() throws Exception
	//	//	{
	//	//
	//	//	}

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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		Query q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(attr0);
		assertEquals(repo.findOne(q), attr0);
	}

	@Test
	public void findOneQueryUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType);
		when(qEntityType.findAll()).thenReturn(Stream.of(entityType0));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
		Query<AttributeMetaData> q = new QueryImpl<>();
		when(decoratedRepo.findOne(q)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOne(q), attr0);
	}

	@Test
	public void findOneQueryUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType);
		when(qEntityType.findAll()).thenReturn(Stream.of(entityType0));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
		Query<AttributeMetaData> q = new QueryImpl<>();
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType);
		when(qEntityType.findAll()).thenReturn(Stream.of(entityType0));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionAllowedAttrInCompoundWithOneAttr() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attrCompoundattr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String attrCompoundName = "entity0attrCompound";
		AttributeMetaData attrCompound = when(mock(AttributeMetaData.class).getName()).thenReturn(attrCompoundName)
				.getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		Query<EntityType> qEntityType0 = mock(Query.class);
		Query<EntityType> qEntityType1 = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType0);
		when(qEntityType0.findAll()).thenReturn(Stream.empty()); // attribute is part of compound, not of entity
		when(qEntityType.eq(ATTRIBUTES, attrCompound)).thenReturn(qEntityType1);
		when(qEntityType1.findAll()).thenReturn(Stream.of(entityType0));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		Query<AttributeMetaData> qAttr0 = mock(Query.class);
		Query<AttributeMetaData> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attrCompound)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.of(attrCompound));
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityType0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType);
		when(qEntityType.findAll()).thenReturn(Stream.of(entityType0));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdFetchUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType);
		when(qEntityType.findAll()).thenReturn(Stream.of(entityType0));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
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
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType);
		when(qEntityType.findAll()).thenReturn(Stream.of(entityType0));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		Stream<Object> ids = Stream.of("0", "1");
		when(decoratedRepo.findAll(ids)).thenReturn(Stream.of(attr0, attr1));
		assertEquals(repo.findAll(ids).collect(toList()), asList(attr0, attr1));
	}

	@Test
	public void findAllIdsUser() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr1Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		Query<EntityType> qEntityType0 = mock(Query.class);
		Query<EntityType> qEntityType1 = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType0);
		when(qEntityType.eq(ATTRIBUTES, attr1)).thenReturn(qEntityType1);
		when(qEntityType0.findAll()).thenReturn(Stream.of(entityType0));
		when(qEntityType1.findAll()).thenReturn(Stream.of(entityType1));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		Query<AttributeMetaData> qAttr0 = mock(Query.class);
		Query<AttributeMetaData> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		Stream<Object> ids = Stream.of("0", "1");
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(Stream.of(attr0, attr1));
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(attr0, attr1));
	}

	@Test
	public void findAllIdsFetchUser() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr1Name).getMock();
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getName()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getName()).thenReturn(entityType1Name).getMock();
		Query<EntityType> qEntityType = mock(Query.class);
		Query<EntityType> qEntityType0 = mock(Query.class);
		Query<EntityType> qEntityType1 = mock(Query.class);
		when(qEntityType.eq(ATTRIBUTES, attr0)).thenReturn(qEntityType0);
		when(qEntityType.eq(ATTRIBUTES, attr1)).thenReturn(qEntityType1);
		when(qEntityType0.findAll()).thenReturn(Stream.of(entityType0));
		when(qEntityType1.findAll()).thenReturn(Stream.of(entityType1));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(qEntityType);
		Query<AttributeMetaData> qAttr = mock(Query.class);
		Query<AttributeMetaData> qAttr0 = mock(Query.class);
		Query<AttributeMetaData> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(qAttr);
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
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityTypeRegistry.hasSystemAttributeMetaData(attrIdentifier)).thenReturn(false);

		//noinspection unchecked
		Query<EntityType> entityQ = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(entityQ);

		//noinspection unchecked
		Query<AttributeMetaData> attrQ = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, attr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(null);

		repo.delete(attr);

		verify(decoratedRepo).delete(attr);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting system entity attribute \\[attrName\\] is not allowed")
	public void deleteSystemAttribute()
	{
		String attrName = "attrName";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityTypeRegistry.hasSystemAttributeMetaData(attrIdentifier)).thenReturn(true);
		repo.delete(attr);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting attribute \\[attrName\\] is not allowed, since it is referenced by entity \\[ownerEntity\\]")
	public void deleteReferencedByEntity()
	{
		String attrName = "attrName";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityTypeRegistry.hasSystemAttributeMetaData(attrIdentifier)).thenReturn(false);
		//noinspection unchecked
		Query<EntityType> entityQ = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attr)).thenReturn(entityQ);
		EntityType ownerEntityType = when(mock(EntityType.class).getName()).thenReturn("ownerEntity").getMock();
		when(entityQ.findOne()).thenReturn(ownerEntityType);
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(entityQ);
		repo.delete(attr);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting attribute \\[attrName\\] is not allowed, since it is referenced by attribute \\[ownerAttr\\]")
	public void deleteReferencedByAttribute()
	{
		String attrName = "attrName";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityTypeRegistry.hasSystemAttributeMetaData(attrIdentifier)).thenReturn(false);

		//noinspection unchecked
		Query<EntityType> entityQ = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(entityQ);

		AttributeMetaData ownerAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("ownerAttr").getMock();
		//noinspection unchecked
		Query<AttributeMetaData> attrQ = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, attr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(ownerAttr);

		repo.delete(attr);
	}

	@Test
	public void deleteStream()
	{
		AttributeMetaDataRepositoryDecorator repoSpy = spy(repo);
		doNothing().when(repoSpy).delete(any(AttributeMetaData.class));
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		AttributeMetaData attr1 = mock(AttributeMetaData.class);
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
}