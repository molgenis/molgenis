package org.molgenis.integrationtest.platform;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.integrationtest.platform.PlatformIT.waitForWorkToBeFinished;
import static org.molgenis.security.core.SidUtils.createUserSid;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
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
import org.molgenis.integrationtest.utils.TestProgress;
import org.molgenis.navigator.copy.service.CopyService;
import org.molgenis.navigator.copy.service.CopyServiceImpl;
import org.molgenis.navigator.copy.service.EntityTypeCopier;
import org.molgenis.navigator.copy.service.EntityTypeMetadataCopier;
import org.molgenis.navigator.copy.service.PackageCopier;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.navigator.util.ResourceCollector;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      PlatformITConfig.class,
      CopyServiceImpl.class,
      ResourceCollector.class,
      PackageCopier.class,
      EntityTypeCopier.class,
      EntityTypeMetadataCopier.class
    })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class CopyServiceIT extends AbstractTestNGSpringContextTests {

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

  @BeforeClass
  public void setUp() {
    runAsSystem(
        () -> {
          addPackages();
          addTestEntityTypes();
        });
  }

  @WithMockUser(username = USERNAME)
  @SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
  @Test
  public void testCopyPackage() {
    populatePermissions();
    String targetPackageId = "target1";
    addTargetPackage(targetPackageId);

    ResourceIdentifier id = ResourceIdentifier.create(ResourceType.PACKAGE, PACKAGE_A);
    TestProgress progress = new TestProgress();

    copyService.copy(singletonList(id), targetPackageId, progress);
    waitForWorkToBeFinished(indexService, LOG);

    Package targetPackage = metadataService.getPackage(targetPackageId).get();
    List<Package> packages = newArrayList(targetPackage.getChildren());
    assertEquals(packages.size(), 1);

    Package packageACopy = packages.get(0);
    assertEquals(packageACopy.getLabel(), "Package A");
    List<EntityType> entityTypesInACopy = newArrayList(packageACopy.getEntityTypes());
    List<Package> packagesInACopy = newArrayList(packageACopy.getChildren());
    assertEquals(entityTypesInACopy.size(), 1);
    assertEquals(packagesInACopy.size(), 1);

    Package packageBCopy = packagesInACopy.get(0);
    assertEquals(packageBCopy.getLabel(), "Package B (child of A)");
    List<EntityType> entityTypesInBCopy = newArrayList(packageBCopy.getEntityTypes());
    List<Package> packagesInBCopy = newArrayList(packageBCopy.getChildren());
    assertEquals(entityTypesInBCopy.size(), 1);
    assertEquals(packagesInBCopy.size(), 0);

    EntityType entityTypeACopy = entityTypesInACopy.get(0);
    EntityType entityTypeBCopy = entityTypesInBCopy.get(0);
    assertEquals(entityTypeACopy.getLabel(), "EntityType A");
    assertEquals(entityTypeBCopy.getLabel(), "EntityType B (referenced by A)");
    assertEquals(entityTypeA.getAttribute("xref_attr").getRefEntity().getId(), ENTITY_TYPE_B);
    assertEquals(
        entityTypeACopy.getAttribute("xref_attr").getRefEntity().getId(), entityTypeBCopy.getId());

    assertEquals(progress.getProgress(), 4);

    List<Object> entitiesOfA =
        dataService.findAll(entityTypeACopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(entitiesOfA, asList("0", "1", "2"));

    List<Object> entitiesOfB =
        dataService.findAll(entityTypeBCopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(entitiesOfB, asList("0", "1", "2"));

    cleanupTargetPackage(targetPackageId);
  }

  @WithMockUser(username = USERNAME)
  @SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
  @Test
  public void testCopyEntityType() {
    populatePermissions();
    String targetPackageId = "target2";
    addTargetPackage(targetPackageId);
    ResourceIdentifier id = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, ENTITY_TYPE_A);
    TestProgress progress = new TestProgress();

    copyService.copy(singletonList(id), targetPackageId, progress);
    waitForWorkToBeFinished(indexService, LOG);

    Package targetPackage = metadataService.getPackage(targetPackageId).get();
    List<Package> packages = newArrayList(targetPackage.getChildren());
    List<EntityType> entityTypes = newArrayList(targetPackage.getEntityTypes());
    assertEquals(packages.size(), 0);
    assertEquals(entityTypes.size(), 1);

    EntityType entityTypeACopy = entityTypes.get(0);
    assertEquals(entityTypeACopy.getLabel(), "EntityType A");
    assertEquals(entityTypeA.getAttribute("xref_attr").getRefEntity().getId(), ENTITY_TYPE_B);
    assertEquals(entityTypeACopy.getAttribute("xref_attr").getRefEntity().getId(), ENTITY_TYPE_B);

    assertEquals(progress.getProgress(), 1);

    List<Object> entitiesOfA =
        dataService.findAll(entityTypeACopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(entitiesOfA, asList("0", "1", "2"));

    cleanupTargetPackage(targetPackageId);
  }

  @WithMockUser(username = USERNAME)
  @SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
  @Test
  public void testCopyBoth() {
    populatePermissions();
    String targetPackageId = "target3";
    addTargetPackage(targetPackageId);

    ResourceIdentifier id1 = ResourceIdentifier.create(ResourceType.PACKAGE, PACKAGE_B);
    ResourceIdentifier id2 = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, ENTITY_TYPE_A);
    TestProgress progress = new TestProgress();

    copyService.copy(asList(id1, id2), targetPackageId, progress);
    waitForWorkToBeFinished(indexService, LOG);

    Package targetPackage = metadataService.getPackage(targetPackageId).get();
    List<Package> packages = newArrayList(targetPackage.getChildren());
    List<EntityType> entityTypes = newArrayList(targetPackage.getEntityTypes());
    assertEquals(packages.size(), 1);
    assertEquals(entityTypes.size(), 1);
    Package packageBCopy = packages.get(0);

    assertEquals(packageBCopy.getLabel(), "Package B (child of A)");
    List<EntityType> entityTypesInBCopy = newArrayList(packageBCopy.getEntityTypes());
    List<Package> packagesInBCopy = newArrayList(packageBCopy.getChildren());
    assertEquals(entityTypesInBCopy.size(), 1);
    assertEquals(packagesInBCopy.size(), 0);

    EntityType entityTypeACopy = entityTypes.get(0);
    EntityType entityTypeBCopy = entityTypesInBCopy.get(0);
    assertEquals(entityTypeACopy.getLabel(), "EntityType A");
    assertEquals(entityTypeBCopy.getLabel(), "EntityType B (referenced by A)");
    assertEquals(entityTypeA.getAttribute("xref_attr").getRefEntity().getId(), entityTypeB.getId());
    assertEquals(
        entityTypeACopy.getAttribute("xref_attr").getRefEntity().getId(), entityTypeBCopy.getId());

    assertEquals(progress.getProgress(), 3);

    List<Object> entitiesOfA =
        dataService.findAll(entityTypeACopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(entitiesOfA, asList("0", "1", "2"));

    List<Object> entitiesOfB =
        dataService.findAll(entityTypeBCopy.getId()).map(Entity::getIdValue).collect(toList());
    assertEquals(entitiesOfB, asList("0", "1", "2"));

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

    testPermissionService.grant(
        ImmutableMap.of(new PackageIdentity(id), PermissionSet.WRITEMETA),
        createUserSid(requireNonNull(getCurrentUsername())));

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

  @AfterClass
  public void tearDownAfterClass() {
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

    testPermissionService.grant(permissionMap, createUserSid(requireNonNull(getCurrentUsername())));
  }
}
