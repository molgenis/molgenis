package org.molgenis.navigator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.navigator.copy.job.CopyJobExecutionFactory;
import org.molgenis.navigator.download.job.DownloadJobExecution;
import org.molgenis.navigator.download.job.DownloadJobExecutionFactory;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {NavigatorServiceImplTest.Config.class})
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class NavigatorServiceImplTest extends AbstractMockitoTestNGSpringContextTests {
  @Mock private DataService dataService;
  @Mock private JobExecutor jobExecutor;
  @Mock private DownloadJobExecutionFactory downloadJobExecutionFactory;
  @Mock private CopyJobExecutionFactory copyJobExecutionFactory;
  private NavigatorServiceImpl navigatorServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    navigatorServiceImpl =
        new NavigatorServiceImpl(
            dataService, jobExecutor, downloadJobExecutionFactory, copyJobExecutionFactory);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNavigatorServiceImpl() {
    new NavigatorServiceImpl(null, null, null, null);
  }

  @Test
  public void testGetFolder() {
    String folderId = "myFolderId";

    Package packageParent = mock(Package.class);
    when(packageParent.getId()).thenReturn("parentId");
    when(packageParent.getLabel()).thenReturn("parentLabel");

    Package aPackage = mock(Package.class);
    when(aPackage.getId()).thenReturn(folderId);
    when(aPackage.getLabel()).thenReturn("packageLabel");
    when(aPackage.getParent()).thenReturn(packageParent);

    when(dataService.findOneById(PACKAGE, folderId, Package.class)).thenReturn(aPackage);
    assertEquals(
        navigatorServiceImpl.getFolder(folderId),
        Folder.create(folderId, "packageLabel", Folder.create("parentId", "parentLabel", null)));
  }

  @Test
  public void testGetFolderRootFolder() {
    assertNull(navigatorServiceImpl.getFolder(null));
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testGetFolderUnknown() {
    navigatorServiceImpl.getFolder("unknownFolderId");
  }

  @Test
  public void testGetResources() {
    String folderId = "myFolderId";

    Package package0 = mock(Package.class);
    when(package0.getId()).thenReturn("p0");
    when(package0.getLabel()).thenReturn("package0");
    Stream<Package> packages = Stream.of(package0);

    @SuppressWarnings("unchecked")
    Query<Package> packageQuery = mock(Query.class);
    when(packageQuery.eq(PackageMetadata.PARENT, folderId)).thenReturn(packageQuery);
    when(packageQuery.findAll()).thenReturn(packages);
    doReturn(packageQuery).when(dataService).query(PACKAGE, Package.class);

    EntityType entityType0 = mock(EntityType.class);
    when(entityType0.getId()).thenReturn("e0");
    when(entityType0.getLabel()).thenReturn("entityType0");
    Stream<EntityType> entityTypes = Stream.of(entityType0);

    @SuppressWarnings("unchecked")
    Query<EntityType> entityTypeQuery = mock(Query.class);
    when(entityTypeQuery.eq(EntityTypeMetadata.PACKAGE, folderId)).thenReturn(entityTypeQuery);
    when(entityTypeQuery.findAll()).thenReturn(entityTypes);
    doReturn(entityTypeQuery).when(dataService).query(ENTITY_TYPE_META_DATA, EntityType.class);

    List<Resource> expectedResources =
        asList(
            Resource.builder()
                .setType(ResourceType.PACKAGE)
                .setId("p0")
                .setLabel("package0")
                .build(),
            Resource.builder()
                .setType(ResourceType.ENTITY_TYPE)
                .setId("e0")
                .setLabel("entityType0")
                .build());
    assertEquals(navigatorServiceImpl.getResources(folderId), expectedResources);
  }

  @Test
  public void testMoveResources() {
    String targetFolderId = "targetFolderId";
    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    Package targetPackage = mock(Package.class);
    when(dataService.findOneById(PackageMetadata.PACKAGE, targetFolderId, Package.class))
        .thenReturn(targetPackage);

    Package package0 = mock(Package.class);
    Stream<Package> packages = Stream.of(package0);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdsCaptor = ArgumentCaptor.forClass(Stream.class);
    doReturn(packages)
        .when(dataService)
        .findAll(eq(PackageMetadata.PACKAGE), packageIdsCaptor.capture(), eq(Package.class));

    EntityType entityType0 = mock(EntityType.class);
    Stream<EntityType> entityTypes = Stream.of(entityType0);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityTypeIdsCaptor = ArgumentCaptor.forClass(Stream.class);
    doReturn(entityTypes)
        .when(dataService)
        .findAll(
            eq(EntityTypeMetadata.ENTITY_TYPE_META_DATA),
            entityTypeIdsCaptor.capture(),
            eq(EntityType.class));

    navigatorServiceImpl.moveResources(resources, targetFolderId);

    assertEquals(packageIdsCaptor.getValue().collect(toList()), singletonList("p0"));
    verify(package0).setParent(targetPackage);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> updatedPackagesCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).update(eq(PackageMetadata.PACKAGE), updatedPackagesCaptor.capture());
    assertEquals(updatedPackagesCaptor.getValue().collect(toList()), singletonList(package0));

    assertEquals(entityTypeIdsCaptor.getValue().collect(toList()), singletonList("e0"));
    verify(entityType0).setPackage(targetPackage);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<EntityType>> updatedEntityTypesCaptor =
        ArgumentCaptor.forClass(Stream.class);
    verify(dataService)
        .update(eq(EntityTypeMetadata.ENTITY_TYPE_META_DATA), updatedEntityTypesCaptor.capture());
    assertEquals(updatedEntityTypesCaptor.getValue().collect(toList()), singletonList(entityType0));
  }

  @Test
  public void testMoveResourcesRootPackage() {
    List<ResourceIdentifier> resources =
        singletonList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build());

    Package parentPackage = mock(Package.class);
    Package package0 = mock(Package.class);
    when(package0.getParent()).thenReturn(parentPackage);
    Stream<Package> packages = Stream.of(package0);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdsCaptor = ArgumentCaptor.forClass(Stream.class);
    doReturn(packages)
        .when(dataService)
        .findAll(eq(PackageMetadata.PACKAGE), packageIdsCaptor.capture(), eq(Package.class));
    navigatorServiceImpl.moveResources(resources, null);
    assertEquals(packageIdsCaptor.getValue().collect(toList()), singletonList("p0"));
    verify(package0).setParent(null);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> updatedPackagesCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).update(eq(PackageMetadata.PACKAGE), updatedPackagesCaptor.capture());
    assertEquals(updatedPackagesCaptor.getValue().collect(toList()), singletonList(package0));
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testMoveResourcesUnknownTargetPackage() {
    String targetFolderId = "unknownTargetFolderId";
    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    navigatorServiceImpl.moveResources(resources, targetFolderId);
  }

  @Test
  public void testMoveResourcesNoResources() {
    navigatorServiceImpl.moveResources(emptyList(), "targetFolderId");
    verifyZeroInteractions(dataService);
  }

  @Test
  public void testMoveResourcesSourcePackageIsTargetPackage() {
    String targetFolderId = "targetFolderId";
    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    Package targetPackage = when(mock(Package.class).getId()).thenReturn(targetFolderId).getMock();
    when(dataService.findOneById(PackageMetadata.PACKAGE, targetFolderId, Package.class))
        .thenReturn(targetPackage);

    Package package0 = mock(Package.class);
    when(package0.getParent()).thenReturn(targetPackage);
    Stream<Package> packages = Stream.of(package0);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdsCaptor = ArgumentCaptor.forClass(Stream.class);
    doReturn(packages)
        .when(dataService)
        .findAll(eq(PackageMetadata.PACKAGE), packageIdsCaptor.capture(), eq(Package.class));

    EntityType entityType0 = mock(EntityType.class);
    when(entityType0.getPackage()).thenReturn(targetPackage);
    Stream<EntityType> entityTypes = Stream.of(entityType0);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityTypeIdsCaptor = ArgumentCaptor.forClass(Stream.class);
    doReturn(entityTypes)
        .when(dataService)
        .findAll(
            eq(EntityTypeMetadata.ENTITY_TYPE_META_DATA),
            entityTypeIdsCaptor.capture(),
            eq(EntityType.class));

    navigatorServiceImpl.moveResources(resources, targetFolderId);

    verifyNoMoreInteractions(dataService);
  }

  @Test
  public void testCopyResources() {
    throw new RuntimeException("TODO implement");
  }

  @Test
  public void testCopyResourcesRootPackage() {
    throw new RuntimeException("TODO implement");
  }

  @WithMockUser
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCopyResourcesNoResources() {
    navigatorServiceImpl.copyResources(emptyList(), "targetFolderId");
  }

  @WithMockUser
  @Test
  public void testDownloadResources() {
    DownloadJobExecution downloadJobExecution = mock(DownloadJobExecution.class);
    when(downloadJobExecutionFactory.create()).thenReturn(downloadJobExecution);

    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    assertEquals(navigatorServiceImpl.downloadResources(resources), downloadJobExecution);
    verify(downloadJobExecution)
        .setResources(
            "[{\"type\":\"PACKAGE\",\"id\":\"p0\"},{\"type\":\"ENTITY_TYPE\",\"id\":\"e0\"}]");
    verify(downloadJobExecution).setUser("user");
    verify(jobExecutor).submit(downloadJobExecution);
  }

  @WithMockUser
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDownloadResourcesNoResources() {
    navigatorServiceImpl.downloadResources(emptyList());
  }

  @Test
  public void testUpdateResourcePackage() {
    String packageId = "myPackageId";

    Package aPackage = mock(Package.class);
    when(aPackage.getLabel()).thenReturn("label");
    when(aPackage.getDescription()).thenReturn("description");
    when(dataService.findOneById(PackageMetadata.PACKAGE, packageId, Package.class))
        .thenReturn(aPackage);
    Resource resource =
        Resource.builder()
            .setType(ResourceType.PACKAGE)
            .setId(packageId)
            .setLabel("label")
            .setDescription("updated description")
            .build();
    navigatorServiceImpl.updateResource(resource);

    verify(aPackage).setDescription("updated description");
    verify(dataService).update(PackageMetadata.PACKAGE, aPackage);
  }

  @Test
  public void testUpdateResourcePackageUnchanged() {
    String packageId = "myPackageId";

    Package aPackage = mock(Package.class);
    when(aPackage.getLabel()).thenReturn("label");
    when(aPackage.getDescription()).thenReturn("description");
    when(dataService.findOneById(PackageMetadata.PACKAGE, packageId, Package.class))
        .thenReturn(aPackage);
    Resource resource =
        Resource.builder()
            .setType(ResourceType.PACKAGE)
            .setId(packageId)
            .setLabel("label")
            .setDescription("description")
            .build();
    navigatorServiceImpl.updateResource(resource);

    verifyNoMoreInteractions(dataService);
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testUpdateResourcePackageUnknown() {
    String packageId = "myPackageId";
    Resource resource =
        Resource.builder()
            .setType(ResourceType.PACKAGE)
            .setId(packageId)
            .setLabel("label")
            .setDescription("description")
            .build();
    navigatorServiceImpl.updateResource(resource);
  }

  @Test
  public void testUpdateResourceEntityType() {
    String entityTypeId = "myEntityTypeId";

    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("label");
    when(entityType.getDescription()).thenReturn("description");
    when(dataService.findOneById(
            EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class))
        .thenReturn(entityType);
    Resource resource =
        Resource.builder()
            .setType(ResourceType.ENTITY_TYPE)
            .setId(entityTypeId)
            .setLabel("label")
            .setDescription("updated description")
            .build();
    navigatorServiceImpl.updateResource(resource);

    verify(entityType).setDescription("updated description");
    verify(dataService).update(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityType);
  }

  @Test
  public void testUpdateResourceEntityTypeUnchanged() {
    String entityTypeId = "myEntityTypeId";

    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("label");
    when(entityType.getDescription()).thenReturn("description");
    when(dataService.findOneById(
            EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class))
        .thenReturn(entityType);
    Resource resource =
        Resource.builder()
            .setType(ResourceType.ENTITY_TYPE)
            .setId(entityTypeId)
            .setLabel("label")
            .setDescription("description")
            .build();
    navigatorServiceImpl.updateResource(resource);

    verifyNoMoreInteractions(dataService);
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testUpdateResourceEntityTypeUnknown() {
    String entityTypeId = "myEntityTypeId";
    Resource resource =
        Resource.builder()
            .setType(ResourceType.ENTITY_TYPE)
            .setId(entityTypeId)
            .setLabel("label")
            .setDescription("description")
            .build();
    navigatorServiceImpl.updateResource(resource);
  }

  @Test
  public void testFindResources() {
    String query = "text";

    Package package0 = mock(Package.class);
    when(package0.getId()).thenReturn("p0");
    when(package0.getLabel()).thenReturn("package0");
    Stream<Package> packages = Stream.of(package0);

    @SuppressWarnings("unchecked")
    Query<Package> packageQuery = mock(Query.class);
    doReturn(packageQuery).when(packageQuery).search(PackageMetadata.LABEL, query);
    doReturn(packageQuery).when(packageQuery).or();
    doReturn(packageQuery).when(packageQuery).search(PackageMetadata.DESCRIPTION, query);
    when(packageQuery.findAll()).thenReturn(packages);
    doReturn(packageQuery).when(dataService).query(PACKAGE, Package.class);

    EntityType entityType0 = mock(EntityType.class);
    when(entityType0.getId()).thenReturn("e0");
    when(entityType0.getLabel()).thenReturn("entityType0");
    Stream<EntityType> entityTypes = Stream.of(entityType0);

    @SuppressWarnings("unchecked")
    Query<EntityType> entityTypeQuery = mock(Query.class);
    doReturn(entityTypeQuery).when(entityTypeQuery).search(EntityTypeMetadata.LABEL, query);
    doReturn(entityTypeQuery).when(entityTypeQuery).or();
    doReturn(entityTypeQuery).when(entityTypeQuery).search(EntityTypeMetadata.DESCRIPTION, query);
    when(entityTypeQuery.findAll()).thenReturn(entityTypes);
    doReturn(entityTypeQuery).when(dataService).query(ENTITY_TYPE_META_DATA, EntityType.class);

    List<Resource> expectedResources =
        asList(
            Resource.builder()
                .setType(ResourceType.PACKAGE)
                .setId("p0")
                .setLabel("package0")
                .build(),
            Resource.builder()
                .setType(ResourceType.ENTITY_TYPE)
                .setId("e0")
                .setLabel("entityType0")
                .build());
    assertEquals(navigatorServiceImpl.findResources(query), expectedResources);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteResourcesPackagesAndEntityTypes() {
    EntityType entityTypeA0 = when(mock(EntityType.class).getId()).thenReturn("eA0").getMock();
    EntityType entityTypeA1 = when(mock(EntityType.class).getId()).thenReturn("eA1").getMock();
    EntityType entityTypeB0 = when(mock(EntityType.class).getId()).thenReturn("eB0").getMock();
    EntityType entityTypeB1 = when(mock(EntityType.class).getId()).thenReturn("eB1").getMock();
    EntityType entityTypeC0 = when(mock(EntityType.class).getId()).thenReturn("eC0").getMock();

    Package packageA = when(mock(Package.class).getId()).thenReturn("pA").getMock();
    Package packageB = when(mock(Package.class).getId()).thenReturn("pB").getMock();
    Package packageC = when(mock(Package.class).getId()).thenReturn("pC").getMock();
    Package packageD = when(mock(Package.class).getId()).thenReturn("pD").getMock();
    when(packageA.getEntityTypes()).thenReturn(asList(entityTypeA0, entityTypeA1));
    when(packageB.getEntityTypes()).thenReturn(asList(entityTypeB0, entityTypeB1));
    when(packageC.getEntityTypes()).thenReturn(singletonList(entityTypeC0));
    when(packageA.getChildren()).thenReturn(asList(packageC, packageD));

    doReturn(Stream.of(packageA, packageB))
        .when(dataService)
        .findAll(eq(PACKAGE), any(Stream.class), eq(Package.class));

    navigatorServiceImpl.deleteResources(
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("pA").build(),
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("pB").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e1").build()));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityTypeIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(ENTITY_TYPE_META_DATA), entityTypeIdCaptor.capture());
    assertEquals(
        entityTypeIdCaptor.getValue().collect(toSet()),
        new HashSet<>(asList("e0", "e1", "eA0", "eA1", "eB0", "eB1", "eC0")));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(PACKAGE), packageIdCaptor.capture());
    assertEquals(
        packageIdCaptor.getValue().collect(toSet()), new HashSet<>(asList("pA", "pB", "pC", "pD")));

    verifyNoMoreInteractions(dataService);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteResourcesPackages() {
    EntityType entityTypeA0 = when(mock(EntityType.class).getId()).thenReturn("eA0").getMock();
    EntityType entityTypeA1 = when(mock(EntityType.class).getId()).thenReturn("eA1").getMock();

    Package packageA = when(mock(Package.class).getId()).thenReturn("pA").getMock();
    when(packageA.getEntityTypes()).thenReturn(asList(entityTypeA0, entityTypeA1));

    doReturn(Stream.of(packageA))
        .when(dataService)
        .findAll(eq(PACKAGE), any(Stream.class), eq(Package.class));

    navigatorServiceImpl.deleteResources(
        singletonList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("pA").build()));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityTypeIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(ENTITY_TYPE_META_DATA), entityTypeIdCaptor.capture());
    assertEquals(
        entityTypeIdCaptor.getValue().collect(toSet()), new HashSet<>(asList("eA0", "eA1")));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(PACKAGE), packageIdCaptor.capture());
    assertEquals(packageIdCaptor.getValue().collect(toSet()), new HashSet<>(singletonList("pA")));

    verifyNoMoreInteractions(dataService);
  }

  @Test
  public void testDeleteResourcesEntityTypes() {
    navigatorServiceImpl.deleteResources(
        asList(
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e1").build()));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityTypeIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(ENTITY_TYPE_META_DATA), entityTypeIdCaptor.capture());
    assertEquals(entityTypeIdCaptor.getValue().collect(toSet()), new HashSet<>(asList("e0", "e1")));

    verifyNoMoreInteractions(dataService);
  }

  @Test
  public void testDeleteResourcesNothing() {
    navigatorServiceImpl.deleteResources(emptyList());
    verifyZeroInteractions(dataService);
  }

  static class Config {}
}
