package org.molgenis.integrationtest.platform;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.EntityTestHarness.ATTR_BOOL;
import static org.molgenis.data.EntityTestHarness.ATTR_CATEGORICAL;
import static org.molgenis.data.EntityTestHarness.ATTR_CATEGORICAL_MREF;
import static org.molgenis.data.EntityTestHarness.ATTR_COMPOUND_CHILD_INT;
import static org.molgenis.data.EntityTestHarness.ATTR_DATE;
import static org.molgenis.data.EntityTestHarness.ATTR_DATETIME;
import static org.molgenis.data.EntityTestHarness.ATTR_DECIMAL;
import static org.molgenis.data.EntityTestHarness.ATTR_EMAIL;
import static org.molgenis.data.EntityTestHarness.ATTR_ENUM;
import static org.molgenis.data.EntityTestHarness.ATTR_HTML;
import static org.molgenis.data.EntityTestHarness.ATTR_HYPERLINK;
import static org.molgenis.data.EntityTestHarness.ATTR_ID;
import static org.molgenis.data.EntityTestHarness.ATTR_INT;
import static org.molgenis.data.EntityTestHarness.ATTR_LONG;
import static org.molgenis.data.EntityTestHarness.ATTR_MREF;
import static org.molgenis.data.EntityTestHarness.ATTR_REF_ID;
import static org.molgenis.data.EntityTestHarness.ATTR_SCRIPT;
import static org.molgenis.data.EntityTestHarness.ATTR_STRING;
import static org.molgenis.data.EntityTestHarness.ATTR_XREF;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.exception.NullPackageNotSuException;
import org.molgenis.data.security.exception.PackagePermissionDeniedException;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.util.MolgenisDateFormat;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.SidUtils;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@TestMethodOrder(OrderAnnotation.class)
@ContextConfiguration(classes = {PlatformITConfig.class})
@Transactional
@Commit
public class MetaDataServiceIT extends AbstractMockitoSpringContextTests {
  private static final String USERNAME = "metaDataService-user";

  private static final String ENTITY_TYPE_ID = "metaDataServiceEntityType";
  private static final String REF_ENTITY_TYPE_ID = "metaDataServiceRefEntityType";
  private static final String PACK_NO_WRITEMETA_PERMISSION = "packageNoWriteMeta";
  private static final String PACK_PERMISSION = "packageWriteMeta";
  public static final String ENTITY_TYPE_3 = "entityType3";
  public static final String ENTITY_TYPE_2 = "entityType2";
  public static final String ENTITY_TYPE_1 = "entityType1";
  private static List<Entity> refEntities;

  @Autowired private MetaDataService metaDataService;
  @Autowired private DataService dataService;
  @Autowired private AttributeFactory attributeFactory;
  @Autowired private EntityTypeFactory entityTypeFactory;

  private static Package packNoPermission;
  private static Package packPermission;

  @BeforeAll
  public static void setUpBeforeAll(ApplicationContext applicationContext) {
    runAsSystem(
        () -> {
          populate(applicationContext);
          waitForAllIndicesStable(applicationContext);
        });
  }

  @AfterAll
  public static void tearDownAfterAll(ApplicationContext applicationContext) {
    runAsSystem(
        () -> {
          depopulate(applicationContext);
          waitForAllIndicesStable(applicationContext);
        });
  }

  @WithMockUser(username = USERNAME)
  @Test
  @Order(1)
  public void testUpdateEntityType() {
    EntityType updatedEntityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    updatedEntityType
        .getAttribute(ATTR_STRING)
        .setDataType(ENUM)
        .setEnumOptions(asList("string0", "string1"));
    updatedEntityType.getAttribute(ATTR_BOOL).setDataType(STRING);
    updatedEntityType.getAttribute(ATTR_CATEGORICAL).setDataType(LONG).setRefEntity(null);
    updatedEntityType.getAttribute(ATTR_CATEGORICAL_MREF).setDataType(MREF);
    updatedEntityType.getAttribute(ATTR_DATE).setDataType(DATE_TIME);
    updatedEntityType.getAttribute(ATTR_DATETIME).setDataType(DATE);
    updatedEntityType.getAttribute(ATTR_EMAIL).setDataType(STRING);
    updatedEntityType.getAttribute(ATTR_DECIMAL).setDataType(INT);
    updatedEntityType.getAttribute(ATTR_HTML).setDataType(TEXT);
    updatedEntityType.getAttribute(ATTR_HYPERLINK).setDataType(STRING);
    updatedEntityType.getAttribute(ATTR_LONG).setDataType(DECIMAL);
    updatedEntityType.getAttribute(ATTR_INT).setDataType(LONG);
    updatedEntityType.getAttribute(ATTR_SCRIPT).setDataType(TEXT);
    updatedEntityType.getAttribute(ATTR_XREF).setDataType(CATEGORICAL);
    updatedEntityType.getAttribute(ATTR_MREF).setDataType(CATEGORICAL_MREF);
    updatedEntityType.getAttribute(ATTR_ENUM).setDataType(STRING).setEnumOptions(emptyList());

    metaDataService.updateEntityType(updatedEntityType);

    Entity expectedEntity = new DynamicEntity(updatedEntityType);
    expectedEntity.set(ATTR_ID, "0");
    expectedEntity.set(ATTR_STRING, "string1");
    expectedEntity.set(ATTR_BOOL, "true");
    expectedEntity.set(ATTR_CATEGORICAL, 0L);
    expectedEntity.set(ATTR_CATEGORICAL_MREF, singletonList(refEntities.get(0)));
    expectedEntity.set(
        ATTR_DATE, LocalDate.parse("2012-12-21").atStartOfDay(systemDefault()).toInstant());
    expectedEntity.set(ATTR_DATETIME, MolgenisDateFormat.parseLocalDate("1985-08-12T06:12:13Z"));
    expectedEntity.set(ATTR_EMAIL, "this.is@mail.address");
    expectedEntity.set(ATTR_DECIMAL, 0); // before update: 0.123
    expectedEntity.set(ATTR_HTML, null);
    expectedEntity.set(ATTR_HYPERLINK, "http://www.molgenis.org");
    expectedEntity.set(ATTR_LONG, 0.0);
    expectedEntity.set(ATTR_INT, 10L);
    expectedEntity.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
    expectedEntity.set(ATTR_XREF, refEntities.get(0));
    expectedEntity.set(ATTR_MREF, singletonList(refEntities.get(0)));
    expectedEntity.set(ATTR_COMPOUND_CHILD_INT, 10);
    expectedEntity.set(ATTR_ENUM, "option1");
    expectedEntity = new EntityWithComputedAttributes(expectedEntity);

    assertTrue(EntityUtils.equals(dataService.findOneById(ENTITY_TYPE_ID, "0"), expectedEntity));
  }

  @WithMockUser(username = USERNAME)
  @Test
  @Order(2)
  public void testUpdateEntityTypeXrefMrefChange() {
    EntityType updatedEntityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    updatedEntityType.getAttribute(ATTR_XREF).setDataType(MREF);

    metaDataService.updateEntityType(updatedEntityType);

    Entity expectedEntity = new DynamicEntity(updatedEntityType);
    expectedEntity.set(ATTR_XREF, singletonList(refEntities.get(0)));

    Entity entity = dataService.findOneById(ENTITY_TYPE_ID, "0");
    assertNotNull(entity);
    List<Entity> entities = newArrayList(entity.getEntities(ATTR_XREF));
    assertEquals(1, entities.size());
    assertEquals("0", entities.get(0).getIdValue());
  }

  @WithMockUser(username = USERNAME)
  @Test
  @Order(3)
  public void testUpdateEntityTypeMrefXrefChange() {
    EntityType updatedEntityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    updatedEntityType.getAttribute(ATTR_MREF).setDataType(XREF);

    metaDataService.updateEntityType(updatedEntityType);

    Entity expectedEntity = new DynamicEntity(updatedEntityType);
    expectedEntity.set(ATTR_MREF, refEntities.get(0));

    Entity entity = dataService.findOneById(ENTITY_TYPE_ID, "0");
    assertNotNull(entity);
    Entity refEntity = entity.getEntity(ATTR_MREF);
    assertEquals("0", refEntity.getIdValue());
  }

  @SuppressWarnings("deprecation")
  @WithMockUser(username = USERNAME)
  @Test
  @Order(4)
  public void testUpdateEntityTypeNotAllowed() {
    EntityType updatedEntityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    ;
    updatedEntityType.getAttribute(ATTR_COMPOUND_CHILD_INT).setDataType(DATE_TIME);
    Exception exception =
        assertThrows(
            MolgenisDataException.class, () -> metaDataService.updateEntityType(updatedEntityType));
    assertThat(exception.getMessage())
        .containsPattern(
            "Attribute data type update from \\[INT\\] to \\[DATE_TIME\\] not allowed, allowed types are \\[BOOL, CATEGORICAL, DECIMAL, ENUM, LONG, STRING, TEXT, XREF\\]");
  }

  @SuppressWarnings("deprecation")
  @WithMockUser(username = USERNAME)
  @Test
  @Order(5)
  public void testCreateNoPackagePermission() {
    EntityType entityType =
        entityTypeFactory
            .create(ENTITY_TYPE_1)
            .setLabel("label")
            .setBackend("PostgreSQL")
            .setPackage(packNoPermission);
    entityType.addAttribute(
        attributeFactory
            .create()
            .setIdentifier(ATTR_REF_ID)
            .setName("attr")
            .setDataType(STRING)
            .setIdAttribute(true)
            .setNillable(false));
    assertThrows(
        PackagePermissionDeniedException.class, () -> metaDataService.createRepository(entityType));
  }

  @SuppressWarnings("deprecation")
  @WithMockUser(username = USERNAME)
  @Test
  @Order(6)
  public void testCreateNullPermission() {
    EntityType entityType =
        entityTypeFactory.create(ENTITY_TYPE_2).setLabel("label").setBackend("PostgreSQL");
    entityType.addAttribute(
        attributeFactory
            .create()
            .setIdentifier(ATTR_REF_ID)
            .setName("attr")
            .setDataType(STRING)
            .setIdAttribute(true)
            .setNillable(false));
    assertThrows(
        NullPackageNotSuException.class, () -> metaDataService.createRepository(entityType));
  }

  @SuppressWarnings("deprecation")
  @WithMockUser(username = USERNAME)
  @Test
  @Order(7)
  public void testCreatePermission() {
    EntityType entityType =
        entityTypeFactory
            .create(ENTITY_TYPE_3)
            .setLabel("label")
            .setBackend("PostgreSQL")
            .setPackage(packPermission);
    entityType.addAttribute(
        attributeFactory
            .create()
            .setIdentifier(ATTR_REF_ID)
            .setName("attr")
            .setDataType(STRING)
            .setIdAttribute(true)
            .setNillable(false));
    metaDataService.createRepository(entityType);
    assertTrue(dataService.hasRepository(ENTITY_TYPE_3));
  }

  @WithMockUser(username = USERNAME)
  @Test
  @Order(8)
  public void testAddAttribute() {
    EntityType entityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    ;
    Attribute attribute = attributeFactory.create().setName("newAttribute");
    attribute.setEntity(entityType);
    metaDataService.addAttribute(attribute);

    EntityType updatedEntityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    ;
    assertTrue(EntityUtils.equals(attribute, updatedEntityType.getAttribute("newAttribute")));
  }

  @WithMockUser(username = USERNAME)
  @Test
  @Order(9)
  public void testUpdateAttribute() {
    EntityType entityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    Attribute attribute = entityType.getAttribute("newAttribute");
    attribute.setLabel("updated-label");
    attribute.setEntity(entityType);
    dataService.update(ATTRIBUTE_META_DATA, attribute);

    EntityType updatedEntityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    ;
    assertTrue(EntityUtils.equals(attribute, updatedEntityType.getAttribute("newAttribute")));
  }

  @WithMockUser(username = USERNAME)
  @Test
  @Order(10)
  public void testDeleteAttribute() {
    EntityType entityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    ;
    String attributeId = entityType.getAttribute("newAttribute").getIdentifier();
    metaDataService.deleteAttributeById(attributeId);

    EntityType updatedEntityType =
        metaDataService
            .getEntityType(ENTITY_TYPE_ID)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_ID));
    ;
    org.junit.jupiter.api.Assertions.assertNull(updatedEntityType.getAttribute("newAttribute"));
  }

  private static void populate(ApplicationContext applicationContext) {
    populateData(applicationContext);
    populateDataPermissions(applicationContext);
  }

  private static void populateData(ApplicationContext applicationContext) {
    EntityTestHarness entityTestHarness = applicationContext.getBean(EntityTestHarness.class);
    MetaDataService metaDataService = applicationContext.getBean(MetaDataService.class);
    DataService dataService = applicationContext.getBean(DataService.class);
    PackageFactory packageFactory = applicationContext.getBean(PackageFactory.class);

    EntityType refEntityType =
        entityTestHarness.createDynamicRefEntityType("metaDataServiceRefEntityType");
    metaDataService.createRepository(refEntityType);
    refEntities = entityTestHarness.createTestRefEntities(refEntityType, 3);
    dataService.add(refEntityType.getId(), refEntities.stream());

    EntityType entityType =
        entityTestHarness.createDynamicTestEntityType(refEntityType, "metaDataServiceEntityType");
    metaDataService.createRepository(entityType);
    List<Entity> entities =
        entityTestHarness.createTestEntities(entityType, 1, refEntities).collect(toList());
    dataService.add(entityType.getId(), entities.stream());

    packNoPermission = packageFactory.create(PACK_NO_WRITEMETA_PERMISSION);
    packPermission = packageFactory.create(PACK_PERMISSION);

    metaDataService.addPackage(packNoPermission);
    metaDataService.addPackage(packPermission);
  }

  private static void populateDataPermissions(ApplicationContext applicationContext) {
    PermissionService permissionService = applicationContext.getBean(PermissionService.class);

    Map<ObjectIdentity, PermissionSet> permissionMap = new HashMap<>();
    permissionMap.put(new EntityTypeIdentity("sys_md_Package"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity("sys_md_EntityType"), PermissionSet.WRITEMETA);
    permissionMap.put(new EntityTypeIdentity("sys_md_Attribute"), PermissionSet.WRITEMETA);
    permissionMap.put(new EntityTypeIdentity("sys_dec_DecoratorConfiguration"), PermissionSet.READ);
    permissionMap.put(new EntityTypeIdentity(ENTITY_TYPE_ID), PermissionSet.WRITEMETA);
    permissionMap.put(new EntityTypeIdentity(REF_ENTITY_TYPE_ID), PermissionSet.READ);
    permissionMap.put(new PackageIdentity(PACK_PERMISSION), PermissionSet.WRITEMETA);
    permissionMap.put(new PackageIdentity(PACK_NO_WRITEMETA_PERMISSION), PermissionSet.WRITE);

    Sid sid = SidUtils.createUserSid(USERNAME);
    for (Entry<ObjectIdentity, PermissionSet> entry : permissionMap.entrySet()) {
      permissionService.createPermission(Permission.create(entry.getKey(), sid, entry.getValue()));
    }
  }

  private static void depopulate(ApplicationContext applicationContext) {
    DataService dataService = applicationContext.getBean(DataService.class);
    List<EntityType> entityTypes =
        dataService
            .findAll(
                ENTITY_TYPE_META_DATA,
                Stream.of(
                    ENTITY_TYPE_ID,
                    REF_ENTITY_TYPE_ID,
                    ENTITY_TYPE_1,
                    ENTITY_TYPE_2,
                    ENTITY_TYPE_3),
                EntityType.class)
            .collect(toList());
    dataService.getMeta().deleteEntityType(entityTypes);
    dataService.delete(PackageMetadata.PACKAGE, Stream.of(packNoPermission, packPermission));
  }

  private static void waitForAllIndicesStable(ApplicationContext applicationContext) {
    IndexJobScheduler indexJobScheduler = applicationContext.getBean(IndexJobScheduler.class);
    try {
      indexJobScheduler.waitForAllIndicesStable();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
