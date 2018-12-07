package org.molgenis.navigator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
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
import org.molgenis.navigator.copy.job.ResourceCopyJobExecution;
import org.molgenis.navigator.copy.job.ResourceCopyJobExecutionFactory;
import org.molgenis.navigator.delete.job.ResourceDeleteJobExecution;
import org.molgenis.navigator.delete.job.ResourceDeleteJobExecutionFactory;
import org.molgenis.navigator.download.job.ResourceDownloadJobExecution;
import org.molgenis.navigator.download.job.ResourceDownloadJobExecutionFactory;
import org.molgenis.navigator.model.Resource;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
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
  @Mock private ResourceDownloadJobExecutionFactory downloadJobExecutionFactory;
  @Mock private ResourceCopyJobExecutionFactory copyJobExecutionFactory;
  @Mock private ResourceDeleteJobExecutionFactory deleteJobExecutionFactory;
  private NavigatorServiceImpl navigatorServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    navigatorServiceImpl =
        new NavigatorServiceImpl(
            dataService,
            jobExecutor,
            downloadJobExecutionFactory,
            copyJobExecutionFactory,
            deleteJobExecutionFactory);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNavigatorServiceImpl() {
    new NavigatorServiceImpl(null, null, null, null, null);
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
  public void testMoveResourcesSourcePackageIsTargetPackageIsRootPackage() {
    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

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

    navigatorServiceImpl.moveResources(resources, null);

    verifyNoMoreInteractions(dataService);
  }

  @WithMockUser
  @Test
  public void testCopyResources() {
    ResourceCopyJobExecution copyJobExecution = mock(ResourceCopyJobExecution.class);
    when(copyJobExecutionFactory.create()).thenReturn(copyJobExecution);

    String targetFolderId = "targetFolderId";
    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    Package targetPackage = when(mock(Package.class).getId()).thenReturn(targetFolderId).getMock();
    when(dataService.findOneById(PackageMetadata.PACKAGE, targetFolderId, Package.class))
        .thenReturn(targetPackage);

    assertEquals(navigatorServiceImpl.copyResources(resources, targetFolderId), copyJobExecution);
    verify(copyJobExecution).setResources(resources);
    verify(copyJobExecution).setTargetPackage(targetFolderId);
    verify(jobExecutor).submit(copyJobExecution);
  }

  @WithMockUser
  @Test
  public void testCopyResourcesRootPackage() {
    ResourceCopyJobExecution copyJobExecution = mock(ResourceCopyJobExecution.class);
    when(copyJobExecutionFactory.create()).thenReturn(copyJobExecution);

    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    assertEquals(navigatorServiceImpl.copyResources(resources, null), copyJobExecution);
    verify(copyJobExecution).setResources(resources);
    verify(copyJobExecution).setTargetPackage(null);
    verify(jobExecutor).submit(copyJobExecution);
  }

  @WithMockUser
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCopyResourcesNoResources() {
    navigatorServiceImpl.copyResources(emptyList(), "targetFolderId");
  }

  @WithMockUser
  @Test(expectedExceptions = UnknownEntityException.class)
  public void testCopyResourcesUnknownTargetFolder() {
    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    navigatorServiceImpl.copyResources(resources, "unknownTargetFolderId");
  }

  @WithMockUser
  @Test
  public void testDownloadResources() {
    ResourceDownloadJobExecution downloadJobExecution = mock(ResourceDownloadJobExecution.class);
    when(downloadJobExecutionFactory.create()).thenReturn(downloadJobExecution);

    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    assertEquals(navigatorServiceImpl.downloadResources(resources), downloadJobExecution);
    verify(downloadJobExecution).setResources(resources);
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

  @WithMockUser
  @Test
  public void testDeleteResources() {
    ResourceDeleteJobExecution deleteJobExecution = mock(ResourceDeleteJobExecution.class);
    when(deleteJobExecutionFactory.create()).thenReturn(deleteJobExecution);

    List<ResourceIdentifier> resources =
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build());

    assertEquals(navigatorServiceImpl.deleteResources(resources), deleteJobExecution);
    verify(deleteJobExecution).setResources(resources);
    verify(jobExecutor).submit(deleteJobExecution);
  }

  static class Config {}
}
