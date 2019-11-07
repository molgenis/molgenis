package org.molgenis.integrationtest.platform;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.molgenis.data.EntityTestHarness.ATTR_BOOL;
import static org.molgenis.data.EntityTestHarness.ATTR_DECIMAL;
import static org.molgenis.data.EntityTestHarness.ATTR_ENUM;
import static org.molgenis.data.EntityTestHarness.ATTR_INT;
import static org.molgenis.data.EntityTestHarness.ATTR_REF_STRING;
import static org.molgenis.data.i18n.model.L10nStringMetadata.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.security.core.SidUtils.createUserSid;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.util.i18n.LanguageService.getBundle;
import static org.molgenis.util.i18n.LanguageService.getCurrentUserLanguageCode;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySelfXrefTestHarness;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.i18n.model.L10nStringMetadata;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.index.IndexActionRegisterServiceImpl;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetadata;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.util.i18n.LanguageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(classes = {PlatformITConfig.class})
@Transactional
class PlatformIT extends AbstractMockitoSpringContextTests {

  private static final Logger LOG = LoggerFactory.getLogger(PlatformIT.class);

  private static final String USERNAME = "platform-user";

  private static EntityType entityTypeStatic;
  private static EntityType refEntityTypeStatic;
  private static EntityType entityTypeDynamic;
  private static EntityType refEntityTypeDynamic;
  private static EntityType selfXrefEntityType;

  @Autowired private IndexJobScheduler indexService;
  @Autowired private EntityTestHarness testHarness;
  @Autowired private EntitySelfXrefTestHarness entitySelfXrefTestHarness;
  @Autowired private DataService dataService;
  @Autowired private ElasticsearchService searchService;
  @Autowired private MetaDataService metaDataService;
  @Autowired private EntityListenersService entityListenersService;
  @Autowired private AttributeFactory attributeFactory;
  @Autowired private IndexActionRegisterServiceImpl indexActionRegisterService;
  @Autowired private L10nStringFactory l10nStringFactory;
  @Autowired private PackageFactory packageFactory;
  @Autowired private PermissionService testPermissionService;

  static void waitForWorkToBeFinished(ApplicationContext applicationContext, Logger log) {
    IndexJobScheduler indexJobScheduler = applicationContext.getBean(IndexJobScheduler.class);
    waitForWorkToBeFinished(indexJobScheduler, log);
  }

  /** Wait till the whole index is stable. Index job is done a-synchronized. */
  static void waitForWorkToBeFinished(IndexJobScheduler indexService, Logger log) {
    try {
      indexService.waitForAllIndicesStable();
      log.info("All work finished");
    } catch (InterruptedException e) {
      log.warn("Interrupted while waiting for index to become stable!", e);
      fail("Interrupted while waiting for index to become stable!");
    }
  }

  /**
   * Wait till the index is stable. Index job is executed asynchronously. This method waits for all
   * index jobs relevant for this entity to be finished.
   *
   * @param entityType name of the entity whose index needs to be stable
   */
  static void waitForIndexToBeStable(
      EntityType entityType, IndexJobScheduler indexService, Logger log) {
    try {
      indexService.waitForIndexToBeStableIncludingReferences(entityType);
      log.info("Index for entity [{}] incl. references is stable", entityType.getId());
    } catch (InterruptedException e) {
      log.info(
          "Interrupted waiting for [{}] incl. references to become stable", entityType.getId(), e);
    }
  }

  @BeforeAll
  static void setUpBeforeAll(ApplicationContext applicationContext) {
    EntityTestHarness testHarness = applicationContext.getBean(EntityTestHarness.class);
    EntitySelfXrefTestHarness entitySelfXrefTestHarness =
        applicationContext.getBean(EntitySelfXrefTestHarness.class);
    MetaDataService metaDataService = applicationContext.getBean(MetaDataService.class);

    refEntityTypeStatic = testHarness.createStaticRefTestEntityType();
    entityTypeStatic = testHarness.createStaticTestEntityType();
    refEntityTypeDynamic = testHarness.createDynamicRefEntityType("PlatformITRefEntityType");
    entityTypeDynamic =
        testHarness.createDynamicTestEntityType(refEntityTypeDynamic, "PlatformITEntityType");

    // Create a self refer entity
    selfXrefEntityType = entitySelfXrefTestHarness.createDynamicEntityType();

    runAsSystem(
        () -> {
          addDefaultLanguages(applicationContext);
          metaDataService.addEntityType(refEntityTypeDynamic);
          metaDataService.addEntityType(entityTypeDynamic);
          metaDataService.addEntityType(selfXrefEntityType);
          entitySelfXrefTestHarness.addSelfReference(selfXrefEntityType);
          metaDataService.updateEntityType(selfXrefEntityType);
        });
    waitForWorkToBeFinished(applicationContext, LOG);
  }

  @BeforeEach
  void setUpBeforeEach() {
    entityTypeDynamic =
        runAsSystem(
            () ->
                metaDataService
                    .getEntityType(entityTypeDynamic.getId())
                    .orElseThrow(() -> new UnknownEntityTypeException(entityTypeDynamic.getId())));
  }

  @AfterAll
  static void setUpAfterAll(ApplicationContext applicationContext) {
    DataService dataService = applicationContext.getBean(DataService.class);
    IndexJobScheduler indexJobScheduler = applicationContext.getBean(IndexJobScheduler.class);
    MetaDataService metaDataService = applicationContext.getBean(MetaDataService.class);

    runAsSystem(
        () -> {
          dataService.deleteAll(entityTypeStatic.getId());
          dataService.deleteAll(refEntityTypeStatic.getId());
          dataService.deleteAll(entityTypeDynamic.getId());
          dataService.deleteAll(refEntityTypeDynamic.getId());
          dataService.deleteAll(selfXrefEntityType.getId());
        });
    waitForIndexToBeStable(entityTypeStatic, indexJobScheduler, LOG);
    waitForIndexToBeStable(refEntityTypeStatic, indexJobScheduler, LOG);
    waitForIndexToBeStable(entityTypeDynamic, indexJobScheduler, LOG);
    waitForIndexToBeStable(refEntityTypeDynamic, indexJobScheduler, LOG);
    waitForIndexToBeStable(selfXrefEntityType, indexJobScheduler, LOG);
    cleanupUserPermissions(applicationContext);

    runAsSystem(
        () -> {
          entityTypeDynamic = dataService.getEntityType(entityTypeDynamic.getId());
          refEntityTypeDynamic = dataService.getEntityType(refEntityTypeDynamic.getId());
          selfXrefEntityType = dataService.getEntityType(selfXrefEntityType.getId());
          metaDataService.deleteEntityTypes(
              asList(
                  refEntityTypeDynamic.getId(),
                  entityTypeDynamic.getId(),
                  selfXrefEntityType.getId()));
        });

    waitForWorkToBeFinished(applicationContext, LOG);
  }

  private static void addDefaultLanguages(ApplicationContext applicationContext) {
    DataService dataService = applicationContext.getBean(DataService.class);
    LanguageFactory languageFactory = applicationContext.getBean(LanguageFactory.class);

    if (dataService.count(LANGUAGE) == 0) {
      dataService.add(
          LANGUAGE,
          languageFactory.create(
              LanguageService.DEFAULT_LANGUAGE_CODE, LanguageService.DEFAULT_LANGUAGE_NAME, true));
      dataService.add(
          LANGUAGE,
          languageFactory.create("nl", new Locale("nl").getDisplayName(new Locale("nl")), false));
      dataService.add(
          LANGUAGE,
          languageFactory.create("pt", new Locale("pt").getDisplayName(new Locale("pt")), false));
      dataService.add(
          LANGUAGE,
          languageFactory.create("es", new Locale("es").getDisplayName(new Locale("es")), false));
      dataService.add(
          LANGUAGE,
          languageFactory.create("de", new Locale("de").getDisplayName(new Locale("de")), false));
      dataService.add(
          LANGUAGE,
          languageFactory.create("it", new Locale("it").getDisplayName(new Locale("it")), false));
      dataService.add(
          LANGUAGE,
          languageFactory.create("fr", new Locale("fr").getDisplayName(new Locale("fr")), false));
      dataService.add(LANGUAGE, languageFactory.create("xx", "My language", false));
    }
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testLanguageService() {
    populateUserPermissions();

    assertEquals(
        "labelEn",
        dataService
            .getMeta()
            .getEntityType(ENTITY_TYPE_META_DATA)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_META_DATA))
            .getAttribute("labelEn")
            .getName());
    assertEquals(
        "label",
        dataService
            .getMeta()
            .getEntityType(ENTITY_TYPE_META_DATA)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_META_DATA))
            .getLabelAttribute("en")
            .getName());
    assertEquals(
        "label",
        dataService
            .getMeta()
            .getEntityType(ENTITY_TYPE_META_DATA)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_META_DATA))
            .getLabelAttribute("pt")
            .getName());
    assertEquals(
        "label",
        dataService
            .getMeta()
            .getEntityType(ENTITY_TYPE_META_DATA)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_META_DATA))
            .getLabelAttribute("nl")
            .getName());
    assertEquals(
        "label",
        dataService
            .getMeta()
            .getEntityType(ENTITY_TYPE_META_DATA)
            .orElseThrow(() -> new UnknownEntityTypeException(ENTITY_TYPE_META_DATA))
            .getLabelAttribute()
            .getName());

    assertEquals("en", getCurrentUserLanguageCode());
    assertArrayEquals(
        new String[] {"en", "nl", "de", "es", "it", "pt", "fr", "xx"},
        getLanguageCodes().toArray());

    // NL
    assertNotNull(dataService.getEntityType(L10N_STRING).getAttribute("nl"));
    assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("labelNl"));
    assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("descriptionNl"));
    assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("labelNl"));
    assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("descriptionNl"));

    // EN
    assertNotNull(dataService.getEntityType(L10N_STRING).getAttribute("en"));
    assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("labelEn"));
    assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("descriptionEn"));
    assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("labelEn"));
    assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("descriptionEn"));

    L10nString car = l10nStringFactory.create();
    car.setMessageID("car");
    car.set("en", "car");
    car.set("nl", "auto");
    car.setNamespace("platform-it");
    dataService.add(L10nStringMetadata.L10N_STRING, car);

    // Test default value
    assertEquals("car", getBundle().getString("car"));
  }

  /** Test used as a caching benchmark */
  @Disabled
  @WithMockUser(username = USERNAME)
  @Test
  void cachePerformanceTest() {
    populateUserPermissions();

    createDynamicAndAdd(10000);

    Query<Entity> q1 = new QueryImpl<>().eq(EntityTestHarness.ATTR_STRING, "string1");
    q1.pageSize(1000);

    Query<Entity> q2 = new QueryImpl<>().eq(EntityTestHarness.ATTR_BOOL, true);
    q2.pageSize(500);

    Query<Entity> q3 = new QueryImpl<>().eq(ATTR_DECIMAL, 1.123);

    runAsSystem(
        () -> {
          for (int i = 0; i < 100000; i++) {
            dataService.findAll(entityTypeDynamic.getId(), q1);
            dataService.findAll(entityTypeDynamic.getId(), q2);
            dataService.findOne(entityTypeDynamic.getId(), q3);
          }
        });
  }

  @Disabled // FIXME: sys_md_attributes spam
  @WithMockUser(username = USERNAME)
  @Test
  void testUpdateSingleRefEntityIndexesLargeAmountOfReferencingEntities() {
    populateUserPermissions();

    createDynamicAndAdd(10000);

    Query<Entity> q = new QueryImpl<>().search("refstring4").or().search("refstring5");

    assertEquals(3333, searchService.count(entityTypeDynamic, q));
    Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getId(), "4");
    refEntity4.set(ATTR_REF_STRING, "qwerty");
    runAsSystem(() -> dataService.update(refEntityTypeDynamic.getId(), refEntity4));

    Entity refEntity5 = dataService.findOneById(refEntityTypeDynamic.getId(), "5");
    refEntity5.set(ATTR_REF_STRING, "qwerty");
    runAsSystem(() -> dataService.update(refEntityTypeDynamic.getId(), refEntity5));

    waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
    assertEquals(0, searchService.count(entityTypeDynamic, q));

    assertEquals(3333, searchService.count(entityTypeDynamic, new QueryImpl<>().search("qwerty")));
  }

  private List<Entity> createDynamicAndAdd(int count) {
    return createAndAdd(entityTypeDynamic, refEntityTypeDynamic, count);
  }

  private List<Entity> createAndAdd(EntityType entityType, EntityType refEntityType, int count) {
    List<Entity> entities = createTestEntities(entityType, refEntityType, count).collect(toList());
    dataService.add(entityType.getId(), entities.stream());
    waitForIndexToBeStable(entityType, indexService, LOG);
    return entities;
  }

  private Stream<Entity> createTestEntities(
      EntityType entityType, EntityType refEntityType, int count) {
    List<Entity> refEntities = testHarness.createTestRefEntities(refEntityType, 6);
    //noinspection RedundantCast
    runAsSystem((Runnable) () -> dataService.add(refEntityType.getId(), refEntities.stream()));
    return testHarness.createTestEntities(entityType, count, refEntities);
  }

  private void assertPresent(EntityType emd, List<Entity> entities) {
    entities.forEach(e -> assertPresent(emd, e));
  }

  private void assertPresent(EntityType emd, Entity entity) {
    // Found in PostgreSQL
    assertNotNull(dataService.findOneById(emd.getId(), entity.getIdValue()));

    // Found in index Elasticsearch
    Query<Entity> q = new QueryImpl<>();
    q.eq(emd.getIdAttribute().getName(), entity.getIdValue());
    assertEquals(1, searchService.count(emd, q));
  }

  private void assertNotPresent(Entity entity) {
    // Found in PostgreSQL
    org.junit.jupiter.api.Assertions.assertNull(
        dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue()));

    // Not found in index Elasticsearch
    Query<Entity> q = new QueryImpl<>();
    q.eq(entityTypeDynamic.getIdAttribute().getName(), entity.getIdValue());
    assertEquals(0, searchService.count(entityTypeDynamic, q));
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testIndexCreateMetaData() {
    populateUserPermissions();

    IndexMetadataCUDOperationsPlatformIT.testIndexCreateMetaData(
        searchService, entityTypeStatic, entityTypeDynamic, metaDataService);
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testIndexUpdateMetaDataRemoveCompoundAttribute() {
    populateUserPermissions();

    IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveCompoundAttribute(
        entityTypeDynamic, attributeFactory, searchService, metaDataService, indexService);
  }

  @WithMockUser(username = USERNAME)
  @Test
  void storeIndexActions() {
    populateUserPermissions();

    List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
    List<Entity> entities =
        testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
    runAsSystem(
        () -> {
          dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
          dataService.add(entityTypeDynamic.getId(), entities.stream());
          waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

          indexActionRegisterService.register(entityTypeDynamic, "1");
          indexActionRegisterService.register(entityTypeDynamic, null);

          Query<IndexAction> q = new QueryImpl<>();
          q.eq(IndexActionMetadata.ENTITY_TYPE_ID, "sys_test_TypeTestDynamic");
          Stream<org.molgenis.data.index.meta.IndexAction> all =
              dataService.findAll(IndexActionMetadata.INDEX_ACTION, q, IndexAction.class);
          all.forEach(e -> LOG.info(e.getEntityTypeId() + "." + e.getEntityId()));
          waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
        });
  }

  @Disabled
  @WithMockUser(username = USERNAME)
  @Test
  void testDistinctAggregateQueryManyRows() {
    populateUserPermissions();

    createDynamicAndAdd(20000);
    Query<Entity> query = new QueryImpl<>().eq(ATTR_BOOL, true);

    AggregateResult result = runAggregateQuery(ATTR_BOOL, ATTR_ENUM, ATTR_ENUM, query);

    AggregateResult expectedResult =
        new AggregateResult(
            singletonList(singletonList(1L)), singletonList(1L), singletonList("option1"));
    assertEquals(expectedResult, result);
  }

  @Disabled
  @WithMockUser(username = USERNAME)
  @Test
  void testAggregateQueryManyRows() {
    populateUserPermissions();

    createDynamicAndAdd(1000000);
    Query<Entity> query = new QueryImpl<>().eq(ATTR_BOOL, true).or().lt(ATTR_INT, 15000);

    AggregateResult result = runAggregateQuery(ATTR_BOOL, ATTR_ENUM, null, query);

    AggregateResult expectedResult =
        new AggregateResult(
            asList(asList(0L, 7495L), asList(500000L, 0L)),
            asList(0L, 1L),
            asList("option1", "option2"));
    assertEquals(expectedResult, result);
  }

  private AggregateResult runAggregateQuery(
      String attrX, String attrY, String attrDistinct, Query<Entity> query) {
    requireNonNull(attrX);
    requireNonNull(query);

    Attribute x = entityTypeDynamic.getAttribute(attrX);
    Attribute y = attrY != null ? entityTypeDynamic.getAttribute(attrY) : null;
    Attribute distinct = attrDistinct != null ? entityTypeDynamic.getAttribute(attrDistinct) : null;

    AggregateQuery aggregateQuery = new AggregateQueryImpl(x, y, distinct, query);
    return runAsSystem(() -> dataService.aggregate(entityTypeDynamic.getId(), aggregateQuery));
  }

  private void populateUserPermissions() {
    Map<ObjectIdentity, PermissionSet> entityTypePermissionMap =
        getObjectIdentityPermissionSetMap();
    Sid sid = createUserSid(requireNonNull(USERNAME));
    for (Entry<ObjectIdentity, PermissionSet> entry : entityTypePermissionMap.entrySet()) {
      runAsSystem(
          () -> {
            testPermissionService.createPermission(
                Permission.create(entry.getKey(), sid, entry.getValue()));
          });
    }
  }

  private static void cleanupUserPermissions(ApplicationContext applicationContext) {
    PermissionService permissionService = applicationContext.getBean(PermissionService.class);
    Map<ObjectIdentity, PermissionSet> entityTypePermissionMap =
        getObjectIdentityPermissionSetMap();
    Sid sid = createUserSid(requireNonNull(USERNAME));
    for (Entry<ObjectIdentity, PermissionSet> entry : entityTypePermissionMap.entrySet()) {
      runAsSystem(
          () -> {
            if (!permissionService
                .getPermissionsForObject(entry.getKey(), singleton(sid), false)
                .isEmpty()) {
              permissionService.deletePermission(sid, entry.getKey());
            }
          });
    }
  }

  private static Map<ObjectIdentity, PermissionSet> getObjectIdentityPermissionSetMap() {
    Map<ObjectIdentity, PermissionSet> entityTypePermissionMap = new HashMap<>();
    entityTypePermissionMap.put(new EntityTypeIdentity("sys_md_Package"), PermissionSet.READ);
    entityTypePermissionMap.put(new EntityTypeIdentity("sys_md_EntityType"), PermissionSet.WRITE);
    entityTypePermissionMap.put(new EntityTypeIdentity("sys_md_Attribute"), PermissionSet.WRITE);
    entityTypePermissionMap.put(new EntityTypeIdentity("sys_Language"), PermissionSet.WRITE);
    entityTypePermissionMap.put(new EntityTypeIdentity("sys_L10nString"), PermissionSet.WRITE);
    entityTypePermissionMap.put(
        new EntityTypeIdentity("sys_dec_DecoratorConfiguration"), PermissionSet.READ);
    entityTypePermissionMap.put(new EntityTypeIdentity(refEntityTypeStatic), PermissionSet.WRITE);
    entityTypePermissionMap.put(new EntityTypeIdentity(entityTypeStatic), PermissionSet.WRITE);
    entityTypePermissionMap.put(new EntityTypeIdentity(entityTypeDynamic), PermissionSet.WRITE);
    entityTypePermissionMap.put(new EntityTypeIdentity(refEntityTypeDynamic), PermissionSet.WRITE);
    entityTypePermissionMap.put(new EntityTypeIdentity(selfXrefEntityType), PermissionSet.WRITE);
    return entityTypePermissionMap;
  }
}
