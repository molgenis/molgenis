package org.molgenis.integrationtest.platform;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.integrationtest.platform.PlatformIT.waitForWorkToBeFinished;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.integrationtest.utils.TestProgress;
import org.molgenis.navigator.copy.service.CopyService;
import org.molgenis.navigator.copy.service.CopyServiceImpl;
import org.molgenis.navigator.copy.service.EntityTypeCopier;
import org.molgenis.navigator.copy.service.EntityTypeMetadataCopier;
import org.molgenis.navigator.copy.service.PackageCopier;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.navigator.util.ResourceCollector;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.SidUtils;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      PlatformITConfig.class,
      CopyServiceImpl.class,
      ResourceCollector.class,
      PackageCopier.class,
      EntityTypeCopier.class,
      EntityTypeMetadataCopier.class
    })
class CopyServiceIT extends AbstractMockitoSpringContextTests {

  private static final Logger LOG = LoggerFactory.getLogger(CopyServiceIT.class);

  private static final String USERNAME = "copy-user";
  private static final String PACKAGE_B = "packageB";
  private static final String PACKAGE_A = "packageA";
  private static final String ENTITY_TYPE_B = "EntityTypeB";
  private static final String ENTITY_TYPE_A = "EntityTypeA";

  @Autowired private DataService dataService;
  @Autowired private CopyService copyService;
  @Autowired private PackageFactory packageFactory;
  @Autowired private EntityTestHarness testHarness;
  @Autowired private MetaDataService metadataService;
  @Autowired private IndexJobScheduler indexService;
  @Autowired private PermissionService testPermissionService;

  private EntityType entityTypeB;
  private EntityType entityTypeA;
  private Package packageA;
  private Package packageB;

  @BeforeEach
  void setUp() {
    runAsSystem(
        () -> {
          addPackages();
          addTestEntityTypes();
          populatePermissions();
        });
  }

  @WithMockUser(username = USERNAME)
  @SuppressWarnings({"OptionalGetWithoutIsPresent"})
  @Test
  void testCopyPackage() {
    String targetPackageId = "target1";
    addTargetPackage(targetPackageId);

    ResourceIdentifier id = ResourceIdentifier.create(ResourceType.PACKAGE, PACKAGE_A);
    TestProgress progress = new TestProgress();

    copyService.copy(singletonList(id), targetPackageId, progress);
    await().atMost(5, TimeUnit.SECONDS).until(copyJobFinished(progress));

    LOG.info("Copy job progress: {}/{}", progress.getProgress(), progress.getProgressMax());
    waitForWorkToBeFinished(indexService, LOG);

    Package targetPackage = metadataService.getPackage(targetPackageId).get();
    List<Package> packages = newArrayList(targetPackage.getChildren());
    assertEquals(1, packages.size());

    Package packageACopy = packages.get(0);
    assertEquals("Package A", packageACopy.getLabel());
    List<EntityType> entityTypesInACopy = newArrayList(packageACopy.getEntityTypes());
    List<Package> packagesInACopy = newArrayList(packageACopy.getChildren());
    assertEquals(1, entityTypesInACopy.size());
    assertEquals(1, packagesInACopy.size());

    Package packageBCopy = packagesInACopy.get(0);
    assertEquals("Package B (child of A)", packageBCopy.getLabel());
    List<EntityType> entityTypesInBCopy = newArrayList(packageBCopy.getEntityTypes());
    List<Package> packagesInBCopy = newArrayList(packageBCopy.getChildren());
    assertEquals(1, entityTypesInBCopy.size());
    assertEquals(0, packagesInBCopy.size());

    EntityType entityTypeACopy = entityTypesInACopy.get(0);
    EntityType entityTypeBCopy = entityTypesInBCopy.get(0);
    assertEquals("EntityType A", entityTypeACopy.getLabel());
    assertEquals("EntityType B (referenced by A)", entityTypeBCopy.getLabel());
    assertEquals(ENTITY_TYPE_B, entityTypeA.getAttribute("xref_attr").getRefEntity().getId());
    assertEquals(
        entityTypeBCopy.getId(), entityTypeACopy.getAttribute("xref_attr").getRefEntity().getId());

    assertEquals(4, progress.getProgress());

    List<Object> entitiesOfA =
        dataService.findAll(entityTypeACopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(asList("0", "1", "2"), entitiesOfA);

    List<Object> entitiesOfB =
        dataService.findAll(entityTypeBCopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(asList("0", "1", "2"), entitiesOfB);

    cleanupTargetPackage(targetPackageId);
  }

  @WithMockUser(username = USERNAME)
  @SuppressWarnings({"OptionalGetWithoutIsPresent"})
  @Test
  void testCopyEntityType() {
    String targetPackageId = "target2";
    addTargetPackage(targetPackageId);
    ResourceIdentifier id = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, ENTITY_TYPE_A);
    TestProgress progress = new TestProgress();

    copyService.copy(singletonList(id), targetPackageId, progress);
    await().atMost(5, TimeUnit.SECONDS).until(copyJobFinished(progress));

    LOG.info("Copy job progress: {}/{}", progress.getProgress(), progress.getProgressMax());
    waitForWorkToBeFinished(indexService, LOG);

    Package targetPackage = metadataService.getPackage(targetPackageId).get();
    List<Package> packages = newArrayList(targetPackage.getChildren());
    List<EntityType> entityTypes = newArrayList(targetPackage.getEntityTypes());
    assertEquals(0, packages.size());
    assertEquals(1, entityTypes.size());

    EntityType entityTypeACopy = entityTypes.get(0);
    assertEquals("EntityType A", entityTypeACopy.getLabel());
    assertEquals(ENTITY_TYPE_B, entityTypeA.getAttribute("xref_attr").getRefEntity().getId());
    assertEquals(ENTITY_TYPE_B, entityTypeACopy.getAttribute("xref_attr").getRefEntity().getId());

    assertEquals(1, progress.getProgress());

    List<Object> entitiesOfA =
        dataService.findAll(entityTypeACopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(asList("0", "1", "2"), entitiesOfA);

    cleanupTargetPackage(targetPackageId);
  }

  private Callable<Boolean> copyJobFinished(TestProgress progress) {
    return () -> progress.getProgress() == progress.getProgressMax();
  }

  @Disabled // FIXME: reenable this test when fixed to preform in a stable manner
  @WithMockUser(username = USERNAME)
  @SuppressWarnings({"OptionalGetWithoutIsPresent"})
  void testCopyBoth() {
    String targetPackageId = "target3";
    addTargetPackage(targetPackageId);

    ResourceIdentifier id1 = ResourceIdentifier.create(ResourceType.PACKAGE, PACKAGE_B);
    ResourceIdentifier id2 = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, ENTITY_TYPE_A);
    TestProgress progress = new TestProgress();

    copyService.copy(asList(id1, id2), targetPackageId, progress);
    await().atMost(5, TimeUnit.SECONDS).until(copyJobFinished(progress));

    LOG.info("Copy job progress: {}/{}", progress.getProgress(), progress.getProgressMax());
    waitForWorkToBeFinished(indexService, LOG);

    Package targetPackage = metadataService.getPackage(targetPackageId).get();
    List<Package> packages = newArrayList(targetPackage.getChildren());
    List<EntityType> entityTypes = newArrayList(targetPackage.getEntityTypes());
    assertEquals(1, packages.size());
    assertEquals(1, entityTypes.size());
    Package packageBCopy = packages.get(0);

    assertEquals("Package B (child of A)", packageBCopy.getLabel());
    List<EntityType> entityTypesInBCopy = newArrayList(packageBCopy.getEntityTypes());
    List<Package> packagesInBCopy = newArrayList(packageBCopy.getChildren());
    assertEquals(1, entityTypesInBCopy.size());
    assertEquals(0, packagesInBCopy.size());

    EntityType entityTypeACopy = entityTypes.get(0);
    EntityType entityTypeBCopy = entityTypesInBCopy.get(0);
    assertEquals("EntityType A", entityTypeACopy.getLabel());
    assertEquals("EntityType B (referenced by A)", entityTypeBCopy.getLabel());
    assertEquals(entityTypeB.getId(), entityTypeA.getAttribute("xref_attr").getRefEntity().getId());
    assertEquals(
        entityTypeBCopy.getId(), entityTypeACopy.getAttribute("xref_attr").getRefEntity().getId());

    assertEquals(3, progress.getProgress());

    List<Object> entitiesOfA =
        dataService.findAll(entityTypeACopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(asList("0", "1", "2"), entitiesOfA);

    List<Object> entitiesOfB =
        dataService.findAll(entityTypeBCopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(asList("0", "1", "2"), entitiesOfB);

    cleanupTargetPackage(targetPackageId);
  }

  private void addTestEntityTypes() {
    entityTypeB = testHarness.createDynamicRefEntityType(ENTITY_TYPE_B);
    entityTypeA = testHarness.createDynamicTestEntityType(entityTypeB, ENTITY_TYPE_A);
    entityTypeA.setLabel("EntityType A");
    entityTypeB.setLabel("EntityType B (referenced by A)");
    entityTypeB.setPackage(packageB);
    entityTypeA.setPackage(packageA);
    metadataService.addEntityType(entityTypeB);
    metadataService.addEntityType(entityTypeA);
    List<Entity> refs = testHarness.createTestRefEntities(entityTypeB, 3);
    List<Entity> entities = testHarness.createTestEntities(entityTypeA, 3, refs).collect(toList());
    dataService.add(entityTypeB.getId(), refs.stream());
    dataService.add(entityTypeA.getId(), entities.stream());

    waitForWorkToBeFinished(indexService, LOG);
  }

  private void addTargetPackage(String id) {
    runAsSystem(
        () -> {
          Package pack = packageFactory.create(id);
          metadataService.addPackage(pack);
        });

    Sid sid = SidUtils.createUserSid(requireNonNull(getCurrentUsername()));
    Map<ObjectIdentity, PermissionSet> permissionMap =
        ImmutableMap.of(new PackageIdentity(id), PermissionSet.WRITEMETA);
    for (Entry<ObjectIdentity, PermissionSet> entry : permissionMap.entrySet()) {
      runAsSystem(
          () -> {
            testPermissionService.createPermission(
                Permission.create(entry.getKey(), sid, entry.getValue()));
          });
    }
    waitForWorkToBeFinished(indexService, LOG);
  }

  private void cleanupTargetPackage(String id) {
    runAsSystem(() -> dataService.deleteAll(PACKAGE, Stream.of(id)));
  }

  private void addPackages() {
    packageA = packageFactory.create(PACKAGE_A);
    packageA.setLabel("Package A");

    packageB = packageFactory.create(PACKAGE_B);
    packageB.setLabel("Package B (child of A)");
    packageB.setParent(packageA);

    metadataService.addPackage(packageA);
    metadataService.addPackage(packageB);

    waitForWorkToBeFinished(indexService, LOG);
  }

  @AfterEach
  void tearDownAfterClass() {
    runAsSystem(() -> dataService.deleteAll(PACKAGE, Stream.of(PACKAGE_A)));
    waitForWorkToBeFinished(indexService, LOG);
  }

  private void populatePermissions() {
    Map<ObjectIdentity, PermissionSet> permissionMap = new HashMap<>();
    permissionMap.put(new EntityTypeIdentity("sys_md_Package"), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity("sys_md_EntityType"), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity("sys_md_Attribute"), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity("sys_Language"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity("sys_L10nString"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity(DECORATOR_CONFIGURATION), PermissionSet.READ);
    permissionMap.put(new PackageIdentity(PACKAGE_A), PermissionSet.READ);

    Sid sid = SidUtils.createUserSid(requireNonNull(USERNAME));
    for (Entry<ObjectIdentity, PermissionSet> entry : permissionMap.entrySet()) {
      runAsSystem(
          () -> {
            testPermissionService.createPermission(
                Permission.create(entry.getKey(), sid, entry.getValue()));
          });
    }
  }
}
