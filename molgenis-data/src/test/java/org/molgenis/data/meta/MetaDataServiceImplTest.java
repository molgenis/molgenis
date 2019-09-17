package org.molgenis.data.meta;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.REF_ENTITY_TYPE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ID;
import static org.molgenis.data.meta.model.EntityTypeMetadata.IS_ABSTRACT;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetadata.PARENT;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.data.util.EntityTypeUtils.getEntityTypeFetch;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionRegistry;
import org.molgenis.data.RepositoryCreationException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryCollectionException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.persist.PackagePersister;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.test.AbstractMockitoTest;

@SuppressWarnings("deprecation")
class MetaDataServiceImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private RepositoryCollectionRegistry repoCollectionRegistry;
  @Mock private SystemEntityTypeRegistry systemEntityTypeRegistry;
  @Mock private EntityTypeDependencyResolver entityTypeDependencyResolver;
  @Mock private PackagePersister packagePersister;

  private MetaDataServiceImpl metaDataServiceImpl;

  MetaDataServiceImplTest() {
    super(Strictness.WARN);
  }

  @BeforeEach
  void setUpBeforeMethod() {
    metaDataServiceImpl =
        new MetaDataServiceImpl(
            dataService,
            repoCollectionRegistry,
            systemEntityTypeRegistry,
            entityTypeDependencyResolver,
            packagePersister);
  }

  @Test
  void getRepository() {
    String entityTypeId = "entity";
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
    String backendName = "backend";
    when(entityType.getBackend()).thenReturn(backendName);
    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA), eq(entityTypeId), any(Fetch.class), eq(EntityType.class)))
        .thenReturn(entityType);
    RepositoryCollection repoCollection = mock(RepositoryCollection.class);
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repoCollection.getRepository(entityType)).thenReturn(repo);
    when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
    assertEquals(of(repo), metaDataServiceImpl.getRepository(entityTypeId));
  }

  @Test
  void getRepositoryAbstractEntityType() {
    String entityTypeId = "entity";
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA), eq(entityTypeId), any(Fetch.class), eq(EntityType.class)))
        .thenReturn(entityType);
    assertEquals(empty(), metaDataServiceImpl.getRepository(entityTypeId));
  }

  @Test
  void getRepositoryUnknownEntity() {
    String unknownEntityTypeId = "unknownEntity";
    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA),
            eq(unknownEntityTypeId),
            any(Fetch.class),
            eq(EntityType.class)))
        .thenReturn(null);
    assertThrows(
        UnknownEntityTypeException.class,
        () -> metaDataServiceImpl.getRepository(unknownEntityTypeId));
  }

  @SuppressWarnings("unchecked")
  @Test
  void getRepositoryTyped() {
    String entityTypeId = "entity";
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
    String backendName = "backend";
    when(entityType.getBackend()).thenReturn(backendName);
    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA), eq(entityTypeId), any(Fetch.class), eq(EntityType.class)))
        .thenReturn(entityType);
    RepositoryCollection repoCollection = mock(RepositoryCollection.class);
    Repository<Package> repo = mock(Repository.class);

    when(repoCollection.getRepository(entityType))
        .thenReturn((Repository<Entity>) (Repository<?>) repo);
    when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
    assertEquals(
        of(repo),
        metaDataServiceImpl.getRepository(
            entityTypeId, org.molgenis.data.meta.model.Package.class));
  }

  @Test
  void getRepositoryTypedAbstractEntityType() {
    String entityTypeId = "entity";
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA), eq(entityTypeId), any(Fetch.class), eq(EntityType.class)))
        .thenReturn(entityType);
    assertEquals(
        empty(),
        metaDataServiceImpl.getRepository(
            entityTypeId, org.molgenis.data.meta.model.Package.class));
  }

  @Test
  void getRepositoryTypedUnknownEntity() {
    String unknownEntityTypeId = "unknownEntity";
    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA),
            eq(unknownEntityTypeId),
            any(Fetch.class),
            eq(EntityType.class)))
        .thenReturn(null);
    assertThrows(
        UnknownEntityTypeException.class,
        () -> metaDataServiceImpl.getRepository(unknownEntityTypeId, Package.class));
  }

  @Test
  void getRepositoryEntityType() {
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
    String backendName = "backend";
    when(entityType.getBackend()).thenReturn(backendName);
    RepositoryCollection repoCollection = mock(RepositoryCollection.class);
    @SuppressWarnings("unchecked")
    Repository<Entity> repo = mock(Repository.class);
    when(repoCollection.getRepository(entityType)).thenReturn(repo);
    when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
    assertEquals(of(repo), metaDataServiceImpl.getRepository(entityType));
  }

  @Test
  void getRepositoryEntityTypeAbstract() {
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
    assertEquals(empty(), metaDataServiceImpl.getRepository(entityType));
  }

  @SuppressWarnings("unchecked")
  @Test
  void getRepositoryTypedEntityType() {
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(false).getMock();
    String backendName = "backend";
    when(entityType.getBackend()).thenReturn(backendName);
    RepositoryCollection repoCollection = mock(RepositoryCollection.class);
    Repository<Package> repo = mock(Repository.class);

    when(repoCollection.getRepository(entityType))
        .thenReturn((Repository<Entity>) (Repository<?>) repo);

    when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repoCollection);
    assertEquals(of(repo), metaDataServiceImpl.getRepository(entityType, Package.class));
  }

  @Test
  void getRepositoryTypedEntityTypeAbstract() {
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
    assertEquals(empty(), metaDataServiceImpl.getRepository(entityType, Package.class));
  }

  @Test
  void getRepositories() {
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
    doReturn(repoCollection0).when(repoCollectionRegistry).getRepositoryCollection(backendName0);
    @SuppressWarnings("unchecked")
    Repository<Entity> repo1 = mock(Repository.class);
    RepositoryCollection repoCollection1 = mock(RepositoryCollection.class);
    when(repoCollection1.getRepository(entityType1)).thenReturn(repo1);
    doReturn(repoCollection1).when(repoCollectionRegistry).getRepositoryCollection(backendName1);
    List<Repository<Entity>> expectedRepos = newArrayList(repo0, repo1);
    assertEquals(expectedRepos, metaDataServiceImpl.getRepositories().collect(toList()));
  }

  @Test
  void hasRepository() {
    String entityTypeId = "entity";
    EntityType entityType = mock(EntityType.class);

    @SuppressWarnings("unchecked")
    Query<EntityType> entityQ = mock(Query.class);
    @SuppressWarnings("unchecked")
    Query<EntityType> entityQ2 = mock(Query.class);
    when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);

    when(entityQ.eq(ID, entityTypeId)).thenReturn(entityQ);
    when(entityQ.and()).thenReturn(entityQ2);
    when(entityQ2.eq(IS_ABSTRACT, false)).thenReturn(entityQ2);
    when(entityQ2.findOne()).thenReturn(entityType);

    assertTrue(metaDataServiceImpl.hasRepository(entityTypeId));
  }

  @Test
  void hasRepositoryAbstractEntityType() {
    String entityTypeId = "entity";

    @SuppressWarnings("unchecked")
    Query<EntityType> entityQ = mock(Query.class);
    @SuppressWarnings("unchecked")
    Query<EntityType> entityQ2 = mock(Query.class);
    when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);

    when(entityQ.eq(ID, entityTypeId)).thenReturn(entityQ);
    when(entityQ.and()).thenReturn(entityQ2);
    when(entityQ2.eq(IS_ABSTRACT, false)).thenReturn(entityQ2);
    when(entityQ2.findOne()).thenReturn(null);

    assertFalse(metaDataServiceImpl.hasRepository(entityTypeId));
  }

  @Test
  void createRepository() {
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
    assertEquals(repo, metaDataServiceImpl.createRepository(entityType));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
    assertEquals(newArrayList(attr0, attr1), attrsCaptor.getValue().collect(toList()));

    verify(dataService).add(ENTITY_TYPE_META_DATA, entityType);

    verifyNoMoreInteractions(dataService);
  }

  @Test
  void createRepositoryAbstractEntityType() {
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
    assertThrows(
        RepositoryCreationException.class, () -> metaDataServiceImpl.createRepository(entityType));
  }

  @SuppressWarnings("unchecked")
  @Test
  void createRepositoryTyped() {
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
    when(repoCollection.getRepository(entityType))
        .thenReturn((Repository<Entity>) (Repository<?>) repo);
    assertEquals(repo, metaDataServiceImpl.createRepository(entityType, entityClass));

    ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
    assertEquals(newArrayList(attr0, attr1), attrsCaptor.getValue().collect(toList()));

    verify(dataService).add(ENTITY_TYPE_META_DATA, entityType);

    verifyNoMoreInteractions(dataService);
  }

  @Test
  void createRepositoryTypedAbstractEntityType() {
    EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
    assertThrows(
        RepositoryCreationException.class,
        () -> metaDataServiceImpl.createRepository(entityType, Package.class));
  }

  @Test
  void getBackend() {
    String backendName = "backend";
    RepositoryCollection repo = mock(RepositoryCollection.class);
    when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repo);
    assertEquals(repo, metaDataServiceImpl.getBackend(backendName));
  }

  @Test
  void getBackendUnknown() {
    String backendName = "backend";
    when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(null);
    assertThrows(
        UnknownRepositoryCollectionException.class,
        () -> metaDataServiceImpl.getBackend(backendName));
  }

  @Test
  void getBackendEntityType() {
    String backendName = "backend";
    EntityType entityType =
        when(mock(EntityType.class).getBackend()).thenReturn(backendName).getMock();
    RepositoryCollection repo = mock(RepositoryCollection.class);
    when(repoCollectionRegistry.getRepositoryCollection(backendName)).thenReturn(repo);
    assertEquals(repo, metaDataServiceImpl.getBackend(entityType));
  }

  @Test
  void getBackendEntityTypeUnknownBackend() {
    String backendName = "backend";
    EntityType entityType =
        when(mock(EntityType.class).getBackend()).thenReturn(backendName).getMock();
    assertThrows(
        UnknownRepositoryCollectionException.class,
        () -> metaDataServiceImpl.getBackend(entityType));
  }

  @Test
  void hasBackendFalse() {
    String backendName = "backend";
    when(repoCollectionRegistry.hasRepositoryCollection(backendName)).thenReturn(false);
    assertFalse(metaDataServiceImpl.hasBackend(backendName));
  }

  @Test
  void getDefaultRepositoryCollection() {
    RepositoryCollection repo = mock(RepositoryCollection.class);
    when(repoCollectionRegistry.getDefaultRepoCollection()).thenReturn(repo);
    assertEquals(repo, metaDataServiceImpl.getDefaultBackend());
  }

  @Test
  void hasBackendTrue() {
    String backendName = "backend";
    when(repoCollectionRegistry.hasRepositoryCollection(backendName)).thenReturn(true);
    assertTrue(metaDataServiceImpl.hasBackend(backendName));
  }

  @Test
  void getPackages() {
    Package package0 = mock(Package.class);
    Package package1 = mock(Package.class);
    when(dataService.findAll(PACKAGE, Package.class)).thenReturn(Stream.of(package0, package1));
    assertEquals(newArrayList(package0, package1), metaDataServiceImpl.getPackages());
  }

  @Test
  void getRootPackages() {
    Package package0 = mock(Package.class);
    Package package1 = mock(Package.class);
    @SuppressWarnings("unchecked")
    Query<Package> packageQ = mock(Query.class);
    when(packageQ.eq(PARENT, null)).thenReturn(packageQ);
    when(packageQ.findAll()).thenReturn(Stream.of(package0, package1));
    when(dataService.query(PACKAGE, Package.class)).thenReturn(packageQ);
    assertEquals(newArrayList(package0, package1), metaDataServiceImpl.getRootPackages());
  }

  @Test
  void getPackageString() {
    Package package_ = mock(Package.class);
    String packageId = "package";
    when(dataService.findOneById(PACKAGE, packageId, Package.class)).thenReturn(package_);
    assertEquals(of(package_), metaDataServiceImpl.getPackage(packageId));
  }

  @Test
  void getPackageStringUnknownPackage() {
    String packageName = "package";
    when(dataService.findOneById(PACKAGE, packageName, Package.class)).thenReturn(null);
    assertEquals(empty(), metaDataServiceImpl.getPackage(packageName));
  }

  @Test
  void addPackage() {
    Package package_ = mock(Package.class);
    metaDataServiceImpl.addPackage(package_);
    verify(dataService).add(PACKAGE, package_);
    verifyNoMoreInteractions(dataService);
  }

  @Test
  void upsertPackages() {
    Package package0 = mock(Package.class);
    Package package1 = mock(Package.class);
    metaDataServiceImpl.upsertPackages(Stream.of(package0, package1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(packagePersister).upsertPackages(captor.capture());
    assertEquals(asList(package0, package1), captor.getValue().collect(toList()));
  }

  @Test
  void hasEntityTypeSystemEntityType() {
    String entityTypeId = "entityTypeId";
    when(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)).thenReturn(true);
    assertTrue(metaDataServiceImpl.hasEntityType(entityTypeId));
  }

  @Test
  void hasEntityTypeNonSystemEntityType() {
    String entityTypeId = "entityTypeId";
    when(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)).thenReturn(false);
    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA), eq(entityTypeId), any(Fetch.class), eq(EntityType.class)))
        .thenReturn(mock(EntityType.class));
    assertTrue(metaDataServiceImpl.hasEntityType(entityTypeId));
  }

  @Test
  void hasEntityTypeUnknownEntityType() {
    String entityTypeId = "entityTypeId";
    when(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)).thenReturn(false);
    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA), eq(entityTypeId), any(Fetch.class), eq(EntityType.class)))
        .thenReturn(null);
    assertFalse(metaDataServiceImpl.hasEntityType(entityTypeId));
  }

  @Test
  void getEntityType() {
    String entityTypeId = "entity";
    EntityType entityType = mock(EntityType.class);

    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA), eq(entityTypeId), any(Fetch.class), eq(EntityType.class)))
        .thenReturn(entityType);

    assertEquals(of(entityType), metaDataServiceImpl.getEntityType(entityTypeId));
    verify(systemEntityTypeRegistry).getSystemEntityType(entityTypeId);
  }

  @Test
  void getEntityTypeNull() {
    assertEquals(empty(), metaDataServiceImpl.getEntityType(null));
  }

  @Test
  void getEntityTypeUnknownEntity() {
    String entityTypeId = "entity";

    when(dataService.findOneById(
            eq(ENTITY_TYPE_META_DATA), eq(entityTypeId), any(), eq(EntityType.class)))
        .thenReturn(null);

    assertEquals(empty(), metaDataServiceImpl.getEntityType(entityTypeId));

    verify(systemEntityTypeRegistry).getSystemEntityType(entityTypeId);
    verify(dataService)
        .findOneById(ENTITY_TYPE_META_DATA, entityTypeId, getEntityTypeFetch(), EntityType.class);
  }

  @Test
  void getSystemEntityTypeFromRegistry() {
    String systemEntityTypeId = "sys_blah_MySystemEntityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);

    when(systemEntityTypeRegistry.getSystemEntityType(systemEntityTypeId))
        .thenReturn(systemEntityType);
    assertEquals(of(systemEntityType), metaDataServiceImpl.getEntityType(systemEntityTypeId));

    verifyZeroInteractions(dataService);
  }

  @Test
  void getSystemEntityTypeBypassingRegistry() {
    String systemEntityTypeId = "sys_blah_MySystemEntityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(dataService.findOneById(
            ENTITY_TYPE_META_DATA, systemEntityTypeId, getEntityTypeFetch(), EntityType.class))
        .thenReturn(systemEntityType);

    assertEquals(
        systemEntityType, metaDataServiceImpl.getEntityTypeBypassingRegistry(systemEntityTypeId));
    verifyZeroInteractions(systemEntityTypeRegistry);
  }

  @Test
  void testGetReferringAttributes() {
    String entityTypeId = "entityTypeId";
    Attribute attr1 = mock(Attribute.class);
    Attribute attr2 = mock(Attribute.class);

    @SuppressWarnings("unchecked")
    Query<Attribute> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(query);
    when(query.eq(REF_ENTITY_TYPE, entityTypeId).findAll()).thenReturn(Stream.of(attr1, attr2));

    assertEquals(
        ImmutableList.of(attr1, attr2),
        metaDataServiceImpl.getReferringAttributes(entityTypeId).collect(toList()));
  }

  @Test
  void addEntityType() {
    EntityType entityType = mock(EntityType.class);
    Attribute attr0 = mock(Attribute.class);
    Attribute attr1 = mock(Attribute.class);
    when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(attr0, attr1));
    metaDataServiceImpl.addEntityType(entityType);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> attrsCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor.capture());
    assertEquals(newArrayList(attr0, attr1), attrsCaptor.getValue().collect(toList()));

    verify(dataService).add(ENTITY_TYPE_META_DATA, entityType);

    verifyNoMoreInteractions(dataService);
  }

  @SuppressWarnings("unchecked")
  @Test
  void addEntityTypesNoMappedByAttrs() {
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

    inOrder.verify(dataService).add(ENTITY_TYPE_META_DATA, entityType1);
    ArgumentCaptor<Stream<Entity>> attrsCaptor1 = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor1.capture());
    assertEquals(
        newArrayList(entity1Attr0, entity1Attr1), attrsCaptor1.getValue().collect(toList()));

    inOrder.verify(dataService).add(ENTITY_TYPE_META_DATA, entityType0);
    ArgumentCaptor<Stream<Entity>> attrsCaptor0 = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0.capture());
    assertEquals(
        newArrayList(entity0Attr0, entity0Attr1), attrsCaptor0.getValue().collect(toList()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void addEntityTypesMappedByAttrs() {
    EntityType entityType0 = mock(EntityType.class);
    when(entityType0.getId()).thenReturn("entityType0");
    when(entityType0.hasMappedByAttributes()).thenReturn(true);

    Attribute entity0Attr0 = mock(Attribute.class);
    when(entity0Attr0.getName()).thenReturn("entity0Attr0");
    when(entity0Attr0.isMappedBy()).thenReturn(true);

    Attribute entity0Attr1 = mock(Attribute.class);
    when(entity0Attr1.getName()).thenReturn("entity0Attr1");
    when(entity0Attr1.getIdentifier()).thenReturn("id01");
    when(entity0Attr1.getTags()).thenReturn(emptyList());

    when(entityType0.getOwnAllAttributes()).thenReturn(newArrayList(entity0Attr0, entity0Attr1));
    when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());

    EntityType entityType1 = mock(EntityType.class);
    when(entityType1.getId()).thenReturn("entityType1");
    when(entityType1.hasMappedByAttributes()).thenReturn(false);
    Attribute entity1Attr0 = mock(Attribute.class);
    Attribute entity1Attr1 = mock(Attribute.class);
    when(entityType1.getOwnAllAttributes()).thenReturn(newArrayList(entity1Attr0, entity1Attr1));

    when(dataService.findOneById(eq(ENTITY_TYPE_META_DATA), any(), eq(EntityType.class)))
        .thenReturn(null);
    when(entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1)))
        .thenReturn(newArrayList(entityType1, entityType0));
    metaDataServiceImpl.upsertEntityTypes(newArrayList(entityType0, entityType1));

    InOrder inOrder = inOrder(dataService);

    inOrder.verify(dataService).add(ENTITY_TYPE_META_DATA, entityType1);
    ArgumentCaptor<Stream<Entity>> attrsCaptor1 = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor1.capture());
    assertEquals(
        newArrayList(entity1Attr0, entity1Attr1), attrsCaptor1.getValue().collect(toList()));

    ArgumentCaptor<EntityType> entityCaptor0 = ArgumentCaptor.forClass(EntityType.class);
    inOrder.verify(dataService).add(eq(ENTITY_TYPE_META_DATA), entityCaptor0.capture());
    assertEquals(
        singletonList(entity0Attr1), newArrayList(entityCaptor0.getValue().getOwnAllAttributes()));

    ArgumentCaptor<Stream<Entity>> attrsCaptor0 = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0.capture());
    assertEquals(singletonList(entity0Attr1), attrsCaptor0.getValue().collect(toList()));

    ArgumentCaptor<EntityType> entityCaptor0b = ArgumentCaptor.forClass(EntityType.class);
    inOrder.verify(dataService).update(eq(ENTITY_TYPE_META_DATA), entityCaptor0b.capture());
    assertEquals(entityType0, entityCaptor0b.getValue());

    ArgumentCaptor<Stream<Entity>> attrsCaptor0b = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrsCaptor0b.capture());
    assertEquals(singletonList(entity0Attr0), attrsCaptor0b.getValue().collect(toList()));
  }

  @Test
  void deleteEntityType() {
    String entityTypeId = "entity";
    metaDataServiceImpl.deleteEntityType(entityTypeId);
    verify(dataService).deleteById(ENTITY_TYPE_META_DATA, entityTypeId);
    verifyNoMoreInteractions(dataService);
  }

  @Test
  void deleteEntityTypeCollection() {
    EntityType entityType0 = mock(EntityType.class);
    String entityTypeId0 = "entity0";
    when(entityType0.getId()).thenReturn(entityTypeId0);

    EntityType entityType1 = mock(EntityType.class);
    String entityTypeId1 = "entity1";
    when(entityType1.getId()).thenReturn(entityTypeId1);

    metaDataServiceImpl.deleteEntityType(newArrayList(entityType0, entityType1));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<EntityType>> entityIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).delete(eq(ENTITY_TYPE_META_DATA), entityIdCaptor.capture());
    assertEquals(
        newArrayList(entityType0, entityType1), entityIdCaptor.getValue().collect(toList()));

    verifyNoMoreInteractions(dataService);
  }

  @Test
  void deleteEntityTypeCollectionEmpty() {
    metaDataServiceImpl.deleteEntityType(emptyList());
    verifyNoMoreInteractions(dataService);
  }

  @Test
  void updateEntityType() {
    String entityTypeId = "entity";
    String attrShared0Name = "attrSame";
    String attrShared1Name = "attrUpdated";
    String attrAddedName = "attrAdded";
    String attrDeletedName = "attrDeleted";
    Attribute attrShared0 =
        when(mock(Attribute.class).getName()).thenReturn(attrShared0Name).getMock();
    when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
    when(attrShared0.getTags()).thenReturn(emptyList());
    Attribute attrShared1 =
        when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
    when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
    Attribute attrShared1Updated =
        when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
    Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
    Attribute attrDeleted =
        when(mock(Attribute.class).getName()).thenReturn(attrDeletedName).getMock();

    EntityType existingEntityType =
        when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(existingEntityType.getId()).thenReturn(entityTypeId);
    when(existingEntityType.getLabel()).thenReturn("label");
    when(existingEntityType.getOwnAllAttributes())
        .thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));

    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(entityType.getId()).thenReturn(entityTypeId);
    when(entityType.getLabel()).thenReturn("new label");
    when(entityType.getOwnAllAttributes())
        .thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));

    @SuppressWarnings("unchecked")
    Query<EntityType> entityQ = mock(Query.class);
    when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
    when(entityQ.eq(ID, entityTypeId)).thenReturn(entityQ);
    when(entityQ.fetch(any())).thenReturn(entityQ);
    when(entityQ.findOne()).thenReturn(existingEntityType);

    metaDataServiceImpl.updateEntityType(entityType);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
    assertEquals(singletonList(attrAdded), attrAddCaptor.getValue().collect(toList()));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
    assertEquals(singletonList(attrShared1Updated), attrUpdateCaptor.getValue().collect(toList()));

    verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);
  }

  @Test
  void updateEntityTypeEntityDoesNotExist() {
    String entityTypeId = "entity";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(entityType.getId()).thenReturn(entityTypeId);
    @SuppressWarnings("unchecked")
    Query<EntityType> entityQ = mock(Query.class);

    when(entityQ.eq(ID, entityTypeId)).thenReturn(entityQ);
    when(entityQ.fetch(any())).thenReturn(entityQ);
    when(entityQ.findOne()).thenReturn(null);
    when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
    assertThrows(
        UnknownEntityTypeException.class, () -> metaDataServiceImpl.updateEntityType(entityType));
  }

  @Test
  void updateEntityTypeCollectionEmpty() {
    metaDataServiceImpl.upsertEntityTypes(emptyList());
    verifyNoMoreInteractions(dataService);
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateEntityTypeCollection() {
    String entityTypeId = "entity";
    String attrShared0Name = "attrSame";
    String attrShared1Name = "attrUpdated";
    String attrAddedName = "attrAdded";
    String attrDeletedName = "attrDeleted";
    Attribute attrShared0 =
        when(mock(Attribute.class).getName()).thenReturn(attrShared0Name).getMock();
    when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
    when(attrShared0.getTags()).thenReturn(emptyList());
    Attribute attrShared1 =
        when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
    when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
    Attribute attrShared1Updated =
        when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
    Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
    Attribute attrDeleted =
        when(mock(Attribute.class).getName()).thenReturn(attrDeletedName).getMock();

    EntityType existingEntityType = mock(EntityType.class);
    when(existingEntityType.getLabel()).thenReturn("label");
    when(existingEntityType.getId()).thenReturn(entityTypeId);
    when(existingEntityType.getOwnAllAttributes())
        .thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
    //noinspection AnonymousInnerClassMayBeStatic

    when(existingEntityType.getOwnMappedByAttributes()).thenAnswer(invocation -> Stream.empty());
    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("new label");
    when(entityType.getId()).thenReturn(entityTypeId);
    when(entityType.getOwnAllAttributes())
        .thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
    //noinspection AnonymousInnerClassMayBeStatic

    when(entityType.getOwnMappedByAttributes()).thenAnswer(invocation -> Stream.empty());

    when(entityTypeDependencyResolver.resolve(singletonList(entityType)))
        .thenReturn(singletonList(entityType));

    when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class))
        .thenReturn(existingEntityType);

    metaDataServiceImpl.upsertEntityTypes(singletonList(entityType));

    ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
    assertEquals(singletonList(attrAdded), attrAddCaptor.getValue().collect(toList()));

    ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
    assertEquals(singletonList(attrShared1Updated), attrUpdateCaptor.getValue().collect(toList()));

    verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateEntityTypeCollectionMappedByExisting() {
    String entityTypeId = "entity";

    String attrShared0Name = "attrSame";
    String attrShared1Name = "attrUpdated";
    String attrAddedName = "attrAdded";
    String attrDeletedName = "attrDeleted";
    Attribute attrShared0 =
        when(mock(Attribute.class).getName()).thenReturn(attrShared0Name).getMock();
    when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
    when(attrShared0.getTags()).thenReturn(emptyList());
    Attribute attrShared1 =
        when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
    when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
    Attribute attrShared1Updated =
        when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
    Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
    Attribute attrDeleted =
        when(mock(Attribute.class).getName()).thenReturn(attrDeletedName).getMock();

    EntityType existingEntityType = mock(EntityType.class);
    when(existingEntityType.getId()).thenReturn(entityTypeId);
    when(existingEntityType.getLabel()).thenReturn("label");
    when(existingEntityType.getOwnAllAttributes())
        .thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
    //noinspection AnonymousInnerClassMayBeStatic

    when(existingEntityType.getOwnMappedByAttributes())
        .thenAnswer(invocation -> Stream.of(attrShared1));

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(entityTypeId);
    when(entityType.getLabel()).thenReturn("new label");
    when(entityType.getOwnAllAttributes())
        .thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
    //noinspection AnonymousInnerClassMayBeStatic

    when(entityType.getOwnMappedByAttributes())
        .thenAnswer(invocation -> Stream.of(attrShared1Updated));

    when(entityTypeDependencyResolver.resolve(singletonList(entityType)))
        .thenReturn(singletonList(entityType));
    when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class))
        .thenReturn(existingEntityType);

    metaDataServiceImpl.upsertEntityTypes(singletonList(entityType));

    InOrder inOrder = inOrder(dataService);
    inOrder.verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);

    ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
    assertEquals(singletonList(attrAdded), attrAddCaptor.getValue().collect(toList()));

    ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
    assertEquals(singletonList(attrShared1Updated), attrUpdateCaptor.getValue().collect(toList()));

    ArgumentCaptor<Stream<Entity>> attrDeletedCaptor = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrDeletedCaptor.capture());
    assertEquals(singletonList(attrDeleted), attrDeletedCaptor.getValue().collect(toList()));

    inOrder.verifyNoMoreInteractions();
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateEntityTypeCollectionMappedByNew() {
    String entityTypeId = "entity";
    String attrShared0Name = "attrSame";
    String attrShared1Name = "attrUpdated";
    String attrAddedName = "attrAdded";
    String attrDeletedName = "attrDeleted";
    Attribute attrShared0 =
        when(mock(Attribute.class).getName()).thenReturn(attrShared0Name).getMock();
    when(attrShared0.getIdentifier()).thenReturn(attrShared0Name);
    when(attrShared0.getTags()).thenReturn(emptyList());
    Attribute attrShared1 =
        when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
    when(attrShared1.getIdentifier()).thenReturn(attrShared1Name);
    Attribute attrShared1Updated =
        when(mock(Attribute.class).getName()).thenReturn(attrShared1Name).getMock();
    when(attrShared1Updated.isMappedBy()).thenReturn(false);
    Attribute attrAdded = when(mock(Attribute.class).getName()).thenReturn(attrAddedName).getMock();
    Attribute attrDeleted =
        when(mock(Attribute.class).getName()).thenReturn(attrDeletedName).getMock();

    EntityType existingEntityType = mock(EntityType.class);
    when(existingEntityType.getId()).thenReturn(entityTypeId);
    when(existingEntityType.getLabel()).thenReturn("label");
    when(existingEntityType.getOwnAllAttributes())
        .thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
    //noinspection AnonymousInnerClassMayBeStatic

    when(existingEntityType.getOwnMappedByAttributes())
        .thenAnswer(invocation -> Stream.of(attrShared1));

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(entityTypeId);
    when(entityType.getLabel()).thenReturn("new label");
    when(entityType.getOwnAllAttributes())
        .thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
    //noinspection AnonymousInnerClassMayBeStatic

    when(entityType.getOwnMappedByAttributes()).thenAnswer(invocation -> Stream.empty());

    when(entityTypeDependencyResolver.resolve(singletonList(entityType)))
        .thenReturn(singletonList(entityType));
    when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class))
        .thenReturn(existingEntityType);

    metaDataServiceImpl.upsertEntityTypes(singletonList(entityType));

    InOrder inOrder = inOrder(dataService);
    inOrder.verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);

    ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
    assertEquals(singletonList(attrAdded), attrAddCaptor.getValue().collect(toList()));

    ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
    assertEquals(singletonList(attrShared1Updated), attrUpdateCaptor.getValue().collect(toList()));

    ArgumentCaptor<Stream<Entity>> attrDeletedCaptor = ArgumentCaptor.forClass(Stream.class);
    inOrder.verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrDeletedCaptor.capture());
    assertEquals(singletonList(attrDeleted), attrDeletedCaptor.getValue().collect(toList()));

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void addAttribute() {
    Attribute attr = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    EntityType currentEntityType = mock(EntityType.class);
    when(attr.getEntity()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("EntityTypeName");
    when(dataService.getEntityType("EntityTypeName")).thenReturn(currentEntityType);

    metaDataServiceImpl.addAttribute(attr);
    verify(dataService).update(ENTITY_TYPE_META_DATA, currentEntityType);
    verify(dataService).add(ATTRIBUTE_META_DATA, attr);
    verify(currentEntityType).addAttribute(attr);
  }

  @Test
  void deleteAttributeById() {
    Object attrId = "attr0";
    Attribute attribute = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    when(dataService.findOneById(ATTRIBUTE_META_DATA, attrId, Attribute.class))
        .thenReturn(attribute);
    when(attribute.getEntity()).thenReturn(entityType);

    metaDataServiceImpl.deleteAttributeById(attrId);
    verify(dataService).update(ENTITY_TYPE_META_DATA, entityType);
    verify(dataService).delete(ATTRIBUTE_META_DATA, attribute);
    verify(entityType).removeAttribute(attribute);
  }

  static Iterator<Object[]> isMetaEntityTypeProvider() {
    return newArrayList(
            new Object[] {ENTITY_TYPE_META_DATA, true},
            new Object[] {ATTRIBUTE_META_DATA, true},
            new Object[] {TAG, true},
            new Object[] {PACKAGE, true},
            new Object[] {"noMeta", false})
        .iterator();
  }

  @ParameterizedTest
  @MethodSource("isMetaEntityTypeProvider")
  void isMetaEntityType(String entityTypeId, boolean isMeta) {
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    assertEquals(isMeta, MetaDataService.isMetaEntityType(entityType));
  }

  @Test
  void upsertEntityTypeCollectionEmpty() {
    metaDataServiceImpl.upsertEntityTypes(emptyList());
    verifyZeroInteractions(dataService);
  }
}
