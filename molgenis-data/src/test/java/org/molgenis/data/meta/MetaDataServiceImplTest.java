package org.molgenis.data.meta;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetadata.PARENT;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertNull;

public class MetaDataServiceImplTest
{
	private MetaDataServiceImpl metaDataServiceImpl;
	private DataService dataService;
	private RepositoryCollectionRegistry repoCollectionRegistry;
	private EntityTypeDependencyResolver entityTypeDependencyResolver;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		repoCollectionRegistry = mock(RepositoryCollectionRegistry.class);

		SystemEntityTypeRegistry systemEntityTypeRegistry = mock(SystemEntityTypeRegistry.class);
		entityTypeDependencyResolver = mock(EntityTypeDependencyResolver.class);
		metaDataServiceImpl = new MetaDataServiceImpl(dataService, repoCollectionRegistry, systemEntityTypeRegistry,
				entityTypeDependencyResolver);
	}

	@Test
	public void getRepository()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_TYPE_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(entityType);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityType)).thenReturn(repo);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		assertEquals(metaDataServiceImpl.getRepository(entityName), repo);
	}

	@Test
	public void getRepositoryAbstractEntityType()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_TYPE_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(entityType);
		assertNull(metaDataServiceImpl.getRepository(entityName));
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void getRepositoryUnknownEntity()
	{
		metaDataServiceImpl.getRepository("unknownEntity");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getRepositoryTyped()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_TYPE_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(entityType);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		Repository<Package> repo = mock(Repository.class);

		when(repoCollection.getRepository(entityType)).thenReturn((Repository<Entity>) (Repository<?>) repo);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		assertEquals(metaDataServiceImpl.getRepository(entityName, Package.class), repo);
	}

	@Test
	public void getRepositoryTypedAbstractEntityType()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_TYPE_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(entityType);
		assertNull(metaDataServiceImpl.getRepository(entityName, Package.class));
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void getRepositoryTypedUnknownEntity()
	{
		metaDataServiceImpl.getRepository("unknownEntity", Package.class);
	}

	@Test
	public void getRepositoryEntityType()
	{
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityType)).thenReturn(repo);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		assertEquals(metaDataServiceImpl.getRepository(entityType), repo);
	}

	@Test
	public void getRepositoryEntityTypeAbstract()
	{
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
		assertNull(metaDataServiceImpl.getRepository(entityType));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getRepositoryTypedEntityType()
	{
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		Repository<Package> repo = mock(Repository.class);

		when(repoCollection.getRepository(entityType)).thenReturn((Repository<Entity>) (Repository<?>) repo);

		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		assertEquals(metaDataServiceImpl.getRepository(entityType, Package.class), repo);
	}

	@Test
	public void getRepositoryTypedEntityTypeAbstract()
	{
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
		assertNull(metaDataServiceImpl.getRepository(entityType, Package.class));
	}

	@Test
	public void getRepositories()
	{
		String backendName0 = "backend0";
		String backendName1 = "backend1";

		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.getBackend()).thenReturn(backendName0);
		EntityType entityType1 = mock(EntityType.class);
		when(entityType1.getBackend()).thenReturn(backendName1);

		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ = mock(Query.class);

		when(entityQ.eq(IS_ABSTRACT, false)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findAll()).thenReturn(Stream.of(entityType0, entityType1));
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
		RepositoryCollection repoCollection0 = mock(RepositoryCollection.class);
		@SuppressWarnings("unchecked")
		Repository<Entity> repo0 = mock(Repository.class);
		when(repoCollection0.getRepository(entityType0)).thenReturn(repo0);
		when(repoCollectionRegistry.getRepositoryCollection(backendName0)).thenReturn(repoCollection0);
		@SuppressWarnings("unchecked")
		Repository<Entity> repo1 = mock(Repository.class);
		RepositoryCollection repoCollection1 = mock(RepositoryCollection.class);
		when(repoCollection1.getRepository(entityType1)).thenReturn(repo1);
		when(repoCollectionRegistry.getRepositoryCollection(backendName1)).thenReturn(repoCollection1);
		@SuppressWarnings("unchecked")
		List<Repository<Entity>> expectedRepos = newArrayList(repo0, repo1);
		assertEquals(metaDataServiceImpl.getRepositories().collect(toList()), expectedRepos);
	}

	@Test
	public void hasRepository()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();

		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ = mock(Query.class);
		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ2 = mock(Query.class);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);

		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.and()).thenReturn(entityQ2);
		when(entityQ2.eq(IS_ABSTRACT, false)).thenReturn(entityQ2);
		when(entityQ2.findOne()).thenReturn(entityType);

		assertTrue(metaDataServiceImpl.hasRepository(entityName));
	}

	@Test
	public void hasRepositoryAbstractEntityType()
	{
		String entityName = "entity";

		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ = mock(Query.class);
		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ2 = mock(Query.class);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);

		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.and()).thenReturn(entityQ2);
		when(entityQ2.eq(IS_ABSTRACT, false)).thenReturn(entityQ2);
		when(entityQ2.findOne()).thenReturn(null);

		assertFalse(metaDataServiceImpl.hasRepository(entityName));
	}

	@Test
	public void createRepository()
	{
		String backendName = "backend";

		EntityType entityType = mock(EntityType.class);
		when(entityType.getBackend()).thenReturn(backendName);
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));

		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityType)).thenReturn(repo);
		assertEquals(metaDataServiceImpl.createRepository(entityType), repo);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_TYPE_META_DATA, entityType);

		verifyNoMoreInteractions(dataService);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void createRepositoryAbstractEntityType()
	{
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
		metaDataServiceImpl.createRepository(entityType);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createRepositoryTyped()
	{
		String backendName = "backend";
		Class<Package> entityClass = Package.class;

		EntityType entityType = mock(EntityType.class);
		when(entityType.getBackend()).thenReturn(backendName);
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));

		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);

		Repository<Package> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityType)).thenReturn((Repository<Entity>) (Repository<?>) repo);
		assertEquals(metaDataServiceImpl.createRepository(entityType, entityClass), repo);

		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_TYPE_META_DATA, entityType);

		verifyNoMoreInteractions(dataService);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void createRepositoryTypedAbstractEntityType()
	{
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
		metaDataServiceImpl.createRepository(entityType, Package.class);
	}

	@Test
	public void getBackend()
	{
		String backendName = "backend";
		RepositoryCollection repo = mock(RepositoryCollection.class);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repo);
		assertEquals(metaDataServiceImpl.getBackend(backendName), repo);
	}

	@Test
	public void getBackendUnknown()
	{
		String backendName = "backend";
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(null);
		assertNull(metaDataServiceImpl.getBackend(backendName));
	}

	@Test
	public void hasBackendFalse()
	{
		String backendName = "backend";
		when(repoCollectionRegistry.hasRepositoryCollection(backendName)).thenReturn(false);
		assertFalse(metaDataServiceImpl.hasBackend(backendName));
	}

	@Test
	public void getDefaultRepositoryCollection()
	{
		RepositoryCollection repo = mock(RepositoryCollection.class);
		when(repoCollectionRegistry.getDefaultRepoCollection()).thenReturn(repo);
		assertEquals(metaDataServiceImpl.getDefaultBackend(), repo);
	}

	@Test
	public void hasBackendTrue()
	{
		String backendName = "backend";
		when(repoCollectionRegistry.hasRepositoryCollection(backendName)).thenReturn(true);
		assertTrue(metaDataServiceImpl.hasBackend(backendName));
	}

	@Test
	public void getPackages()
	{
		Package package0 = mock(Package.class);
		Package package1 = mock(Package.class);
		when(dataService.findAll(PACKAGE, Package.class)).thenReturn(Stream.of(package0, package1));
		assertEquals(metaDataServiceImpl.getPackages(), newArrayList(package0, package1));
	}

	@Test
	public void getRootPackages()
	{
		Package package0 = mock(Package.class);
		Package package1 = mock(Package.class);
		@SuppressWarnings("unchecked")
		Query<Package> packageQ = mock(Query.class);
		when(packageQ.eq(PARENT, null)).thenReturn(packageQ);
		when(packageQ.findAll()).thenReturn(Stream.of(package0, package1));
		when(dataService.query(PACKAGE, Package.class)).thenReturn(packageQ);
		assertEquals(metaDataServiceImpl.getRootPackages(), newArrayList(package0, package1));
	}

	@Test
	public void getPackageString()
	{
		Package package_ = mock(Package.class);
		String packageName = "package";
		when(dataService.findOneById(PACKAGE, packageName, Package.class)).thenReturn(package_);
		assertEquals(metaDataServiceImpl.getPackage(packageName), package_);
	}

	@Test
	public void getPackageStringUnknownPackage()
	{
		String packageName = "package";
		when(dataService.findOneById(PACKAGE, packageName, Package.class)).thenReturn(null);
		assertNull(metaDataServiceImpl.getPackage(packageName));
	}

	@Test
	public void addPackage()
	{
		Package package_ = mock(Package.class);
		metaDataServiceImpl.addPackage(package_);
		verify(dataService).add(PACKAGE, package_);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void upsertPackages()
	{
		String newPackageName = "newPackage";
		Package packageNew = when(mock(Package.class).getFullyQualifiedName()).thenReturn(newPackageName).getMock();
		String updatedPackageName = "updatedPackage";
		Package packageUpdated = when(mock(Package.class).getFullyQualifiedName()).thenReturn(updatedPackageName).getMock();
		when(dataService.findOneById(PACKAGE, newPackageName, Package.class)).thenReturn(null);
		when(dataService.findOneById(PACKAGE, updatedPackageName, Package.class)).thenReturn(packageUpdated);
		metaDataServiceImpl.upsertPackages(Stream.of(packageNew, packageUpdated));
		verify(dataService).findOneById(PACKAGE, newPackageName, Package.class);
		verify(dataService).findOneById(PACKAGE, updatedPackageName, Package.class);
		verify(dataService).add(PACKAGE, packageNew);
		verify(dataService).update(PACKAGE, packageUpdated);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void getEntityType()
	{
		String entityName = "entity";
		EntityType entityType = mock(EntityType.class);
		when(dataService.findOneById(eq(ENTITY_TYPE_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(entityType);
		assertEquals(metaDataServiceImpl.getEntityType(entityName), entityType);
	}

	@Test
	public void getEntityTypeUnknownEntity()
	{
		String entityName = "entity";
		when(dataService.findOneById(eq(ENTITY_TYPE_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(null);
		assertNull(metaDataServiceImpl.getEntityType(entityName));
	}

	@Test
	public void addEntityType()
	{
		EntityType entityType = mock(EntityType.class);
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));
		metaDataServiceImpl.addEntityType(entityType);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_TYPE_META_DATA, entityType);

		verifyNoMoreInteractions(dataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addEntityTypesNoMappedByAttrs()
	{
		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), (Stream<Object>) any(Stream.class), any(Fetch.class),
				eq(EntityType.class))).thenReturn(Stream.empty());

		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.hasMappedByAttributes()).thenReturn(false);
		Attribute entity0Attr0 = mock(Attribute.class);
		Attribute entity0Attr1 = mock(Attribute.class);
		when(entityType0.getOwnAllAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));

		EntityType entityType1 = mock(EntityType.class);
		when(entityType1.hasMappedByAttributes()).thenReturn(false);
		Attribute entity1Attr0 = mock(Attribute.class);
		Attribute entity1Attr1 = mock(Attribute.class);
		when(entityType1.getOwnAllAttributes()).thenReturn(newArrayList(entity1Attr0, entity1Attr1));

		when(entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1)))
				.thenReturn(newArrayList(entityType1, entityType0));
		metaDataServiceImpl.upsertEntityTypes(newArrayList(entityType0, entityType1));

		InOrder inOrder = inOrder(dataService);

		inOrder.verify(dataService)
				.findAll(eq(ENTITY_TYPE_META_DATA), (Stream<Object>) any(Stream.class), any(Fetch.class),
						eq(EntityType.class));

		inOrder.verify(dataService).add(ENTITY_TYPE_META_DATA, entityType1);
		ArgumentCaptor<Stream<Entity>> attrsCaptor1 = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor1.capture());
		assertEquals(attrsCaptor1.getValue().collect(toList()), newArrayList(entity1Attr0, entity1Attr1));

		inOrder.verify(dataService).add(ENTITY_TYPE_META_DATA, entityType0);
		ArgumentCaptor<Stream<Entity>> attrsCaptor0 = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0.capture());
		assertEquals(attrsCaptor0.getValue().collect(toList()), newArrayList(entity0Attr0, entity0Attr1));

		verifyNoMoreInteractions(dataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addEntityTypesMappedByAttrs()
	{
		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.getFullyQualifiedName()).thenReturn("entity0");
		when(entityType0.getName()).thenReturn("entity0");
		when(entityType0.hasMappedByAttributes()).thenReturn(true);

		Attribute entity0Attr0 = mock(Attribute.class);
		when(entity0Attr0.getName()).thenReturn("entity0Attr0");
		when(entity0Attr0.getIdentifier()).thenReturn("id00");
		when(entity0Attr0.getChildren()).thenReturn(emptyList());
		when(entity0Attr0.getTags()).thenReturn(emptyList());
		when(entity0Attr0.isMappedBy()).thenReturn(true);

		Attribute entity0Attr1 = mock(Attribute.class);
		when(entity0Attr1.getName()).thenReturn("entity0Attr1");
		when(entity0Attr1.getIdentifier()).thenReturn("id01");
		when(entity0Attr1.getChildren()).thenReturn(emptyList());
		when(entity0Attr1.getTags()).thenReturn(emptyList());

		when(entityType0.getOwnAllAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
		when(entityType0.getOwnAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
		when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityType0.getTags()).thenReturn(emptyList());

		EntityType entityType1 = mock(EntityType.class);
		when(entityType1.hasMappedByAttributes()).thenReturn(false);
		Attribute entity1Attr0 = mock(Attribute.class);
		Attribute entity1Attr1 = mock(Attribute.class);
		when(entityType1.getOwnAllAttributes()).thenReturn(newArrayList(entity1Attr0, entity1Attr1));

		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), (Stream<Object>) any(Stream.class), any(Fetch.class),
				eq(EntityType.class))).thenReturn(Stream.empty());

		when(entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1)))
				.thenReturn(newArrayList(entityType1, entityType0));
		metaDataServiceImpl.upsertEntityTypes(newArrayList(entityType0, entityType1));

		InOrder inOrder = inOrder(dataService);

		inOrder.verify(dataService)
				.findAll(eq(ENTITY_TYPE_META_DATA), (Stream<Object>) any(Stream.class), any(Fetch.class),
						eq(EntityType.class));

		inOrder.verify(dataService).add(ENTITY_TYPE_META_DATA, entityType1);
		ArgumentCaptor<Stream<Entity>> attrsCaptor1 = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor1.capture());
		assertEquals(attrsCaptor1.getValue().collect(toList()), newArrayList(entity1Attr0, entity1Attr1));

		ArgumentCaptor<EntityType> entityCaptor0 = ArgumentCaptor.forClass(EntityType.class);
		inOrder.verify(dataService).add(eq(ENTITY_TYPE_META_DATA), entityCaptor0.capture());
		assertEquals(newArrayList(entityCaptor0.getValue().getOwnAllAttributes()), singletonList(entity0Attr1));

		ArgumentCaptor<Stream<Entity>> attrsCaptor0 = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0.capture());
		assertEquals(attrsCaptor0.getValue().collect(toList()), singletonList(entity0Attr1));

		ArgumentCaptor<EntityType> entityCaptor0b = ArgumentCaptor.forClass(EntityType.class);
		inOrder.verify(dataService).update(eq(ENTITY_TYPE_META_DATA), entityCaptor0b.capture());
		assertEquals(entityCaptor0b.getValue(), entityType0);

		ArgumentCaptor<Stream<Entity>> attrsCaptor0b = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0b.capture());
		assertEquals(attrsCaptor0b.getValue().collect(toList()), singletonList(entity0Attr0));

		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityType()
	{
		String entityName = "entity";
		metaDataServiceImpl.deleteEntityType(entityName);
		verify(dataService).deleteById(ENTITY_TYPE_META_DATA, entityName);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityTypeCollection()
	{
		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.hasMappedByAttributes()).thenReturn(false);
		String entityName0 = "entity0";
		when(entityType0.getFullyQualifiedName()).thenReturn(entityName0);

		EntityType entityType1 = mock(EntityType.class);
		when(entityType1.hasMappedByAttributes()).thenReturn(false);
		String entityName1 = "entity1";
		when(entityType1.getFullyQualifiedName()).thenReturn(entityName1);

		when(entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1)))
				.thenReturn(newArrayList(entityType1, entityType0));

		metaDataServiceImpl.deleteEntityType(newArrayList(entityType0, entityType1));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> entityIdCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).deleteAll(eq(ENTITY_TYPE_META_DATA), entityIdCaptor.capture());
		assertEquals(entityIdCaptor.getValue().collect(toList()), newArrayList(entityName0, entityName1));
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityTypeCollectionEmpty()
	{
		metaDataServiceImpl.deleteEntityType(emptyList());
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityTypeCollectionMappedBy()
	{
		EntityType entityType0 = mock(EntityType.class);
		String entityName0 = "entity0";
		when(entityType0.getFullyQualifiedName()).thenReturn(entityName0);
		when(entityType0.getName()).thenReturn(entityName0);
		when(entityType0.hasMappedByAttributes()).thenReturn(true);

		Attribute entity0Attr0 = mock(Attribute.class);
		when(entity0Attr0.getName()).thenReturn("entity0Attr0");
		when(entity0Attr0.getIdentifier()).thenReturn("id00");
		when(entity0Attr0.getChildren()).thenReturn(emptyList());
		when(entity0Attr0.getTags()).thenReturn(emptyList());
		when(entity0Attr0.isMappedBy()).thenReturn(true);

		Attribute entity0Attr1 = mock(Attribute.class);
		when(entity0Attr1.getName()).thenReturn("entity0Attr1");
		when(entity0Attr1.getIdentifier()).thenReturn("id01");
		when(entity0Attr1.getChildren()).thenReturn(emptyList());
		when(entity0Attr1.getTags()).thenReturn(emptyList());

		when(entityType0.getOwnAllAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
		when(entityType0.getOwnAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
		when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityType0.getTags()).thenReturn(emptyList());

		EntityType entityType1 = mock(EntityType.class);
		String entityName1 = "entity1";

		when(entityType1.getFullyQualifiedName()).thenReturn(entityName1);
		when(entityType1.hasMappedByAttributes()).thenReturn(false);
		Attribute entity1Attr0 = mock(Attribute.class);
		Attribute entity1Attr1 = mock(Attribute.class);
		when(entityType1.getOwnAllAttributes()).thenReturn(newArrayList(entity1Attr0, entity1Attr1));

		when(entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1)))
				.thenReturn(newArrayList(entityType1, entityType0));

		InOrder inOrder = inOrder(dataService);
		metaDataServiceImpl.deleteEntityType(newArrayList(entityType0, entityType1));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<EntityType>> entityCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).update(eq(ENTITY_TYPE_META_DATA), entityCaptor.capture());
		List<EntityType> updatedEntities = entityCaptor.getValue().collect(toList());
		assertEquals(updatedEntities.size(), 1);
		assertEquals(updatedEntities.get(0).getFullyQualifiedName(), entityName0);
		assertEquals(newArrayList(updatedEntities.get(0).getOwnAllAttributes()), newArrayList(entity0Attr1));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> entityIdCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).deleteAll(eq(ENTITY_TYPE_META_DATA), entityIdCaptor.capture());
		assertEquals(entityIdCaptor.getValue().collect(toList()), newArrayList(entityName0, entityName1));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void updateEntityType()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		Attribute attrShared0 = when(mock(Attribute.class).getName()).thenReturn(attrShared0Name).getMock();
		when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
		when(attrShared0.getChildren()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		Attribute attrShared1 = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
		when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getChildren()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		Attribute attrShared1Updated = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getChildren()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getChildren()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		Attribute attrDeleted = when(mock(Attribute.class).getName()).thenReturn(attrDeletedName).getMock();
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedName);
		when(attrDeleted.getChildren()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityType existingEntityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		when(existingEntityType.getLabel()).thenReturn("label");
		when(existingEntityType.getName()).thenReturn(entityName);
		when(existingEntityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityType.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityType.getTags()).thenReturn(emptyList());

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		when(entityType.getLabel()).thenReturn("new label");
		when(entityType.getName()).thenReturn(entityName);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityType.getOwnAttributes()).thenReturn(emptyList());
		when(entityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityType.getTags()).thenReturn(emptyList());

		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityType);

		metaDataServiceImpl.updateEntityType(entityType);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void updateEntityTypeEntityDoesNotExist()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ = mock(Query.class);

		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
		metaDataServiceImpl.updateEntityType(entityType);
	}

	@Test
	public void updateEntityTypeCollectionEmpty()
	{
		metaDataServiceImpl.upsertEntityTypes(emptyList());
		verifyNoMoreInteractions(dataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateEntityTypeCollection()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		Attribute attrShared0 = when(mock(Attribute.class).getName()).thenReturn(attrShared0Name).getMock();
		when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
		when(attrShared0.getChildren()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		Attribute attrShared1 = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
		when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getChildren()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		Attribute attrShared1Updated = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getChildren()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getChildren()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		Attribute attrDeleted = when(mock(Attribute.class).getName()).thenReturn(attrDeletedName).getMock();
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedName);
		when(attrDeleted.getChildren()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityType existingEntityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		when(existingEntityType.getLabel()).thenReturn("label");
		when(existingEntityType.getName()).thenReturn(entityName);
		when(existingEntityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityType.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityType.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic

		when(existingEntityType.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		when(entityType.getLabel()).thenReturn("new label");
		when(entityType.getName()).thenReturn(entityName);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityType.getOwnAttributes()).thenReturn(emptyList());
		when(entityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityType.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic

		when(entityType.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});
		Query<EntityType> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
		when(entityQ.eq(ATTRIBUTE_META_DATA, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityType);

		when(entityTypeDependencyResolver.resolve(singletonList(entityType))).thenReturn(singletonList(entityType));

		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), (Stream<Object>) any(Stream.class), any(Fetch.class),
				eq(EntityType.class))).thenReturn(Stream.of(existingEntityType));

		metaDataServiceImpl.upsertEntityTypes(singletonList(entityType));

		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateEntityTypeCollectionMappedByExisting()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		Attribute attrShared0 = when(mock(Attribute.class).getName()).thenReturn(attrShared0Name).getMock();
		when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
		when(attrShared0.getChildren()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		Attribute attrShared1 = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
		when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getChildren()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		when(attrShared1.isMappedBy()).thenReturn(true);
		Attribute attrShared1Updated = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getChildren()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		when(attrShared1.isMappedBy()).thenReturn(false);
		Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getChildren()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		Attribute attrDeleted = when(mock(Attribute.class).getName()).thenReturn(attrDeletedName).getMock();
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedName);
		when(attrDeleted.getChildren()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityType existingEntityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		when(existingEntityType.getLabel()).thenReturn("label");
		when(existingEntityType.getName()).thenReturn(entityName);
		when(existingEntityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityType.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityType.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic

		when(existingEntityType.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(attrShared1);
			}
		});

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		when(entityType.getLabel()).thenReturn("new label");
		when(entityType.getName()).thenReturn(entityName);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityType.getOwnAttributes()).thenReturn(emptyList());
		when(entityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityType.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic

		when(entityType.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(attrShared1Updated);
			}
		});

		Query<EntityType> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
		when(entityQ.eq(ATTRIBUTE_META_DATA, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityType);

		when(entityTypeDependencyResolver.resolve(singletonList(entityType))).thenReturn(singletonList(entityType));

		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), (Stream<Object>) any(Stream.class), any(Fetch.class),
				eq(EntityType.class))).thenReturn(Stream.of(existingEntityType));

		metaDataServiceImpl.upsertEntityTypes(singletonList(entityType));

		InOrder inOrder = inOrder(dataService);

		inOrder.verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);

		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		ArgumentCaptor<Stream<Entity>> attrDeletedCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrDeletedCaptor.capture());
		assertEquals(attrDeletedCaptor.getValue().collect(toList()), singletonList(attrDeleted));

		inOrder.verifyNoMoreInteractions();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateEntityTypeCollectionMappedByNew()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		Attribute attrShared0 = when(mock(Attribute.class).getName()).thenReturn(attrShared0Name).getMock();
		when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
		when(attrShared0.getChildren()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		Attribute attrShared1 = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
		when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getChildren()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		when(attrShared1.isMappedBy()).thenReturn(true);
		Attribute attrShared1Updated = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getChildren()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		when(attrShared1Updated.isMappedBy()).thenReturn(false);
		Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getChildren()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		Attribute attrDeleted = when(mock(Attribute.class).getName()).thenReturn(attrDeletedName).getMock();
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedName);
		when(attrDeleted.getChildren()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityType existingEntityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		when(existingEntityType.getLabel()).thenReturn("label");
		when(existingEntityType.getName()).thenReturn(entityName);
		when(existingEntityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityType.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityType.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic

		when(existingEntityType.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(attrShared1);
			}
		});

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		when(entityType.getLabel()).thenReturn("new label");
		when(entityType.getName()).thenReturn(entityName);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityType.getOwnAttributes()).thenReturn(emptyList());
		when(entityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityType.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic

		when(entityType.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});

		Query<EntityType> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
		when(entityQ.eq(ATTRIBUTE_META_DATA, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityType);

		when(entityTypeDependencyResolver.resolve(singletonList(entityType))).thenReturn(singletonList(entityType));

		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), (Stream<Object>) any(Stream.class), any(Fetch.class),
				eq(EntityType.class))).thenReturn(Stream.of(existingEntityType));

		InOrder inOrder = inOrder(dataService);

		metaDataServiceImpl.upsertEntityTypes(singletonList(entityType));

		inOrder.verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);

		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		ArgumentCaptor<Stream<Entity>> attrDeletedCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrDeletedCaptor.capture());
		assertEquals(attrDeletedCaptor.getValue().collect(toList()), singletonList(attrDeleted));

		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void addAttribute()
	{
		Attribute attr = mock(Attribute.class);
		EntityType entityType = mock(EntityType.class);
		EntityType currentEntityType = mock(EntityType.class);
		when(attr.getEntity()).thenReturn(entityType);
		when(entityType.getFullyQualifiedName()).thenReturn("EntityTypeName");
		when(dataService.getEntityType("EntityTypeName")).thenReturn(currentEntityType);

		metaDataServiceImpl.addAttribute(attr);
		verify(dataService).update(ENTITY_TYPE_META_DATA, currentEntityType);
		verify(dataService).add(ATTRIBUTE_META_DATA, attr);
		verify(currentEntityType).addAttribute(attr);
	}

	@Test
	public void deleteAttributeById()
	{
		Object attrId = "attr0";
		Attribute attribute = mock(Attribute.class);
		EntityType entityType = mock(EntityType.class);
		when(dataService.findOneById(ATTRIBUTE_META_DATA, attrId, Attribute.class)).thenReturn(attribute);
		when(attribute.getEntity()).thenReturn(entityType);

		metaDataServiceImpl.deleteAttributeById(attrId);
		verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);
		verify(dataService).delete(ATTRIBUTE_META_DATA, attribute);
		verify(entityType).removeAttribute(attribute);
	}

	@DataProvider(name = "isMetaEntityTypeProvider")
	public static Iterator<Object[]> isMetaEntityTypeProvider()
	{
		return newArrayList(new Object[] { ENTITY_TYPE_META_DATA, true },
				new Object[] { ATTRIBUTE_META_DATA, true }, new Object[] { TAG, true },
				new Object[] { PACKAGE, true }, new Object[] { "noMeta", false }).iterator();
	}

	@Test(dataProvider = "isMetaEntityTypeProvider")
	public void isMetaEntityType(String entityName, boolean isMeta)
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn(entityName).getMock();
		assertEquals(metaDataServiceImpl.isMetaEntityType(entityType), isMeta);
	}

	@Test
	public void upsertEntityTypeCollectionEmpty()
	{
		metaDataServiceImpl.upsertEntityTypes(emptyList());
		verifyZeroInteractions(dataService);
	}
}
