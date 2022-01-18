package org.molgenis.data.index.queue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.index.IndexActionService;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;
import org.molgenis.test.AbstractMockitoTest;

class RunnableIndexActionTest extends AbstractMockitoTest {
  @Mock private IndexActionService indexActionService;

  @Test
  void testRun() {
    var action = mock(IndexAction.class);
    var runnableAction = new RunnableIndexAction(action, indexActionService);
    when(indexActionService.performAction(action)).thenReturn(true);

    runnableAction.run();

    verify(indexActionService).updateIndexActionStatus(action, IndexStatus.STARTED);
    verify(indexActionService).updateIndexActionStatus(action, IndexStatus.FINISHED);
  }

  @Test
  void testRunFail() {
    var action = mock(IndexAction.class);
    var runnableAction = new RunnableIndexAction(action, indexActionService);
    when(indexActionService.performAction(action)).thenReturn(false);

    runnableAction.run();

    verify(indexActionService).updateIndexActionStatus(action, IndexStatus.STARTED);
    verify(indexActionService).updateIndexActionStatus(action, IndexStatus.FAILED);
  }

  @Test
  void testRunException() {
    var action = mock(IndexAction.class);
    var runnableAction = new RunnableIndexAction(action, indexActionService);
    Exception e = new IllegalArgumentException();
    when(indexActionService.performAction(action)).thenThrow(e);

    assertThrows(IllegalArgumentException.class, runnableAction::run);

    verify(indexActionService).updateIndexActionStatus(action, IndexStatus.STARTED);
    verify(indexActionService).updateIndexActionStatus(action, IndexStatus.FAILED);
  }

  @Test
  void testRunNotPending() {
    var action = mock(IndexAction.class);
    var runnableAction = new RunnableIndexAction(action, indexActionService);

    runnableAction.setStatus(IndexStatus.SKIPPED);
    runnableAction.run();

    verify(indexActionService).updateIndexActionStatus(action, IndexStatus.SKIPPED);
    verifyNoMoreInteractions(indexActionService);
  }

  @Test
  void testSetStatus() {
    var action = mock(IndexAction.class);
    var runnableAction = new RunnableIndexAction(action, indexActionService);

    runnableAction.setStatus(IndexStatus.CANCELED);

    verify(indexActionService).updateIndexActionStatus(action, IndexStatus.CANCELED);
  }

  @Test
  void testContainsSameEntityType(){
    var runnableAction1 = createRunnableIndexAction("entityType1");
    var runnableAction2 = createRunnableIndexAction("entityType1");

    assertTrue(runnableAction1.contains(runnableAction2));
    assertTrue(runnableAction2.contains(runnableAction1));
    assertTrue(runnableAction1.isContainedBy(runnableAction2));
    assertTrue(runnableAction2.isContainedBy(runnableAction1));
  }

  @Test
  void testContainsDifferentEntityType(){
    var runnableAction1 = createRunnableIndexAction("entityType1");
    var runnableAction2 = createRunnableIndexAction("entityType2");

    assertFalse(runnableAction1.contains(runnableAction2));
    assertFalse(runnableAction2.contains(runnableAction1));
    assertFalse(runnableAction1.isContainedBy(runnableAction2));
    assertFalse(runnableAction2.isContainedBy(runnableAction1));
  }

  @Test
  void testContainsEntity(){
    var runnableAction1 = createRunnableIndexAction("entityType1", "entity1");
    var runnableAction2 = createRunnableIndexAction("entityType1");

    assertFalse(runnableAction1.contains(runnableAction2));
    assertTrue(runnableAction2.contains(runnableAction1));
    assertTrue(runnableAction1.isContainedBy(runnableAction2));
    assertFalse(runnableAction2.isContainedBy(runnableAction1));
  }

  @Test
  void testContainsDifferentEntity(){
    var runnableAction1 = createRunnableIndexAction("entityType1", "entity1");
    var runnableAction2 = createRunnableIndexAction("entityType1", "entity2");

    assertFalse(runnableAction1.contains(runnableAction2));
    assertFalse(runnableAction2.contains(runnableAction1));
    assertFalse(runnableAction1.isContainedBy(runnableAction2));
    assertFalse(runnableAction2.isContainedBy(runnableAction1));
  }

  @Test
  void testContainsSameEntity(){
    var runnableAction1 = createRunnableIndexAction("entityType1", "entity1");
    var runnableAction2 = createRunnableIndexAction("entityType1", "entity1");

    assertTrue(runnableAction1.contains(runnableAction2));
    assertTrue(runnableAction2.contains(runnableAction1));
    assertTrue(runnableAction1.isContainedBy(runnableAction2));
    assertTrue(runnableAction2.isContainedBy(runnableAction1));
  }

  @Test
  void testConcerns(){
    var runnableAction = createRunnableIndexAction("entityType1");

    assertTrue(runnableAction.concerns("entityType1"));
    assertFalse(runnableAction.concerns("entityType2"));
  }

  private RunnableIndexAction createRunnableIndexAction(String entityType) {
    return createRunnableIndexAction(entityType, null);
  }

  private RunnableIndexAction createRunnableIndexAction(String entityType, String entityId){
    var action = mock(IndexAction.class);
    when(action.getEntityTypeId()).thenReturn(entityType);
    if (entityId != null){
      when(action.getEntityId()).thenReturn(entityId);
    }
    return new RunnableIndexAction(action, indexActionService);
  }
}