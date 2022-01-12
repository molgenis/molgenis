 package org.molgenis.data.index.job;

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.mockito.Mockito.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.reset;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.openMocks;
 import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.STARTED;

 import java.util.Optional;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoSettings;
 import org.mockito.quality.Strictness;
 import org.molgenis.data.AbstractMolgenisSpringTest;
 import org.molgenis.data.DataService;
 import org.molgenis.data.Entity;
 import org.molgenis.data.EntityTestHarness;
 import org.molgenis.data.TestHarnessConfig;
 import org.molgenis.data.index.IndexService;
 import org.molgenis.data.index.config.IndexTestConfig;
 import org.molgenis.data.index.meta.IndexAction;
 import org.molgenis.data.index.meta.IndexActionFactory;
 import org.molgenis.data.meta.MetaDataService;
 import org.molgenis.data.meta.model.EntityType;
 import org.molgenis.data.meta.model.EntityTypeFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.security.core.Authentication;
 import org.springframework.test.context.ContextConfiguration;

 @MockitoSettings(strictness = Strictness.LENIENT)
 @ContextConfiguration(classes = {IndexJobServiceTest.Config.class})
 class IndexJobServiceTest extends AbstractMolgenisSpringTest {
  @Autowired private IndexService indexService;
  @Autowired private MetaDataService mds;
  @Autowired private Config config;
  @Autowired private DataService dataService;
  @Autowired private EntityTestHarness harness;
  @Autowired private IndexActionFactory indexActionFactory;

  @Autowired private EntityTypeFactory entityTypeFactory;
  private final String transactionId = "aabbcc";
  private IndexJobService indexJobService;
  private EntityType testEntityType;
  private Entity toIndexEntity;

  @BeforeEach
  void beforeMethod() {
    config.resetMocks();
    indexJobService = new IndexJobService(dataService, indexService, entityTypeFactory);
    when(dataService.getMeta()).thenReturn(mds);
    testEntityType = harness.createDynamicRefEntityType();
    when(mds.getEntityType("TypeTestRefDynamic")).thenReturn(Optional.of(testEntityType));
    toIndexEntity = harness.createTestRefEntities(testEntityType, 1).get(0);
    when(dataService.hasEntityType("TypeTestRefDynamic")).thenReturn(true);
    when(dataService.getEntityType("TypeTestRefDynamic")).thenReturn(testEntityType);
    when(dataService.findOneById("TypeTestRefDynamic", "entityId")).thenReturn(toIndexEntity);
    when(dataService.hasEntityType("entityType")).thenReturn(true);
    when(dataService.getEntityType("entityType")).thenReturn(testEntityType);
  }


  @Test
  void rebuildIndexDeleteSingleEntityTest() {
    when(dataService.findOneById("TypeTestRefDynamic", "entityId")).thenReturn(null);

    IndexAction indexAction =
        indexActionFactory
            .create()
            .setTransactionId(transactionId)
            .setEntityTypeId("entityType")
            .setEntityId("entityId")
            .setIndexStatus(STARTED);

    when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);

    indexJobService.performAction(indexAction);

    assertEquals(STARTED, indexAction.getIndexStatus());
    verify(indexService).deleteById(testEntityType, "entityId");
  }

    @Test
  void rebuildIndexCreateSingleEntityTest() {
    IndexAction indexAction =
        indexActionFactory
            .create()
            .setTransactionId(transactionId)
            .setEntityTypeId("entityType")
            .setEntityId("entityId")
            .setIndexStatus(STARTED);
    when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);

    indexJobService.performAction(indexAction);

    assertEquals(STARTED, indexAction.getIndexStatus());
    verify(this.indexService).index(testEntityType, toIndexEntity);
  }

  @Test
  void rebuildIndexMetaUpdateDataTest() {
    when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);
    EntityType entityType = dataService.getEntityType("TypeTestRefDynamic");
    when(indexService.hasIndex(entityType)).thenReturn(true);

    IndexAction indexAction =
        indexActionFactory
            .create()
            .setTransactionId(transactionId)
            .setEntityTypeId("entityType")
            .setEntityId(null)
            .setIndexStatus(STARTED);

    indexJobService.performAction(indexAction);

    assertEquals(STARTED, indexAction.getIndexStatus());
    verify(this.indexService).rebuildIndex(this.dataService.getRepository("any"));
  }

  @Test
  void rebuildIndexMetaCreateDataTest() {
    when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);

    IndexAction indexAction =
        indexActionFactory
            .create()
            .setTransactionId(transactionId)
            .setEntityTypeId("entityType")
            .setEntityId(null)
            .setIndexStatus(STARTED);

    indexJobService.performAction(indexAction);

    assertEquals(STARTED, indexAction.getIndexStatus());
    verify(this.indexService).rebuildIndex(this.dataService.getRepository("any"));
  }

  @Test
  void rebuildIndexDeleteMetaDataEntityTest() {
    String entityTypeId = "entityTypeId";
    String entityTypeLabel = "entityTypeLabel";
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(entityTypeId);
    when(entityType.getLabel()).thenReturn(entityTypeLabel);
    IndexAction indexAction =
        indexActionFactory
            .create()
            .setTransactionId(transactionId)
            .setEntityTypeId(entityTypeId)
            .setEntityId(null)
            .setIndexStatus(STARTED);

    when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(false);
    when(dataService.getEntityType("entityTypeName")).thenReturn(null);

    when(indexService.hasIndex(any(EntityType.class))).thenReturn(true);

    indexJobService.performAction(indexAction);

    assertEquals(STARTED, indexAction.getIndexStatus());
    ArgumentCaptor<EntityType> entityTypeCaptor = ArgumentCaptor.forClass(EntityType.class);
    verify(this.indexService).deleteIndex(entityTypeCaptor.capture());
    EntityType actualEntityType = entityTypeCaptor.getValue();
    assertEquals(entityTypeId, actualEntityType.getId());
  }

  @SuppressWarnings("java:S5979") // mocks are initialized
  @Configuration
  @Import({IndexTestConfig.class, TestHarnessConfig.class})
  static class Config {
    @Mock private Authentication authentication;
    @Mock private IndexService indexService;
    @Mock private MetaDataService mds;

    public Config() {
      openMocks(this);
    }

    @Bean
    public Authentication authentication() {
      return authentication;
    }

    @Bean
    public IndexService indexService() {
      return indexService;
    }

    @Bean
    public MetaDataService metaDataService() {
      return mds;
    }

    void resetMocks() {
      reset(authentication, indexService, mds);
    }
  }
 }
