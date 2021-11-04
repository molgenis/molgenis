//package org.molgenis.data.index;
//
//import static com.google.common.collect.Lists.newArrayList;
//import static java.util.Collections.singleton;
//import static java.util.stream.Collectors.toList;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.mockito.Mockito.verifyZeroInteractions;
//import static org.mockito.Mockito.when;
//import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
//import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.PENDING;
//import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
//
//import java.util.stream.Stream;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.Mock;
//import org.molgenis.data.DataService;
//import org.molgenis.data.Entity;
//import org.molgenis.data.EntityKey;
//import org.molgenis.data.Query;
//import org.molgenis.data.index.meta.IndexAction;
//import org.molgenis.data.index.meta.IndexActionFactory;
//import org.molgenis.data.meta.model.AttributeMetadata;
//import org.molgenis.data.meta.model.EntityType;
//import org.molgenis.data.transaction.TransactionConstants;
//import org.molgenis.test.AbstractMockitoTest;
//import org.springframework.transaction.support.TransactionSynchronizationManager;
//
//class IndexActionRegisterServiceTest extends AbstractMockitoTest {
//  private IndexActionRegisterServiceImpl indexActionRegisterServiceImpl;
//  @Mock private IndexActionFactory indexActionFactory;
//  @Mock private IndexAction indexAction;
//  @Mock private DataService dataService;
//  @Captor private ArgumentCaptor<Stream<IndexAction>> indexActionStreamCaptor;
//
//  @BeforeEach
//  void beforeMethod() {
//    TransactionSynchronizationManager.bindResource(
//        TransactionConstants.TRANSACTION_ID_RESOURCE_NAME, "1");
//    indexActionRegisterServiceImpl =
//        new IndexActionRegisterServiceImpl(
//            dataService, indexActionFactory, new IndexingStrategy());
//  }
//
//  @AfterEach
//  void afterMethod() {
//    TransactionSynchronizationManager.unbindResource(
//        TransactionConstants.TRANSACTION_ID_RESOURCE_NAME);
//  }
//
//  @SuppressWarnings("unchecked")
//  @Test
//  void testRegisterCreateSingleEntityNoReferences() {
//    when(indexActionGroupFactory.create("1")).thenReturn(indexActionGroup);
//    when(indexActionGroup.setCount(1)).thenReturn(indexActionGroup);
//
//    when(indexActionFactory.create()).thenReturn(indexAction);
//    when(indexAction.setTransactionId(indexActionGroup)).thenReturn(indexAction);
//    when(indexAction.setEntityTypeId("entityTypeId")).thenReturn(indexAction);
//    when(indexAction.setEntityId("123")).thenReturn(indexAction);
//    when(indexAction.setActionOrder(0)).thenReturn(indexAction);
//    when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);
//    EntityType entityType = mock(EntityType.class);
//    when(entityType.getId()).thenReturn("entityTypeId");
//
//    indexActionRegisterServiceImpl.register(entityType, 123);
//
//    verifyZeroInteractions(dataService);
//
//    Query<Entity> refEntityQuery = mock(Query.class);
//    when(refEntityQuery.count()).thenReturn(0L);
//    when(refEntityQuery.in(AttributeMetadata.REF_ENTITY_TYPE, singleton("entityTypeId")))
//        .thenReturn(refEntityQuery);
//    doReturn(refEntityQuery).when(dataService).query(ATTRIBUTE_META_DATA);
//    indexActionRegisterServiceImpl.storeIndexActions("1");
//
//    verify(dataService).add(INDEX_ACTION_GROUP, indexActionGroup);
//    verify(dataService).add(eq(INDEX_ACTION), indexActionStreamCaptor.capture());
//    assertEquals(newArrayList(indexAction), indexActionStreamCaptor.getValue().collect(toList()));
//  }
//
//  @Test
//  void testRegisterAndForget() {
//    EntityType entityType = mock(EntityType.class);
//    when(entityType.getId()).thenReturn("entityTypeId");
//    indexActionRegisterServiceImpl.register(entityType, 123);
//
//    verifyZeroInteractions(dataService);
//
//    indexActionRegisterServiceImpl.forgetIndexActions("1");
//
//    verifyZeroInteractions(dataService);
//
//    indexActionRegisterServiceImpl.storeIndexActions("1");
//
//    verifyZeroInteractions(dataService);
//  }
//
//  @Test
//  void testRegisterExcludedEntities() {
//    EntityType entityType = mock(EntityType.class);
//    when(entityType.getId()).thenReturn("entityTypeId");
//    indexActionRegisterServiceImpl.addExcludedEntity("ABC");
//
//    indexActionRegisterServiceImpl.register(entityType, 123);
//    verifyNoMoreInteractions(dataService);
//  }
//
//  @Test
//  void isEntityDirtyTrue() {
//    String entityTypeId = "myEntityTypeId";
//    int entityId = 123;
//    EntityType entityType = mock(EntityType.class);
//    when(entityType.getId()).thenReturn(entityTypeId);
//    indexActionRegisterServiceImpl.register(entityType, entityId);
//    EntityKey entityKey = EntityKey.create(entityTypeId, entityId);
//    assertTrue(indexActionRegisterServiceImpl.isEntityDirty(entityKey));
//  }
//
//  @Test
//  void isEntityDirtyFalse() {
//    String entityTypeId = "myEntityTypeId";
//    String entityId = "id";
//    String otherId = "otherID";
//    EntityType entityType = mock(EntityType.class);
//    when(entityType.getId()).thenReturn(entityTypeId);
//    indexActionRegisterServiceImpl.register(entityType, entityId);
//    EntityKey entityKey = EntityKey.create(entityTypeId, otherId);
//    assertFalse(indexActionRegisterServiceImpl.isEntityDirty(entityKey));
//  }
//}
