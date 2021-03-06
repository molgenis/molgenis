package org.molgenis.data;

import static java.lang.Integer.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;

class EntityReferenceResolverDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Entity> delegateRepository;
  @Mock private EntityType entityType;
  @Mock private EntityManager entityManager;
  private EntityReferenceResolverDecorator entityReferenceResolverDecorator;
  @Mock private Consumer<List<Entity>> consumer;
  @Captor private ArgumentCaptor<Consumer<List<Entity>>> consumerArgumentCaptor;
  @Captor private ArgumentCaptor<Stream<Entity>> streamArgumentCaptor;

  @BeforeEach
  void setUpBeforeMethod() {
    entityReferenceResolverDecorator =
        new EntityReferenceResolverDecorator(delegateRepository, entityManager);
  }

  @SuppressWarnings("resource")
  @Test
  void EntityReferenceResolverDecorator() {
    assertThrows(
        NullPointerException.class, () -> new EntityReferenceResolverDecorator(null, null));
  }

  @Test
  void addEntity() {
    Entity entity = mock(Entity.class);
    entityReferenceResolverDecorator.add(entity);
    verify(delegateRepository, times(1)).add(entity);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void addStream() {
    Stream<Entity> entities = Stream.empty();
    when(delegateRepository.add(entities)).thenReturn(123);
    assertEquals(valueOf(123), entityReferenceResolverDecorator.add(entities));
  }

  @Test
  void aggregate() {
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    entityReferenceResolverDecorator.aggregate(aggregateQuery);
    verify(delegateRepository, times(1)).aggregate(aggregateQuery);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void close() throws IOException {
    entityReferenceResolverDecorator.close();
    verify(delegateRepository, times(1)).close();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void count() {
    entityReferenceResolverDecorator.count();
    verify(delegateRepository, times(1)).count();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void countQuery() {
    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    entityReferenceResolverDecorator.count(q);
    verify(delegateRepository, times(1)).count(q);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void deleteEntity() {
    Entity entity = mock(Entity.class);
    entityReferenceResolverDecorator.delete(entity);
    verify(delegateRepository, times(1)).delete(entity);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void deleteStream() {
    @SuppressWarnings("unchecked")
    Stream<Entity> entities = mock(Stream.class);
    entityReferenceResolverDecorator.delete(entities);
    verify(delegateRepository, times(1)).delete(entities);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void deleteAll() {
    entityReferenceResolverDecorator.deleteAll();
    verify(delegateRepository, times(1)).deleteAll();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void deleteByIdObject() {
    Object id = mock(Object.class);
    entityReferenceResolverDecorator.deleteById(id);
    verify(delegateRepository, times(1)).deleteById(id);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void deleteByIdIterableObject() {
    @SuppressWarnings("unchecked")
    Iterable<Object> ids = mock(Iterable.class);
    entityReferenceResolverDecorator.deleteById(ids);
    verify(delegateRepository, times(1)).deleteById(ids);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void findAllAsStreamFetch() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Entity entity0 = mock(Entity.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Fetch fetch = mock(Fetch.class);
    when(query.getFetch()).thenReturn(fetch);
    Stream<Entity> entities = Stream.of(entity0);
    when(delegateRepository.findAll(query)).thenReturn(entities);
    when(entityManager.resolveReferences(entityType, entities, fetch)).thenReturn(entities);
    Stream<Entity> expectedEntities = entityReferenceResolverDecorator.findAll(query);
    assertEquals(singletonList(entity0), expectedEntities.collect(toList()));
  }

  @Test
  void findAllAsStreamNoFetch() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Entity entity0 = mock(Entity.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Stream<Entity> entities = Stream.of(entity0);
    when(delegateRepository.findAll(query)).thenReturn(entities);
    when(entityManager.resolveReferences(entityType, entities, null)).thenReturn(entities);
    Stream<Entity> expectedEntities = entityReferenceResolverDecorator.findAll(query);
    assertEquals(singletonList(entity0), expectedEntities.collect(toList()));
  }

  @Test
  void findAllStream() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    Entity entity0WithRefs = mock(Entity.class);
    Entity entity1WithRefs = mock(Entity.class);
    Stream<Object> entityIds = Stream.of(id0, id1);
    Stream<Entity> entities = Stream.of(entity0, entity1);
    when(delegateRepository.findAll(entityIds)).thenReturn(entities);
    when(entityManager.resolveReferences(entityType, entities, null))
        .thenReturn(Stream.of(entity0WithRefs, entity1WithRefs));
    Stream<Entity> expectedEntities = entityReferenceResolverDecorator.findAll(entityIds);
    assertEquals(asList(entity0WithRefs, entity1WithRefs), expectedEntities.collect(toList()));
  }

  @Test
  void findAllStreamFetch() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch();
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    Entity entity0WithRefs = mock(Entity.class);
    Entity entity1WithRefs = mock(Entity.class);
    Stream<Object> entityIds = Stream.of(id0, id1);
    Stream<Entity> entities = Stream.of(entity0, entity1);
    when(delegateRepository.findAll(entityIds, fetch)).thenReturn(entities);
    when(entityManager.resolveReferences(entityType, entities, fetch))
        .thenReturn(Stream.of(entity0WithRefs, entity1WithRefs));
    Stream<Entity> expectedEntities = entityReferenceResolverDecorator.findAll(entityIds, fetch);
    assertEquals(asList(entity0WithRefs, entity1WithRefs), expectedEntities.collect(toList()));
  }

  @Test
  void streamFetch() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch();
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    Entity entity0WithRefs = mock(Entity.class);
    Entity entity1WithRefs = mock(Entity.class);
    List<Entity> entities = Arrays.asList(entity0, entity1);
    List<Entity> entitiesWithRefs = Arrays.asList(entity0WithRefs, entity1WithRefs);

    when(entityManager.resolveReferences(eq(entityType), streamArgumentCaptor.capture(), eq(fetch)))
        .thenReturn(entitiesWithRefs.stream());

    // the test
    entityReferenceResolverDecorator.forEachBatched(fetch, consumer, 123);

    verify(delegateRepository).forEachBatched(eq(fetch), consumerArgumentCaptor.capture(), eq(123));
    consumerArgumentCaptor.getValue().accept(entities);

    Stream<Entity> entitiesToDecorate = streamArgumentCaptor.getValue();

    assertEquals(entities, entitiesToDecorate.collect(toList()));
    verify(consumer).accept(entitiesWithRefs);
  }

  @Test
  void findOneQueryEntity() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch();
    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    when(q.getFetch()).thenReturn(fetch);
    Entity entity = mock(Entity.class);
    when(delegateRepository.findOne(q)).thenReturn(entity);
    entityReferenceResolverDecorator.findOne(q);
    verify(delegateRepository, times(1)).findOne(q);
    verify(entityManager).resolveReferences(entityType, entity, fetch);
  }

  @Test
  void findOneQueryEntityNull() {
    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    entityReferenceResolverDecorator.findOne(q);
    verify(delegateRepository, times(1)).findOne(q);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void findOneObject() {
    Object id = mock(Object.class);
    entityReferenceResolverDecorator.findOneById(id);
    verify(delegateRepository, times(1)).findOneById(id);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void findOneObjectFetch() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object id = 1;
    Fetch fetch = new Fetch();
    Entity entity = mock(Entity.class);
    when(delegateRepository.findOneById(id, fetch)).thenReturn(entity);
    entityReferenceResolverDecorator.findOneById(id, fetch);
    verify(delegateRepository, times(1)).findOneById(id, fetch);
    verify(entityManager).resolveReferences(entityType, entity, fetch);
  }

  @Test
  void findOneObjectFetchNull() {
    Object id = 1;
    entityReferenceResolverDecorator.findOneById(id, null);
    verify(delegateRepository, times(1)).findOneById(id, null);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getCapabilities() {
    entityReferenceResolverDecorator.getCapabilities();
    verify(delegateRepository, times(1)).getCapabilities();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getEntityType() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    entityReferenceResolverDecorator.getEntityType();
    verify(delegateRepository, times(1)).getEntityType();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getName() {
    entityReferenceResolverDecorator.getName();
    verify(delegateRepository, times(1)).getName();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void iterator() {
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    QueryImpl<Entity> q = new QueryImpl<>();
    Stream<Entity> entities = Stream.of(mock(Entity.class));
    when(delegateRepository.findAll(q)).thenReturn(entities);
    when(entityManager.resolveReferences(entityType, entities, null)).thenReturn(entities);
    entityReferenceResolverDecorator.iterator();
    verify(delegateRepository, times(1)).findAll(q);
    verify(entityManager).resolveReferences(entityType, entities, null);
  }

  @Test
  void query() {
    assertEquals(
        entityReferenceResolverDecorator, entityReferenceResolverDecorator.query().getRepository());
    verifyZeroInteractions(entityManager);
  }

  @Test
  void updateEntity() {
    Entity entity = mock(Entity.class);
    entityReferenceResolverDecorator.update(entity);
    verify(delegateRepository, times(1)).update(entity);
    verifyZeroInteractions(entityManager);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void updateStream() {
    Entity entity0 = mock(Entity.class);
    Stream<Entity> entities = Stream.of(entity0);
    ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
    doNothing().when(delegateRepository).update(captor.capture());
    entityReferenceResolverDecorator.update(entities);
    assertEquals(singletonList(entity0), captor.getValue().collect(toList()));
  }
}
