package org.molgenis.integrationtest.platform;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.DYNAMIC_DECORATOR;
import static org.molgenis.integrationtest.platform.PlatformIT.waitForWorkToBeFinished;
import static org.molgenis.security.core.SidUtils.createUserSid;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.molgenis.data.decorator.meta.*;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.integrationtest.config.JsonTestConfig;
import org.molgenis.integrationtest.data.decorator.AddingRepositoryDecoratorFactory;
import org.molgenis.integrationtest.data.decorator.PostFixingRepositoryDecoratorFactory;
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
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      PlatformITConfig.class,
      AddingRepositoryDecoratorFactory.class,
      PostFixingRepositoryDecoratorFactory.class,
      JsonTestConfig.class
    })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
@Transactional
public class DynamicDecoratorIT extends AbstractTestNGSpringContextTests {
  private static final Logger LOG = LoggerFactory.getLogger(DynamicDecoratorIT.class);

  private static final String USERNAME = "dynamic-decorator-user";

  @Autowired private DataService dataService;
  @Autowired private IndexJobScheduler indexService;
  @Autowired private MetaDataService metaDataService;
  @Autowired private EntityTestHarness testHarness;
  @Autowired private DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry;
  @Autowired private DecoratorConfigurationFactory decoratorConfigurationFactory;
  @Autowired private DecoratorParametersFactory decoratorParametersFactory;
  @Autowired private PermissionService testPermissionService;

  private EntityType refEntityTypeDynamic;
  private EntityType entityTypeDynamic;
  private DynamicDecorator addingDynamicDecorator;
  private DynamicDecorator postfixDynamicDecorator;

  @BeforeClass
  public void setUp() {
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

          waitForWorkToBeFinished(indexService, LOG);

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

  @AfterClass
  public void tearDownAfterClass() {
    runAsSystem(
        () -> {
          dataService.deleteAll(entityTypeDynamic.getId());
          dataService.deleteAll(refEntityTypeDynamic.getId());
          dataService.deleteAll(DECORATOR_CONFIGURATION);
          dataService.deleteAll(DecoratorParametersMetadata.DECORATOR_PARAMETERS);
          metaDataService.deleteEntityType(entityTypeDynamic.getId());
          metaDataService.deleteEntityType(refEntityTypeDynamic.getId());
        });
    waitForWorkToBeFinished(indexService, LOG);
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

    testPermissionService.grant(permissionMap, createUserSid(requireNonNull(getCurrentUsername())));
  }
}
