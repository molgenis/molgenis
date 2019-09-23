package org.molgenis.data.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;

class EntityListenersServiceTest {
  private EntityListenersService entityListenersService = new EntityListenersService();

  @Test
  void registerNullPointer() {
    assertThrows(NullPointerException.class, () -> entityListenersService.register(null));
  }

  @Test
  void registerTest() {
    String repoFullName = "EntityRepo";
    entityListenersService.register(repoFullName);
    EntityListener entityListener1 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    EntityListener entityListener2 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(2).getMock();
    entityListenersService.addEntityListener(repoFullName, entityListener1);
    entityListenersService.addEntityListener(repoFullName, entityListener2);
    assertFalse(entityListenersService.isEmpty(repoFullName));
    entityListenersService.register(repoFullName);
    assertFalse(entityListenersService.isEmpty(repoFullName));
    entityListenersService.removeEntityListener(repoFullName, entityListener1);
    entityListenersService.removeEntityListener(repoFullName, entityListener2);
    assertTrue(entityListenersService.isEmpty(repoFullName));
  }

  @Test
  void updateEntitiesTest() {
    String repoFullName = "EntityRepo";
    Entity entity1 = Mockito.mock(Entity.class);
    Entity entity2 = Mockito.mock(Entity.class);
    entityListenersService.register(repoFullName);
    EntityListener entityListener1 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    EntityListener entityListener2 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(2).getMock();
    Mockito.when(entity1.getIdValue()).thenReturn(1).getMock();
    Mockito.when(entity2.getIdValue()).thenReturn(2).getMock();
    entityListenersService.addEntityListener(repoFullName, entityListener1);
    entityListenersService.addEntityListener(repoFullName, entityListener2);
    entityListenersService
        .updateEntities(repoFullName, Stream.of(entity1, entity2))
        .collect(Collectors.toList());
    Mockito.verify(entityListener1).postUpdate(entity1);
    Mockito.verify(entityListener2).postUpdate(entity2);
    entityListenersService.removeEntityListener(repoFullName, entityListener1);
    entityListenersService.removeEntityListener(repoFullName, entityListener2);
    assertTrue(entityListenersService.isEmpty(repoFullName));
  }

  @Test
  void updateEntityTest() {
    String repoFullName = "EntityRepo";
    Entity entity = Mockito.mock(Entity.class);
    EntityListener entityListener1 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    EntityListener entityListener2 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    Mockito.when(entity.getIdValue()).thenReturn(1).getMock();
    entityListenersService.register(repoFullName);
    entityListenersService.addEntityListener(repoFullName, entityListener1);
    entityListenersService.addEntityListener(repoFullName, entityListener2);
    entityListenersService.updateEntity(repoFullName, entity);
    Mockito.verify(entityListener1).postUpdate(entity);
    Mockito.verify(entityListener2).postUpdate(entity);
    entityListenersService.removeEntityListener(repoFullName, entityListener1);
    entityListenersService.removeEntityListener(repoFullName, entityListener2);
    assertTrue(entityListenersService.isEmpty(repoFullName));
  }

  @Test
  void addEntityListenerTest() {
    String repoFullName = "EntityRepo";
    EntityListener entityListener =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    entityListenersService.register(repoFullName);
    entityListenersService.addEntityListener(repoFullName, entityListener);
    Mockito.verify(entityListener).getEntityId();
    entityListenersService.removeEntityListener(repoFullName, entityListener);
    assertTrue(entityListenersService.isEmpty(repoFullName));
  }

  @Test
  void removeEntityListenerTest() {
    String repoFullName = "EntityRepo";
    EntityListener entityListener =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    entityListenersService.register(repoFullName);
    entityListenersService.addEntityListener(repoFullName, entityListener);
    assertFalse(entityListenersService.isEmpty(repoFullName));
    entityListenersService.removeEntityListener(repoFullName, entityListener);
    assertTrue(entityListenersService.isEmpty(repoFullName));
  }

  @Test
  void isEmptyTest() {
    String repoFullName = "EntityRepo";
    entityListenersService.register(repoFullName);
    assertTrue(entityListenersService.isEmpty(repoFullName));
  }

  @Test
  void verifyRepoRegistered() {
    this.entityListenersService = new EntityListenersService();
    String repoFullName = "EntityRepo";
    EntityListener entityListener =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    try {
      entityListenersService.addEntityListener(repoFullName, entityListener);
    } catch (MolgenisDataException mde) {
      entityListenersService.register(repoFullName);
      assertTrue(entityListenersService.isEmpty(repoFullName));
      assertEquals(
          "Repository [EntityRepo] is not registered, please contact your administrator",
          mde.getMessage());
      return;
    }
    fail();
  }

  @Test
  void noExceptionsStressTest() {
    List<Thread> ts = new ArrayList<>();
    ts.add(new NewThread("EntityRepo1", 0, 10).getThread());
    ts.add(new NewThread("EntityRepo1", 0, 10).getThread());
    ts.add(new NewThread("EntityRepo2", 0, 10).getThread());
    ts.add(new NewThread("EntityRepo2", 0, 10).getThread());

    ts.forEach(
        t -> {
          try {
            t.join();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        });

    ts.forEach(t -> assertFalse(t.isAlive()));

    assertTrue(entityListenersService.isEmpty("EntityRepo1"));
    assertTrue(entityListenersService.isEmpty("EntityRepo2"));
  }

  private class NewThread implements Runnable {
    String repoFullName;
    int minIdRange;
    int maxIdRange;
    Thread t;

    NewThread(String repoFullName, int minIdRange, int maxIdRange) {
      this.repoFullName = repoFullName;
      if (maxIdRange <= minIdRange) throw new RuntimeException("max must be larger than min");
      this.minIdRange = minIdRange;
      this.maxIdRange = maxIdRange;
      t = new Thread(this);
      t.start();
    }

    Thread getThread() {
      return t;
    }

    public void run() {
      for (int i = minIdRange; i < maxIdRange; i++) {
        entityListenersService.register(repoFullName);
        EntityListener entityListener =
            Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(i).getMock();
        entityListenersService.addEntityListener(repoFullName, entityListener);
        entityListenersService.removeEntityListener(repoFullName, entityListener);
      }
    }
  }
}
