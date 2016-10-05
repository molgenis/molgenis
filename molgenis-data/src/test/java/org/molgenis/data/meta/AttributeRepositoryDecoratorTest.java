package org.molgenis.data.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
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
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.PARTS;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;
import static org.molgenis.security.core.runas.SystemSecurityToken.USER_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class AttributeRepositoryDecoratorTest
{
	private AttributeMetaDataRepositoryDecorator repo;
	private Repository<Attribute> decoratedRepo;
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
		repo = new AttributeMetaDataRepositoryDecorator(decoratedRepo, systemEntityMetaRegistry, dataService,
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
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		Query<EntityMetaData> qEntityMeta0 = mock(Query.class);
		Query<EntityMetaData> qEntityMeta1 = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta0);
		when(qEntityMeta.eq(ATTRIBUTES, attr1)).thenReturn(qEntityMeta1);
		when(qEntityMeta0.findAll()).thenReturn(Stream.of(entityMeta0));
		when(qEntityMeta1.findAll()).thenReturn(Stream.of(entityMeta1));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		Query<Attribute> qAttr0 = mock(Query.class);
		Query<Attribute> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		when(decoratedRepo.spliterator()).thenReturn(asList(attr0, attr1).spliterator());
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
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		Query<EntityMetaData> qEntityMeta0 = mock(Query.class);
		Query<EntityMetaData> qEntityMeta1 = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta0);
		when(qEntityMeta.eq(ATTRIBUTES, attr1)).thenReturn(qEntityMeta1);
		when(qEntityMeta0.findAll()).thenReturn(Stream.of(entityMeta0));
		when(qEntityMeta1.findAll()).thenReturn(Stream.of(entityMeta1));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		Query<Attribute> qAttr0 = mock(Query.class);
		Query<Attribute> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Query<Attribute> q = new QueryImpl<>();
		ArgumentCaptor<Query<Attribute>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
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
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		Query q = mock(Query.class);
		when(decoratedRepo.findAll(q)).thenReturn(Stream.of(attr0, attr1));
		assertEquals(repo.findAll(q).collect(toList()), asList(attr0, attr1));
	}

	@Test
	public void findAllQueryUser() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		Query<EntityMetaData> qEntityMeta0 = mock(Query.class);
		Query<EntityMetaData> qEntityMeta1 = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta0);
		when(qEntityMeta.eq(ATTRIBUTES, attr1)).thenReturn(qEntityMeta1);
		when(qEntityMeta0.findAll()).thenReturn(Stream.of(entityMeta0));
		when(qEntityMeta1.findAll()).thenReturn(Stream.of(entityMeta1));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		Query<Attribute> qAttr0 = mock(Query.class);
		Query<Attribute> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Query<Attribute> q = new QueryImpl<>();
		ArgumentCaptor<Query<Attribute>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(true);
		assertEquals(repo.findAll(q).collect(toList()), singletonList(attr1));
		assertEquals(queryCaptor.getValue().getOffset(), 0);
		assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
	}

	@Test
	public void findAllQueryUserOffsetLimit() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		Query<EntityMetaData> qEntityMeta0 = mock(Query.class);
		Query<EntityMetaData> qEntityMeta1 = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta0);
		when(qEntityMeta.eq(ATTRIBUTES, attr1)).thenReturn(qEntityMeta1);
		when(qEntityMeta0.findAll()).thenReturn(Stream.of(entityMeta0));
		when(qEntityMeta1.findAll()).thenReturn(Stream.of(entityMeta1));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		Query<Attribute> qAttr0 = mock(Query.class);
		Query<Attribute> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Query<Attribute> q = mock(Query.class);
		when(q.getOffset()).thenReturn(1);
		when(q.getPageSize()).thenReturn(1);
		ArgumentCaptor<Query<Attribute>> queryCaptor = forClass((Class) Query.class);
		when(decoratedRepo.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(true);
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
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		Query<EntityMetaData> qEntityMeta0 = mock(Query.class);
		Query<EntityMetaData> qEntityMeta1 = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta0);
		when(qEntityMeta.eq(ATTRIBUTES, attr1)).thenReturn(qEntityMeta1);
		when(qEntityMeta0.findAll()).thenReturn(Stream.of(entityMeta0));
		when(qEntityMeta1.findAll()).thenReturn(Stream.of(entityMeta1));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		Query<Attribute> qAttr0 = mock(Query.class);
		Query<Attribute> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		when(decoratedRepo.spliterator()).thenReturn(asList(attr0, attr1).spliterator());
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(true);
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
		Consumer<List<Attribute>> consumer = mock(Consumer.class);
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
		Attribute attr0 = mock(Attribute.class);
		Query q = mock(Query.class);
		when(decoratedRepo.findOne(q)).thenReturn(attr0);
		assertEquals(repo.findOne(q), attr0);
	}

	@Test
	public void findOneQueryUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta);
		when(qEntityMeta.findAll()).thenReturn(Stream.of(entityMeta0));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Query<Attribute> q = new QueryImpl<>();
		when(decoratedRepo.findOne(q)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		assertEquals(repo.findOne(q), attr0);
	}

	@Test
	public void findOneQueryUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta);
		when(qEntityMeta.findAll()).thenReturn(Stream.of(entityMeta0));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Query<Attribute> q = new QueryImpl<>();
		when(decoratedRepo.findOne(q)).thenReturn(attr0);
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
		Attribute attr0 = mock(Attribute.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta);
		when(qEntityMeta.findAll()).thenReturn(Stream.of(entityMeta0));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionAllowedAttrInCompoundWithOneAttr() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attrCompoundattr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String attrCompoundName = "entity0attrCompound";
		Attribute attrCompound = when(mock(Attribute.class).getName()).thenReturn(attrCompoundName)
				.getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		Query<EntityMetaData> qEntityMeta0 = mock(Query.class);
		Query<EntityMetaData> qEntityMeta1 = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta0);
		when(qEntityMeta0.findAll()).thenReturn(Stream.empty()); // attribute is part of compound, not of entity
		when(qEntityMeta.eq(ATTRIBUTES, attrCompound)).thenReturn(qEntityMeta1);
		when(qEntityMeta1.findAll()).thenReturn(Stream.of(entityMeta0));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		Query<Attribute> qAttr0 = mock(Query.class);
		Query<Attribute> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attrCompound)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.of(attrCompound));
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta);
		when(qEntityMeta.findAll()).thenReturn(Stream.of(entityMeta0));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
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
		Attribute attr0 = mock(Attribute.class);
		Object id = "0";
		when(decoratedRepo.findOneById(id)).thenReturn(attr0);
		assertEquals(repo.findOneById(id), attr0);
	}

	@Test
	public void findOneByIdFetchUserPermissionAllowed() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta);
		when(qEntityMeta.findAll()).thenReturn(Stream.of(entityMeta0));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Object id = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(attr0);
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(true);
		assertEquals(repo.findOneById(id, fetch), attr0);
	}

	@Test
	public void findOneByIdFetchUserPermissionDenied() throws Exception
	{
		setUserAuthentication();
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta);
		when(qEntityMeta.findAll()).thenReturn(Stream.of(entityMeta0));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr);
		when(qAttr.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Object id = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(attr0);
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
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		Query<EntityMetaData> qEntityMeta0 = mock(Query.class);
		Query<EntityMetaData> qEntityMeta1 = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta0);
		when(qEntityMeta.eq(ATTRIBUTES, attr1)).thenReturn(qEntityMeta1);
		when(qEntityMeta0.findAll()).thenReturn(Stream.of(entityMeta0));
		when(qEntityMeta1.findAll()).thenReturn(Stream.of(entityMeta1));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		Query<Attribute> qAttr0 = mock(Query.class);
		Query<Attribute> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Stream<Object> ids = Stream.of(mock(Object.class), mock(Object.class));
		when(decoratedRepo.findAll(ids)).thenReturn(Stream.of(attr0, attr1));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(true);
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
		String attr0Name = "entity0attr0";
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
		String attr1Name = "entity1attr0";
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
		String entityMeta0Name = "entity0";
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta0Name).getMock();
		String entityMeta1Name = "entity1";
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn(entityMeta1Name).getMock();
		Query<EntityMetaData> qEntityMeta = mock(Query.class);
		Query<EntityMetaData> qEntityMeta0 = mock(Query.class);
		Query<EntityMetaData> qEntityMeta1 = mock(Query.class);
		when(qEntityMeta.eq(ATTRIBUTES, attr0)).thenReturn(qEntityMeta0);
		when(qEntityMeta.eq(ATTRIBUTES, attr1)).thenReturn(qEntityMeta1);
		when(qEntityMeta0.findAll()).thenReturn(Stream.of(entityMeta0));
		when(qEntityMeta1.findAll()).thenReturn(Stream.of(entityMeta1));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(qEntityMeta);
		Query<Attribute> qAttr = mock(Query.class);
		Query<Attribute> qAttr0 = mock(Query.class);
		Query<Attribute> qAttr1 = mock(Query.class);
		when(qAttr.eq(PARTS, attr0)).thenReturn(qAttr0);
		when(qAttr.eq(PARTS, attr1)).thenReturn(qAttr1);
		when(qAttr0.findAll()).thenReturn(Stream.empty());
		when(qAttr1.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(qAttr);
		Stream<Object> ids = Stream.of(mock(Object.class), mock(Object.class));
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(Stream.of(attr0, attr1));
		when(permissionService.hasPermissionOnEntity(entityMeta0Name, READ)).thenReturn(false);
		when(permissionService.hasPermissionOnEntity(entityMeta1Name, READ)).thenReturn(true);
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
	public void addMappedByValidEntity()
	{
		String entityName = "entityName";
		EntityMetaData refEntity = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName)
				.getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
		repo.add(attr);
		verify(decoratedRepo).add(attr);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "mappedBy attribute \\[mappedByAttrName\\] is not part of entity \\[entityName\\].")
	public void addMappedByInvalidEntity()
	{
		String entityName = "entityName";
		EntityMetaData refEntity = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName)
				.getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
		repo.add(attr);
		verify(decoratedRepo).add(attr);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Invalid mappedBy attribute \\[mappedByAttrName\\] data type \\[STRING\\].")
	public void addMappedByInvalidDataType()
	{
		String entityName = "entityName";
		EntityMetaData refEntity = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName)
				.getMock();
		when(mappedByAttr.getDataType()).thenReturn(STRING); // invalid type
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
		repo.add(attr);
		verify(decoratedRepo).add(attr);
	}

	@Test
	public void delete()
	{
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityMetaRegistry.hasSystemAttributeMetaData(attrIdentifier)).thenReturn(false);

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);

		//noinspection unchecked
		Query<Attribute> attrQ = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, attr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(null);

		repo.delete(attr);

		verify(decoratedRepo).delete(attr);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting system entity attribute \\[attrName\\] is not allowed")
	public void deleteSystemAttribute()
	{
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityMetaRegistry.hasSystemAttributeMetaData(attrIdentifier)).thenReturn(true);
		repo.delete(attr);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting attribute \\[attrName\\] is not allowed, since it is referenced by entity \\[ownerEntity\\]")
	public void deleteReferencedByEntity()
	{
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityMetaRegistry.hasSystemAttributeMetaData(attrIdentifier)).thenReturn(false);
		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attr)).thenReturn(entityQ);
		EntityMetaData ownerEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("ownerEntity").getMock();
		when(entityQ.findOne()).thenReturn(ownerEntityMeta);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		repo.delete(attr);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Deleting attribute \\[attrName\\] is not allowed, since it is referenced by attribute \\[ownerAttr\\]")
	public void deleteReferencedByAttribute()
	{
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityMetaRegistry.hasSystemAttributeMetaData(attrIdentifier)).thenReturn(false);

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);

		Attribute ownerAttr = when(mock(Attribute.class).getName()).thenReturn("ownerAttr").getMock();
		//noinspection unchecked
		Query<Attribute> attrQ = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, attr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(ownerAttr);

		repo.delete(attr);
	}

	@Test
	public void deleteStream()
	{
		AttributeMetaDataRepositoryDecorator repoSpy = spy(repo);
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
}