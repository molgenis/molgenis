package org.molgenis.data.meta;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
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

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		repoCollectionRegistry = mock(RepositoryCollectionRegistry.class);
		SystemEntityTypeRegistry systemEntityTypeRegistry = mock(SystemEntityTypeRegistry.class);
		metaDataServiceImpl = new MetaDataServiceImpl(dataService, repoCollectionRegistry, systemEntityTypeRegistry);
	}

	@Test
	public void getLanguageCodes()
	{
		RepositoryCollection defaultRepoCollection = mock(RepositoryCollection.class);
		when(defaultRepoCollection.getLanguageCodes()).thenAnswer(new Answer<Stream<String>>()
		{
			@Override
			public Stream<String> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of("en", "nl");
			}
		});
		when(repoCollectionRegistry.getDefaultRepoCollection()).thenReturn(defaultRepoCollection);
		assertEquals(metaDataServiceImpl.getLanguageCodes().collect(toList()), asList("en", "nl"));
	}

	@Test
	public void getRepository()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(entityType);
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
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
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(entityType);
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
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
		String backendName = "backend";
		when(entityType.getBackend()).thenReturn(backendName);
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
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
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
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

		Query<EntityType> entityQ = mock(Query.class);
		when(entityQ.eq(ABSTRACT, false)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findAll()).thenReturn(Stream.of(entityType0, entityType1));
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(entityQ);
		RepositoryCollection repoCollection0 = mock(RepositoryCollection.class);
		Repository<Entity> repo0 = mock(Repository.class);
		when(repoCollection0.getRepository(entityType0)).thenReturn(repo0);
		when(repoCollectionRegistry.getRepositoryCollection(backendName0)).thenReturn(repoCollection0);
		Repository<Entity> repo1 = mock(Repository.class);
		RepositoryCollection repoCollection1 = mock(RepositoryCollection.class);
		when(repoCollection1.getRepository(entityType1)).thenReturn(repo1);
		when(repoCollectionRegistry.getRepositoryCollection(backendName1)).thenReturn(repoCollection1);
		assertEquals(metaDataServiceImpl.getRepositories().collect(toList()), newArrayList(repo0, repo1));
	}

	@Test
	public void hasRepository()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();

		Query<EntityType> entityQ = mock(Query.class);
		Query<EntityType> entityQ2 = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.and()).thenReturn(entityQ2);
		when(entityQ2.eq(ABSTRACT, false)).thenReturn(entityQ2);
		when(entityQ2.findOne()).thenReturn(entityType);

		assertTrue(metaDataServiceImpl.hasRepository(entityName));
	}

	@Test
	public void hasRepositoryAbstractEntityType()
	{
		String entityName = "entity";

		Query<EntityType> entityQ = mock(Query.class);
		Query<EntityType> entityQ2 = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(entityQ);
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

		EntityType entityType = mock(EntityType.class);
		when(entityType.getBackend()).thenReturn(backendName);
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));

		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		Repository<Entity> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityType)).thenReturn(repo);
		assertEquals(metaDataServiceImpl.createRepository(entityType), repo);

		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_META_DATA, entityType);

		verifyNoMoreInteractions(dataService);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void createRepositoryAbstractEntityType()
	{
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
		metaDataServiceImpl.createRepository(entityType);
	}

	@Test
	public void createRepositoryTyped()
	{
		String backendName = "backend";
		Class<Package> entityClass = Package.class;
		EntityType entityType = mock(EntityType.class);
		when(entityType.getBackend()).thenReturn(backendName);
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));

		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
		Repository<Package> repo = mock(Repository.class);
		when(repoCollection.getRepository(entityType)).thenReturn((Repository<Entity>) (Repository<?>) repo);
		assertEquals(metaDataServiceImpl.createRepository(entityType, entityClass), repo);

		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_META_DATA, entityType);

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
		RepositoryCollection repo = mock(RepositoryCollection.class);
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
	public void getEntityType()
	{
		String entityName = "entity";
		EntityType entityType = mock(EntityType.class);
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(entityType);
		assertEquals(metaDataServiceImpl.getEntityType(entityName), entityType);
	}

	@Test
	public void getEntityTypeUnknownEntity()
	{
		String entityName = "entity";
		when(dataService.findOneById(eq(ENTITY_META_DATA), eq(entityName), any(Fetch.class), eq(EntityType.class)))
				.thenReturn(null);
		assertNull(metaDataServiceImpl.getEntityType(entityName));
	}

	// TODO how to test forEach?
	//	@Test
	//	public void getEntityTypes()
	//	{
	//
	//	}

	@Test
	public void addEntityType()
	{
		EntityType entityType = mock(EntityType.class);
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));
		metaDataServiceImpl.addEntityType(entityType);

		ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
		assertEquals(attrsCaptor.getValue().collect(toList()), newArrayList(attr0, attr1));

		verify(dataService).add(ENTITY_META_DATA, entityType);

		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityType()
	{
		String entityName = "entity";
		metaDataServiceImpl.deleteEntityType(entityName);
		verify(dataService).deleteById(ENTITY_META_DATA, entityName);
		verifyNoMoreInteractions(dataService);
	}

	// TODO implement test once DependencyResolver is a dependency instead of used as a static class
	//	@Test
	//	public void deleteEntities()
	//	{
	//	}

	@Test
	public void updateEntityType()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		AttributeMetaData attrShared0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attrShared0Name)
				.getMock();
		when(attrShared0.getAttributeParts()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		AttributeMetaData attrShared1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		AttributeMetaData attrShared1Updated = when(mock(AttributeMetaData.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		AttributeMetaData attrAdded = when(mock(AttributeMetaData.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getAttributeParts()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		AttributeMetaData attrDeleted = when(mock(AttributeMetaData.class).getName()).thenReturn(attrDeletedName)
				.getMock();
		when(attrDeleted.getAttributeParts()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityType existingEntityType = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		when(existingEntityType.getLabel()).thenReturn("label");
		when(existingEntityType.getSimpleName()).thenReturn(entityName);
		when(existingEntityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityType.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityType.getTags()).thenReturn(emptyList());

		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		when(entityType.getLabel()).thenReturn("new label");
		when(entityType.getSimpleName()).thenReturn(entityName);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityType.getOwnAttributes()).thenReturn(emptyList());
		when(entityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityType.getTags()).thenReturn(emptyList());

		Query<EntityType> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityType);

		metaDataServiceImpl.updateEntityType(entityType);

		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		verify(dataService).update(ENTITY_META_DATA, entityType);
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void updateEntityTypeEntityDoesNotExist()
	{
		String entityName = "entity";
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		Query<EntityType> entityQ = mock(Query.class);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.fetch(any())).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_META_DATA, EntityType.class)).thenReturn(entityQ);
		metaDataServiceImpl.updateEntityType(entityType);
	}

	@Test
	public void addAttribute()
	{
		AttributeMetaData attr = mock(AttributeMetaData.class);
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

	@DataProvider(name = "isMetaEntityTypeProvider")
	public static Iterator<Object[]> isMetaEntityTypeProvider()
	{
		return newArrayList(new Object[] { ENTITY_META_DATA, true }, new Object[] { ATTRIBUTE_META_DATA, true },
				new Object[] { TAG, true }, new Object[] { PACKAGE, true }, new Object[] { "noMeta", false })
				.iterator();
	}

	@Test(dataProvider = "isMetaEntityTypeProvider")
	public void isMetaEntityType(String entityName, boolean isMeta)
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		assertEquals(metaDataServiceImpl.isMetaEntityType(entityType), isMeta);
	}
}
