package org.molgenis.data.listeners;

import static java.lang.Integer.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;

class EntityListenerRepositoryDecoratorTest {
  private Repository<Entity> delegateRepository;
  private EntityListenerRepositoryDecorator entityListenerRepositoryDecorator;
  private EntityListenersService entityListenersService = new EntityListenersService();

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    delegateRepository = Mockito.mock(Repository.class);
    Mockito.when(delegateRepository.getName()).thenReturn("entityFullName");
    entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(delegateRepository, entityListenersService);
    Mockito.when(entityListenerRepositoryDecorator.getName()).thenReturn("entityFullName");
  }

  @AfterEach
  void afterMethod() {
    entityListenerRepositoryDecorator = null;
  }

  @SuppressWarnings("resource")
  @Test
  void EntityListenerRepositoryDecorator() {
    assertThrows(
        NullPointerException.class,
        () -> new EntityListenerRepositoryDecorator(null, entityListenersService));
  }

  @Test
  void testQuery() {
    assertEquals(
        entityListenerRepositoryDecorator,
        entityListenerRepositoryDecorator.query().getRepository());
  }

  @Test
  void addStream() {
    Stream<Entity> entities = Stream.empty();
    Mockito.when(delegateRepository.add(entities)).thenReturn(123);
    assertEquals(valueOf(123), entityListenerRepositoryDecorator.add(entities));
  }

  @Test
  void deleteStream() {
    Stream<Entity> entities = Stream.empty();
    entityListenerRepositoryDecorator.delete(entities);
    Mockito.verify(delegateRepository, Mockito.times(1)).delete(entities);
  }

  @SuppressWarnings("resource")
  @Test
  void updateEntityWithListener() {
    @SuppressWarnings("unchecked")
    Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
    Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
    EntityListenerRepositoryDecorator entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);
    EntityListener entityListener0 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    entityListenersService.addEntityListener("entityFullName", entityListener0);

    Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
    entityListenerRepositoryDecorator.update(entity);

    Mockito.verify(decoratedRepository).update(entity);
    Mockito.verify(entityListener0, Mockito.times(1)).postUpdate(entity);
  }

  @SuppressWarnings("resource")
  @Test
  void updateEntityWithListeners() {
    @SuppressWarnings("unchecked")
    Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
    Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
    EntityListenerRepositoryDecorator entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);
    EntityListener entityListener0 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    EntityListener entityListener1 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    entityListenersService.addEntityListener("entityFullName", entityListener0);
    entityListenersService.addEntityListener("entityFullName", entityListener1);

    Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
    entityListenerRepositoryDecorator.update(entity);

    Mockito.verify(decoratedRepository).update(entity);
    Mockito.verify(entityListener0, Mockito.times(1)).postUpdate(entity);
    Mockito.verify(entityListener1, Mockito.times(1)).postUpdate(entity);
  }

  @SuppressWarnings("resource")
  @Test
  void updateEntityWithoutListener() {
    @SuppressWarnings("unchecked")
    Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
    Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
    EntityListenerRepositoryDecorator entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);
    EntityListener entityListener0 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(-1).getMock();
    entityListenersService.addEntityListener("entityFullName", entityListener0);

    Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
    entityListenerRepositoryDecorator.update(entity);

    Mockito.verify(decoratedRepository).update(entity);
    Mockito.verify(entityListener0, Mockito.times(0)).postUpdate(entity);
  }

  @SuppressWarnings("resource")
  @Test
  void updateEntityNoListeners() {
    @SuppressWarnings("unchecked")
    Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
    Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
    EntityListenerRepositoryDecorator entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);

    Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
    entityListenerRepositoryDecorator.update(entity);

    Mockito.verify(decoratedRepository).update(entity);
  }

  @SuppressWarnings({"resource", "unchecked", "rawtypes"})
  @Test
  void updateStreamWithListeners() {
    Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
    Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
    EntityListenerRepositoryDecorator entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);
    EntityListener entityListener0 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    EntityListener entityListener1 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(2).getMock();
    entityListenersService.addEntityListener("entityFullName", entityListener0);
    entityListenersService.addEntityListener("entityFullName", entityListener1);

    Entity entity0 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
    Entity entity1 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(2).getMock();
    Stream<Entity> entities = Stream.of(entity0, entity1);
    entityListenerRepositoryDecorator.update(entities);

    ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
    Mockito.verify(decoratedRepository).update(captor.capture());
    assertEquals(asList(entity0, entity1), captor.getValue().collect(toList()));

    Mockito.verify(entityListener0, Mockito.times(1)).postUpdate(entity0);
    Mockito.verify(entityListener1, Mockito.times(1)).postUpdate(entity1);
  }

  @SuppressWarnings({"resource", "unchecked", "rawtypes"})
  @Test
  void updateStreamWithSomeListeners() {
    Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
    Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
    EntityListenerRepositoryDecorator entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);
    EntityListener entityListener1 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(2).getMock();
    entityListenersService.addEntityListener("entityFullName", entityListener1);

    Entity entity0 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
    Entity entity1 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(2).getMock();
    Stream<Entity> entities = Stream.of(entity0, entity1);
    entityListenerRepositoryDecorator.update(entities);

    ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
    Mockito.verify(decoratedRepository).update(captor.capture());
    assertEquals(asList(entity0, entity1), captor.getValue().collect(toList()));
    Mockito.verify(entityListener1, Mockito.times(1)).postUpdate(entity1);
  }

  @SuppressWarnings({"resource", "unchecked", "rawtypes"})
  @Test
  void updateStreamNoListeners() {
    Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
    Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
    EntityListenerRepositoryDecorator entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);

    Entity entity0 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
    Entity entity1 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(2).getMock();
    Stream<Entity> entities = Stream.of(entity0, entity1);
    entityListenerRepositoryDecorator.update(entities);

    ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
    Mockito.verify(decoratedRepository, Mockito.times(1)).update(captor.capture());
    assertEquals(asList(entity0, entity1), captor.getValue().collect(toList()));
  }

  @SuppressWarnings("resource")
  @Test
  void removeEntityListener() {
    @SuppressWarnings("unchecked")
    Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
    Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
    EntityListenerRepositoryDecorator entityListenerRepositoryDecorator =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);
    EntityListener entityListener0 =
        Mockito.when(Mockito.mock(EntityListener.class).getEntityId()).thenReturn(1).getMock();
    entityListenersService.addEntityListener("entityFullName", entityListener0);
    entityListenersService.removeEntityListener("entityFullName", entityListener0);

    Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
    entityListenerRepositoryDecorator.update(entity);

    Mockito.verify(decoratedRepository).update(entity);
    Mockito.verify(entityListener0, Mockito.times(0)).postUpdate(entity);
  }

  @Test
  void findAllStream() {
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = Mockito.mock(Entity.class);
    Entity entity1 = Mockito.mock(Entity.class);
    Stream<Object> entityIds = Stream.of(id0, id1);
    Mockito.when(delegateRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
    Stream<Entity> expectedEntities = entityListenerRepositoryDecorator.findAll(entityIds);
    assertEquals(asList(entity0, entity1), expectedEntities.collect(toList()));
  }

  @Test
  void findAllStreamFetch() {
    Fetch fetch = new Fetch();
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = Mockito.mock(Entity.class);
    Entity entity1 = Mockito.mock(Entity.class);
    Stream<Object> entityIds = Stream.of(id0, id1);
    Mockito.when(delegateRepository.findAll(entityIds, fetch))
        .thenReturn(Stream.of(entity0, entity1));
    Stream<Entity> expectedEntities = entityListenerRepositoryDecorator.findAll(entityIds, fetch);
    assertEquals(asList(entity0, entity1), expectedEntities.collect(toList()));
  }

  @Test
  void findAllAsStream() {
    Entity entity0 = Mockito.mock(Entity.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = Mockito.mock(Query.class);
    Mockito.when(delegateRepository.findAll(query)).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = entityListenerRepositoryDecorator.findAll(query);
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }

  @Test
  void streamFetch() {
    Fetch fetch = new Fetch();
    @SuppressWarnings("unchecked")
    Consumer<List<Entity>> consumer = Mockito.mock(Consumer.class);
    entityListenerRepositoryDecorator.forEachBatched(fetch, consumer, 543);
    Mockito.verify(delegateRepository, Mockito.times(1)).forEachBatched(fetch, consumer, 543);
  }
}
