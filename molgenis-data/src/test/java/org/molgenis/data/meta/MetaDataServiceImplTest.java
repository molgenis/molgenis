package org.molgenis.data.meta;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
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
import static org.molgenis.data.meta.model.AttributeMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetaData.PARENT;
import static org.molgenis.data.meta.model.TagMetaData.TAG;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertNull;

public class MetaDataServiceImplTest
{
	private MetaDataServiceImpl metaDataServiceImpl;
	private DataService dataService;
	private RepositoryCollectionRegistry repoCollectionRegistry;
	private EntityMetaDataDependencyResolver entityMetaDependencyResolver;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		repoCollectionRegistry = mock(RepositoryCollectionRegistry.class);
		SystemEntityMetaDataRegistry systemEntityMetaRegistry = mock(SystemEntityMetaDataRegistry.class);
		entityMetaDependencyResolver = mock(EntityMetaDataDependencyResolver.class);
		metaDataServiceImpl = new MetaDataServiceImpl(dataService, repoCollectionRegistry, systemEntityMetaRegistry,
				entityMetaDependencyResolver);
	}

	@Test
	public void getRepository()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityMeta.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityMetaData.class)))
				.thenReturn(entityMeta);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		//noinspection unchecked
		Repository<Entity> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityMeta)).thenReturn(repo);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		assertEquals(metaDataServiceImpl.getRepository(entityName), repo);
	}

	@Test
	public void getRepositoryAbstractEntityMeta()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(true).getMock();
		String backendName = "backend";
		when(entityMeta.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityMetaData.class)))
				.thenReturn(entityMeta);
		assertNull(metaDataServiceImpl.getRepository(entityName));
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void getRepositoryUnknownEntity()
	{
		metaDataServiceImpl.getRepository("unknownEntity");
	}

	@Test
	public void getRepositoryTyped()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityMeta.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityMetaData.class)))
				.thenReturn(entityMeta);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		//noinspection unchecked
		Repository<Package> repo = mock(Repository.class);
		//noinspection unchecked
		when(repoCollection.getRepository(entityMeta)).thenReturn((Repository<Entity>) (Repository<?>) repo);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		assertEquals(metaDataServiceImpl.getRepository(entityName, Package.class), repo);
	}

	@Test
	public void getRepositoryTypedAbstractEntityMeta()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(true).getMock();
		String backendName = "backend";
		when(entityMeta.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityMetaData.class)))
				.thenReturn(entityMeta);
		assertNull(metaDataServiceImpl.getRepository(entityName, Package.class));
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void getRepositoryTypedUnknownEntity()
	{
		metaDataServiceImpl.getRepository("unknownEntity", Package.class);
	}

	@Test
	public void getRepositoryEntityMeta()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityMeta.getBackend()).thenReturn(backendName);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		//noinspection unchecked
		Repository<Entity> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityMeta)).thenReturn(repo);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		assertEquals(metaDataServiceImpl.getRepository(entityMeta), repo);
	}

	@Test
	public void getRepositoryEntityMetaAbstract()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(true).getMock();
		assertNull(metaDataServiceImpl.getRepository(entityMeta));
	}

	@Test
	public void getRepositoryTypedEntityMeta()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityMeta.getBackend()).thenReturn(backendName);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		//noinspection unchecked
		Repository<Package> repo = mock(Repository.class);
		//noinspection unchecked
		when(repoCollection.getRepository(entityMeta)).thenReturn((Repository<Entity>) (Repository<?>) repo);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		assertEquals(metaDataServiceImpl.getRepository(entityMeta, Package.class), repo);
	}

	@Test
	public void getRepositoryTypedEntityMetaAbstract()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(true).getMock();
		assertNull(metaDataServiceImpl.getRepository(entityMeta, Package.class));
	}

	@Test
	public void getRepositories()
	{
		String backendName0 = "backend0";
		String backendName1 = "backend1";

		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.getBackend()).thenReturn(backendName0);
		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		when(entityMeta1.getBackend()).thenReturn(backendName1);

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(entityQ.eq(ABSTRACT, false)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findAll()).thenReturn(Stream.of(entityMeta0, entityMeta1));
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		RepositoryCollection repoCollection0 = mock(RepositoryCollection.class);
		//noinspection unchecked
		Repository<Entity> repo0 = mock(Repository.class);
		when(repoCollection0.getRepository(entityMeta0)).thenReturn(repo0);
		when(repoCollectionRegistry.getRepositoryCollection(backendName0)).thenReturn(repoCollection0);
		//noinspection unchecked
		Repository<Entity> repo1 = mock(Repository.class);
		RepositoryCollection repoCollection1 = mock(RepositoryCollection.class);
		when(repoCollection1.getRepository(entityMeta1)).thenReturn(repo1);
		when(repoCollectionRegistry.getRepositoryCollection(backendName1)).thenReturn(repoCollection1);
		List<Repository<Entity>> expectedRepos = newArrayList(repo0, repo1);
		assertEquals(metaDataServiceImpl.getRepositories().collect(toList()), expectedRepos);
	}

	@Test
	public void hasRepository()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(false).getMock();

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		//noinspection unchecked
		Query<EntityMetaData> entityQ2 = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.and()).thenReturn(entityQ2);
		when(entityQ2.eq(ABSTRACT, false)).thenReturn(entityQ2);
		when(entityQ2.findOne()).thenReturn(entityMeta);

		assertTrue(metaDataServiceImpl.hasRepository(entityName));
	}

	@Test
	public void hasRepositoryAbstractEntityMeta()
	{
		String entityName = "entity";

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		//noinspection unchecked
		Query<EntityMetaData> entityQ2 = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.and()).thenReturn(entityQ2);
		when(entityQ2.eq(ABSTRACT, false)).thenReturn(entityQ2);
		when(entityQ2.findOne()).thenReturn(null);

		assertFalse(metaDataServiceImpl.hasRepository(entityName));
	}

	@Test
	public void createRepository()
	{
		String backendName = "backend";

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getBackend()).thenReturn(backendName);
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));

		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		//noinspection unchecked
		Repository<Entity> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityMeta)).thenReturn(repo);
		assertEquals(metaDataServiceImpl.createRepository(entityMeta), repo);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_META_DATA, entityMeta);

		verifyNoMoreInteractions(dataService);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void createRepositoryAbstractEntityMeta()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(true).getMock();
		metaDataServiceImpl.createRepository(entityMeta);
	}

	@Test
	public void createRepositoryTyped()
	{
		String backendName = "backend";
		Class<Package> entityClass = Package.class;
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getBackend()).thenReturn(backendName);
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));

		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		//noinspection unchecked
		Repository<Package> repo = mock(Repository.class);
		//noinspection unchecked
		when(repoCollection.getRepository(entityMeta)).thenReturn((Repository<Entity>) (Repository<?>) repo);
		assertEquals(metaDataServiceImpl.createRepository(entityMeta, entityClass), repo);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_META_DATA, entityMeta);

		verifyNoMoreInteractions(dataService);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void createRepositoryTypedAbstractEntityMeta()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).isAbstract()).thenReturn(true).getMock();
		metaDataServiceImpl.createRepository(entityMeta, Package.class);
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
		//noinspection unchecked
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
		Package packageNew = when(mock(Package.class).getName()).thenReturn(newPackageName).getMock();
		String updatedPackageName = "updatedPackage";
		Package packageUpdated = when(mock(Package.class).getName()).thenReturn(updatedPackageName).getMock();
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
	public void getEntityMeta()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityMetaData.class)))
				.thenReturn(entityMeta);
		assertEquals(metaDataServiceImpl.getEntityMetaData(entityName), entityMeta);
	}

	@Test
	public void getEntityMetaUnknownEntity()
	{
		String entityName = "entity";
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityMetaData.class)))
				.thenReturn(null);
		assertNull(metaDataServiceImpl.getEntityMetaData(entityName));
	}

	// TODO how to test forEach?
	//	@Test
	//	public void getEntityMetas()
	//	{
	//
	//	}

	@Test
	public void addEntityMeta()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));
		metaDataServiceImpl.addEntityMeta(entityMeta);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_META_DATA, entityMeta);

		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void addEntityMetasEmpty()
	{
		metaDataServiceImpl.addEntityMeta(emptyList());
		verifyZeroInteractions(dataService);
	}

	@Test
	public void addEntityMetasNoMappedByAttrs()
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.hasMappedByAttributes()).thenReturn(false);
		Attribute entity0Attr0 = mock(Attribute.class);
		Attribute entity0Attr1 = mock(Attribute.class);
		when(entityMeta0.getOwnAllAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));

		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		when(entityMeta1.hasMappedByAttributes()).thenReturn(false);
		Attribute entity1Attr0 = mock(Attribute.class);
		Attribute entity1Attr1 = mock(Attribute.class);
		when(entityMeta1.getOwnAllAttributes()).thenReturn(newArrayList(entity1Attr0, entity1Attr1));

		when(entityMetaDependencyResolver.resolve(newArrayList(entityMeta0, entityMeta1)))
				.thenReturn(newArrayList(entityMeta1, entityMeta0));
		metaDataServiceImpl.addEntityMeta(newArrayList(entityMeta0, entityMeta1));

		InOrder inOrder = inOrder(dataService);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrsCaptor1 = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor1.capture());
		assertEquals(attrsCaptor1.getValue().collect(toList()), newArrayList(entity1Attr0, entity1Attr1));
		inOrder.verify(dataService).add(ENTITY_META_DATA, entityMeta1);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrsCaptor0 = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0.capture());
		assertEquals(attrsCaptor0.getValue().collect(toList()), newArrayList(entity0Attr0, entity0Attr1));
		inOrder.verify(dataService).add(ENTITY_META_DATA, entityMeta0);

		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void addEntityMetasMappedByAttrs()
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.getName()).thenReturn("entity0");
		when(entityMeta0.getSimpleName()).thenReturn("entity0");
		when(entityMeta0.hasMappedByAttributes()).thenReturn(true);

		Attribute entity0Attr0 = mock(Attribute.class);
		when(entity0Attr0.getName()).thenReturn("entity0Attr0");
		when(entity0Attr0.getIdentifier()).thenReturn("id00");
		when(entity0Attr0.getAttributeParts()).thenReturn(emptyList());
		when(entity0Attr0.getTags()).thenReturn(emptyList());
		when(entity0Attr0.isMappedBy()).thenReturn(true);

		Attribute entity0Attr1 = mock(Attribute.class);
		when(entity0Attr1.getName()).thenReturn("entity0Attr1");
		when(entity0Attr1.getIdentifier()).thenReturn("id01");
		when(entity0Attr1.getAttributeParts()).thenReturn(emptyList());
		when(entity0Attr1.getTags()).thenReturn(emptyList());

		when(entityMeta0.getOwnAllAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
		when(entityMeta0.getOwnAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
		when(entityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityMeta0.getTags()).thenReturn(emptyList());

		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		when(entityMeta1.hasMappedByAttributes()).thenReturn(false);
		Attribute entity1Attr0 = mock(Attribute.class);
		Attribute entity1Attr1 = mock(Attribute.class);
		when(entityMeta1.getOwnAllAttributes()).thenReturn(newArrayList(entity1Attr0, entity1Attr1));

		when(entityMetaDependencyResolver.resolve(newArrayList(entityMeta0, entityMeta1)))
				.thenReturn(newArrayList(entityMeta1, entityMeta0));
		metaDataServiceImpl.addEntityMeta(newArrayList(entityMeta0, entityMeta1));

		InOrder inOrder = inOrder(dataService);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrsCaptor1 = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor1.capture());
		assertEquals(attrsCaptor1.getValue().collect(toList()), newArrayList(entity1Attr0, entity1Attr1));
		inOrder.verify(dataService).add(ENTITY_META_DATA, entityMeta1);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrsCaptor0 = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0.capture());
		assertEquals(attrsCaptor0.getValue().collect(toList()), singletonList(entity0Attr1));

		ArgumentCaptor<EntityMetaData> entityCaptor0 = ArgumentCaptor.forClass(EntityMetaData.class);
		inOrder.verify(dataService).add(eq(ENTITY_META_DATA), entityCaptor0.capture());
		assertEquals(newArrayList(entityCaptor0.getValue().getOwnAllAttributes()), singletonList(entity0Attr1));

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrsCaptor0b = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0b.capture());
		assertEquals(attrsCaptor0b.getValue().collect(toList()), singletonList(entity0Attr0));

		ArgumentCaptor<EntityMetaData> entityCaptor0b = ArgumentCaptor.forClass(EntityMetaData.class);
		inOrder.verify(dataService).update(eq(ENTITY_META_DATA), entityCaptor0b.capture());
		assertEquals(entityCaptor0b.getValue(), entityMeta0);

		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityMeta()
	{
		String entityName = "entity";
		metaDataServiceImpl.deleteEntityMeta(entityName);
		verify(dataService).deleteById(ENTITY_META_DATA, entityName);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityMetaCollection()
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.hasMappedByAttributes()).thenReturn(false);
		String entityName0 = "entity0";
		when(entityMeta0.getName()).thenReturn(entityName0);

		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		when(entityMeta1.hasMappedByAttributes()).thenReturn(false);
		String entityName1 = "entity1";
		when(entityMeta1.getName()).thenReturn(entityName1);

		when(entityMetaDependencyResolver.resolve(newArrayList(entityMeta0, entityMeta1)))
				.thenReturn(newArrayList(entityMeta1, entityMeta0));

		metaDataServiceImpl.deleteEntityMeta(newArrayList(entityMeta0, entityMeta1));

		//noinspection unchecked
		ArgumentCaptor<Stream<Object>> entityIdCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).deleteAll(eq(ENTITY_META_DATA), entityIdCaptor.capture());
		assertEquals(entityIdCaptor.getValue().collect(toList()), newArrayList(entityName0, entityName1));
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityMetaCollectionEmpty()
	{
		metaDataServiceImpl.deleteEntityMeta(emptyList());
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityMetaCollectionMappedBy()
	{
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		String entityName0 = "entity0";
		when(entityMeta0.getName()).thenReturn(entityName0);
		when(entityMeta0.getSimpleName()).thenReturn(entityName0);
		when(entityMeta0.hasMappedByAttributes()).thenReturn(true);

		Attribute entity0Attr0 = mock(Attribute.class);
		when(entity0Attr0.getName()).thenReturn("entity0Attr0");
		when(entity0Attr0.getIdentifier()).thenReturn("id00");
		when(entity0Attr0.getAttributeParts()).thenReturn(emptyList());
		when(entity0Attr0.getTags()).thenReturn(emptyList());
		when(entity0Attr0.isMappedBy()).thenReturn(true);

		Attribute entity0Attr1 = mock(Attribute.class);
		when(entity0Attr1.getName()).thenReturn("entity0Attr1");
		when(entity0Attr1.getIdentifier()).thenReturn("id01");
		when(entity0Attr1.getAttributeParts()).thenReturn(emptyList());
		when(entity0Attr1.getTags()).thenReturn(emptyList());

		when(entityMeta0.getOwnAllAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
		when(entityMeta0.getOwnAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
		when(entityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityMeta0.getTags()).thenReturn(emptyList());

		EntityMetaData entityMeta1 = mock(EntityMetaData.class);
		String entityName1 = "entity1";
		when(entityMeta1.getName()).thenReturn(entityName1);
		when(entityMeta1.hasMappedByAttributes()).thenReturn(false);
		Attribute entity1Attr0 = mock(Attribute.class);
		Attribute entity1Attr1 = mock(Attribute.class);
		when(entityMeta1.getOwnAllAttributes()).thenReturn(newArrayList(entity1Attr0, entity1Attr1));

		when(entityMetaDependencyResolver.resolve(newArrayList(entityMeta0, entityMeta1)))
				.thenReturn(newArrayList(entityMeta1, entityMeta0));

		InOrder inOrder = inOrder(dataService);
		metaDataServiceImpl.deleteEntityMeta(newArrayList(entityMeta0, entityMeta1));

		//noinspection unchecked
		ArgumentCaptor<Stream<EntityMetaData>> entityCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).update(eq(ENTITY_META_DATA), entityCaptor.capture());
		List<EntityMetaData> updatedEntities = entityCaptor.getValue().collect(toList());
		assertEquals(updatedEntities.size(), 1);
		assertEquals(updatedEntities.get(0).getName(), entityName0);
		assertEquals(newArrayList(updatedEntities.get(0).getOwnAllAttributes()), newArrayList(entity0Attr1));

		//noinspection unchecked
		ArgumentCaptor<Stream<Object>> entityIdCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).deleteAll(eq(ENTITY_META_DATA), entityIdCaptor.capture());
		assertEquals(entityIdCaptor.getValue().collect(toList()), newArrayList(entityName0, entityName1));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void updateEntityMeta()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		Attribute attrShared0 = when(mock(Attribute.class).getName()).thenReturn(attrShared0Name)
				.getMock();
		when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
		when(attrShared0.getAttributeParts()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		Attribute attrShared1 = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		Attribute attrShared1Updated = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getAttributeParts()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		Attribute attrDeleted = when(mock(Attribute.class).getName()).thenReturn(attrDeletedName)
				.getMock();
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedName);
		when(attrDeleted.getAttributeParts()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityMetaData existingEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(existingEntityMeta.getLabel()).thenReturn("label");
		when(existingEntityMeta.getSimpleName()).thenReturn(entityName);
		when(existingEntityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getTags()).thenReturn(emptyList());

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(entityMeta.getLabel()).thenReturn("new label");
		when(entityMeta.getSimpleName()).thenReturn(entityName);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(entityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityMeta.getTags()).thenReturn(emptyList());

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityMeta);

		metaDataServiceImpl.updateEntityMeta(entityMeta);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		verify(dataService).update(ENTITY_META_DATA, entityMeta);
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void updateEntityMetaEntityDoesNotExist()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		metaDataServiceImpl.updateEntityMeta(entityMeta);
	}

	@Test
	public void updateEntityMetaCollectionEmpty()
	{
		metaDataServiceImpl.updateEntityMeta(emptyList());
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void updateEntityMetaCollection()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		Attribute attrShared0 = when(mock(Attribute.class).getName()).thenReturn(attrShared0Name)
				.getMock();
		when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
		when(attrShared0.getAttributeParts()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		Attribute attrShared1 = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		Attribute attrShared1Updated = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getAttributeParts()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		Attribute attrDeleted = when(mock(Attribute.class).getName()).thenReturn(attrDeletedName)
				.getMock();
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedName);
		when(attrDeleted.getAttributeParts()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityMetaData existingEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(existingEntityMeta.getLabel()).thenReturn("label");
		when(existingEntityMeta.getSimpleName()).thenReturn(entityName);
		when(existingEntityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic
		when(existingEntityMeta.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(entityMeta.getLabel()).thenReturn("new label");
		when(entityMeta.getSimpleName()).thenReturn(entityName);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(entityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityMeta.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic
		when(entityMeta.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});
		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityMeta);

		when(entityMetaDependencyResolver.resolve(singletonList(entityMeta))).thenReturn(singletonList(entityMeta));

		//noinspection unchecked
		when(dataService.findAll(eq(ENTITY_META_DATA), (Stream<Object>) any(Stream.class), eq(EntityMetaData.class)))
				.thenReturn(Stream.of(existingEntityMeta));

		metaDataServiceImpl.updateEntityMeta(singletonList(entityMeta));

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		verify(dataService).update(ENTITY_META_DATA, entityMeta);
	}

	@Test
	public void updateEntityMetaCollectionMappedByExisting()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		Attribute attrShared0 = when(mock(Attribute.class).getName()).thenReturn(attrShared0Name)
				.getMock();
		when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
		when(attrShared0.getAttributeParts()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		Attribute attrShared1 = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		when(attrShared1.isMappedBy()).thenReturn(true);
		Attribute attrShared1Updated = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		when(attrShared1.isMappedBy()).thenReturn(false);
		Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getAttributeParts()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		Attribute attrDeleted = when(mock(Attribute.class).getName()).thenReturn(attrDeletedName)
				.getMock();
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedName);
		when(attrDeleted.getAttributeParts()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityMetaData existingEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(existingEntityMeta.getLabel()).thenReturn("label");
		when(existingEntityMeta.getSimpleName()).thenReturn(entityName);
		when(existingEntityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic
		when(existingEntityMeta.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(attrShared1);
			}
		});

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(entityMeta.getLabel()).thenReturn("new label");
		when(entityMeta.getSimpleName()).thenReturn(entityName);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(entityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityMeta.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic
		when(entityMeta.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(attrShared1Updated);
			}
		});

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityMeta);

		when(entityMetaDependencyResolver.resolve(singletonList(entityMeta))).thenReturn(singletonList(entityMeta));

		//noinspection unchecked
		when(dataService.findAll(eq(ENTITY_META_DATA), (Stream<Object>) any(Stream.class), eq(EntityMetaData.class)))
				.thenReturn(Stream.of(existingEntityMeta));

		metaDataServiceImpl.updateEntityMeta(singletonList(entityMeta));

		InOrder inOrder = inOrder(dataService);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		inOrder.verify(dataService).update(ENTITY_META_DATA, entityMeta);

		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void updateEntityMetaCollectionMappedByNew()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		Attribute attrShared0 = when(mock(Attribute.class).getName()).thenReturn(attrShared0Name)
				.getMock();
		when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
		when(attrShared0.getAttributeParts()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		Attribute attrShared1 = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		when(attrShared1.isMappedBy()).thenReturn(true);
		Attribute attrShared1Updated = when(mock(Attribute.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		when(attrShared1Updated.isMappedBy()).thenReturn(false);
		Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getAttributeParts()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		Attribute attrDeleted = when(mock(Attribute.class).getName()).thenReturn(attrDeletedName)
				.getMock();
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedName);
		when(attrDeleted.getAttributeParts()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityMetaData existingEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(existingEntityMeta.getLabel()).thenReturn("label");
		when(existingEntityMeta.getSimpleName()).thenReturn(entityName);
		when(existingEntityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic
		when(existingEntityMeta.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(attrShared1);
			}
		});

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(entityMeta.getLabel()).thenReturn("new label");
		when(entityMeta.getSimpleName()).thenReturn(entityName);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(entityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityMeta.getTags()).thenReturn(emptyList());
		//noinspection AnonymousInnerClassMayBeStatic
		when(entityMeta.getOwnMappedByAttributes()).thenAnswer(new Answer<Stream<Attribute>>()
		{
			@Override
			public Stream<Attribute> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityMeta);

		when(entityMetaDependencyResolver.resolve(singletonList(entityMeta))).thenReturn(singletonList(entityMeta));

		//noinspection unchecked
		when(dataService.findAll(eq(ENTITY_META_DATA), (Stream<Object>) any(Stream.class), eq(EntityMetaData.class)))
				.thenReturn(Stream.of(existingEntityMeta));

		InOrder inOrder = inOrder(dataService);

		metaDataServiceImpl.updateEntityMeta(singletonList(entityMeta));

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		inOrder.verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		inOrder.verify(dataService).update(ENTITY_META_DATA, entityMeta);

		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void addAttribute()
	{
		Attribute attr = mock(Attribute.class);
		metaDataServiceImpl.addAttribute(attr);
		verify(dataService).add(ATTRIBUTE_META_DATA, attr);
	}

	@Test
	public void deleteAttributeById()
	{
		Object attrId = "attr0";
		metaDataServiceImpl.deleteAttributeById(attrId);
		verify(dataService).deleteById(ATTRIBUTE_META_DATA, attrId);
	}

	@DataProvider(name = "isMetaEntityMetaDataProvider")
	public static Iterator<Object[]> isMetaEntityMetaDataProvider()
	{
		return newArrayList(new Object[] { ENTITY_META_DATA, true }, new Object[] { ATTRIBUTE_META_DATA, true },
				new Object[] { TAG, true }, new Object[] { PACKAGE, true }, new Object[] { "noMeta", false })
				.iterator();
	}

	@Test(dataProvider = "isMetaEntityMetaDataProvider")
	public void isMetaEntityMetaData(String entityName, boolean isMeta)
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		assertEquals(metaDataServiceImpl.isMetaEntityMetaData(entityMeta), isMeta);
	}

	@Test
	public void upsertEntityMetaCollectionEmpty()
	{
		metaDataServiceImpl.upsertEntityMeta(emptyList());
		verifyZeroInteractions(dataService);
	}
}
