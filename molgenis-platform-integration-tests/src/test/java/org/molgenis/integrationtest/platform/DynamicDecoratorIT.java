package org.molgenis.integrationtest.platform;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.DYNAMIC_DECORATOR;
import static org.molgenis.integrationtest.platform.PlatformIT.waitForWorkToBeFinished;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.molgenis.data.decorator.meta.DecoratorConfiguration;
import org.molgenis.data.decorator.meta.DecoratorConfigurationFactory;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.data.decorator.meta.DecoratorParametersFactory;
import org.molgenis.data.decorator.meta.DecoratorParametersMetadata;
import org.molgenis.data.decorator.meta.DynamicDecorator;
import org.molgenis.data.index.IndexActionScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.integrationtest.config.JsonTestConfig;
import org.molgenis.integrationtest.data.decorator.AddingRepositoryDecoratorFactory;
import org.molgenis.integrationtest.data.decorator.PostFixingRepositoryDecoratorFactory;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.SidUtils;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(
    classes = {
      PlatformITConfig.class,
      AddingRepositoryDecoratorFactory.class,
      PostFixingRepositoryDecoratorFactory.class,
      JsonTestConfig.class
    })
@Transactional
public class DynamicDecoratorIT extends AbstractMockitoSpringContextTests {
  private static final Logger LOG = LoggerFactory.getLogger(DynamicDecoratorIT.class);

  private static final String USERNAME = "dynamic-decorator-user";

  @Autowired private DataService dataService;
  @Autowired private DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry;
  @Autowired private DecoratorConfigurationFactory decoratorConfigurationFactory;
  @Autowired private DecoratorParametersFactory decoratorParametersFactory;
  @Autowired private PermissionService testPermissionService;

  private static EntityType refEntityTypeDynamic;
  private static EntityType entityTypeDynamic;
  private static DynamicDecorator addingDynamicDecorator;
  private static DynamicDecorator postfixDynamicDecorator;

  @BeforeAll
  public static void setUpBeforeAll(ApplicationContext applicationContext) {
    EntityTestHarness testHarness = applicationContext.getBean(EntityTestHarness.class);
    MetaDataService metaDataService = applicationContext.getBean(MetaDataService.class);
    DataService dataService = applicationContext.getBean(DataService.class);

    refEntityTypeDynamic =
        testHarness.createDynamicRefEntityType("DynamicDecoratorITRefEntityType");
    entityTypeDynamic =
        testHarness.createDynamicTestEntityType(
            refEntityTypeDynamic, "DynamicDecoratorITEntityType");

    runAsSystem(
        () -> {
          metaDataService.addEntityType(refEntityTypeDynamic);
          metaDataService.addEntityType(entityTypeDynamic);
          List<Entity> refs = testHarness.createTestRefEntities(refEntityTypeDynamic, 1);
          List<Entity> entities =
              testHarness
                  .createTestEntities(entityTypeDynamic, 1, refs)
                  .collect(Collectors.toList());
          dataService.add(refEntityTypeDynamic.getId(), refs.stream());
          dataService.add(entityTypeDynamic.getId(), entities.stream());

          waitForWorkToBeFinished(applicationContext, LOG);

          addingDynamicDecorator =
              dataService.findOneById("sys_dec_DynamicDecorator", "add", DynamicDecorator.class);
          postfixDynamicDecorator =
              dataService.findOneById(
                  "sys_dec_DynamicDecorator", "postfix", DynamicDecorator.class);
        });
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void testRegistry() {
    populatePermissions();

    // check if all dynamic decorator factory beans get added to the registry
    List<String> factories =
        dynamicRepositoryDecoratorRegistry.getFactoryIds().collect(Collectors.toList());
    List<String> expected = asList("add", "postfix");
    assertTrue(factories.containsAll(expected));
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void testDynamicDecorator() {
    populatePermissions();

    // Add DecoratorParameters
    DecoratorParameters addingDecoratorParameters =
        decoratorParametersFactory.create("addingParams");
    addingDecoratorParameters.setDecorator(addingDynamicDecorator);
    addingDecoratorParameters.setParameters("{attr: 'int_attr'}");
    DecoratorParameters postfixDecoratorParameters =
        decoratorParametersFactory.create("postfixParams");
    postfixDecoratorParameters.setDecorator(postfixDynamicDecorator);
    postfixDecoratorParameters.setParameters("{attr: 'string_attr', text: '_TEST'}");
    dataService.add(
        DecoratorParametersMetadata.DECORATOR_PARAMETERS,
        Stream.of(addingDecoratorParameters, postfixDecoratorParameters));

    // Add DecoratorConfiguration
    DecoratorConfiguration decoratorConfiguration =
        decoratorConfigurationFactory.create("identifier");
    decoratorConfiguration.setDecoratorParameters(
        Stream.of(addingDecoratorParameters, postfixDecoratorParameters));
    decoratorConfiguration.setEntityTypeId(entityTypeDynamic.getId());
    dataService.add(DECORATOR_CONFIGURATION, decoratorConfiguration);

    // update row
    Entity entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
    dataService.update(entityTypeDynamic.getId(), entity);
    entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
    assertEquals("string1_TEST", entity.getString("string_attr"));
    assertEquals(11, entity.getInt("int_attr").intValue());

    // remove second decorator
    decoratorConfiguration.setDecoratorParameters(Stream.of(addingDecoratorParameters));
    dataService.update(DECORATOR_CONFIGURATION, decoratorConfiguration);

    // update row again
    dataService.update(entityTypeDynamic.getId(), entity);
    entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
    // stayed the same since the postfix decorator was removed
    assertEquals("string1_TEST", entity.getString("string_attr"));
    // added 1
    assertEquals(12, entity.getInt("int_attr").intValue());
  }

  @AfterAll
  public static void tearDownAfterClass(ApplicationContext applicationContext) {
    DataService dataService = applicationContext.getBean(DataService.class);
    MetaDataService metaDataService = applicationContext.getBean(MetaDataService.class);
    IndexActionScheduler indexActionScheduler =
        applicationContext.getBean(IndexActionScheduler.class);

    runAsSystem(
        () -> {
          dataService.deleteAll(entityTypeDynamic.getId());
          dataService.deleteAll(refEntityTypeDynamic.getId());
          dataService.deleteAll(DECORATOR_CONFIGURATION);
          dataService.deleteAll(DecoratorParametersMetadata.DECORATOR_PARAMETERS);
          waitForIndexToBeStable(entityTypeDynamic, indexActionScheduler, LOG);
          waitForIndexToBeStable(refEntityTypeDynamic, indexActionScheduler, LOG);
          metaDataService.deleteEntityType(entityTypeDynamic.getId());
          metaDataService.deleteEntityType(refEntityTypeDynamic.getId());
        });
    waitForWorkToBeFinished(applicationContext, LOG);
  }

  private void populatePermissions() {
    Map<ObjectIdentity, PermissionSet> permissionMap = new HashMap<>();
    permissionMap.put(new EntityTypeIdentity("sys_md_Package"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity("sys_md_EntityType"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity("sys_md_Attribute"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity("sys_Language"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity("sys_L10nString"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity(DYNAMIC_DECORATOR), PermissionSet.WRITE);
    permissionMap.put(
        new EntityTypeIdentity(DecoratorParametersMetadata.DECORATOR_PARAMETERS),
        PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity(DECORATOR_CONFIGURATION), PermissionSet.WRITE);

    permissionMap.put(new EntityTypeIdentity(entityTypeDynamic), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity(refEntityTypeDynamic), PermissionSet.READ);
    Sid sid = SidUtils.createUserSid(requireNonNull(getCurrentUsername()));
    for (Entry<ObjectIdentity, PermissionSet> entry : permissionMap.entrySet()) {
      runAsSystem(
          () -> {
            testPermissionService.createPermission(
                Permission.create(entry.getKey(), sid, entry.getValue()));
          });
    }
  }

  /**
   * Wait till the index is stable. Index job is executed asynchronously. This method waits for all
   * index jobs relevant for this entity to be finished.
   *
   * @param entityType name of the entity whose index needs to be stable
   */
  static void waitForIndexToBeStable(
      EntityType entityType, IndexActionScheduler indexService, Logger log) {
    try {
      indexService.waitForIndexToBeStableIncludingReferences(entityType);
      log.info("Index for entity [{}] incl. references is stable", entityType.getId());
    } catch (InterruptedException e) {
      log.info(
          "Interrupted waiting for [{}] incl. references to become stable", entityType.getId(), e);
    }
  }
}
