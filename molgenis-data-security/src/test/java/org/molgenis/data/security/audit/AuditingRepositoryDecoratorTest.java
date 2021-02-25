package org.molgenis.data.security.audit;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.audit.AuditingRepositoryDecorator.ALL_ENTITIES_DELETED;
import static org.molgenis.data.security.audit.AuditingRepositoryDecorator.ENTITIES_AGGREGATED;
import static org.molgenis.data.security.audit.AuditingRepositoryDecorator.ENTITIES_COUNTED;
import static org.molgenis.data.security.audit.AuditingRepositoryDecorator.ENTITIES_READ;
import static org.molgenis.data.security.audit.AuditingRepositoryDecorator.ENTITY_CREATED;
import static org.molgenis.data.security.audit.AuditingRepositoryDecorator.ENTITY_DELETED;
import static org.molgenis.data.security.audit.AuditingRepositoryDecorator.ENTITY_READ;
import static org.molgenis.data.security.audit.AuditingRepositoryDecorator.ENTITY_UPDATED;
import static org.molgenis.data.security.audit.SecurityContextTestUtils.withElevatedUser;
import static org.molgenis.data.security.audit.SecurityContextTestUtils.withSystemToken;
import static org.molgenis.data.security.audit.SecurityContextTestUtils.withUser;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.audit.AuditingRepositoryDecorator.AuditingIterator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class AuditingRepositoryDecoratorTest extends AbstractMockitoTest {

  @Mock private Repository<Entity> repository;
  @Mock private AuditEventPublisher publisher;
  private AuditingRepositoryDecorator decorator;
  private SecurityContext previousContext;

  @BeforeEach
  void setUpBeforeEach() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(testContext);

    decorator = new AuditingRepositoryDecorator(repository, publisher);
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void iterator() {
    withUser("henk");
    onDataEntityType();

    var list = mockTwoEntities();

    when(repository.iterator()).thenReturn(list.iterator());

    decorator
        .iterator()
        .forEachRemaining(
            entity -> {
              // consume
            });

    verify(repository).iterator();
    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id2"));
  }

  @Test
  void iteratorSystem() {
    withUser("henk");
    onSystemEntityType();

    var iterator = decorator.iterator();

    verify(repository).iterator();
    assertFalse(iterator instanceof AuditingIterator);
  }

  @Test
  void iteratorAsSystem() {
    withSystemToken();

    var iterator = decorator.iterator();

    verify(repository).iterator();
    assertFalse(iterator instanceof AuditingIterator);
  }

  @SuppressWarnings("unchecked")
  @Test
  void forEachBatched() {
    withUser("henk");
    onDataEntityType();

    var fetch = mock(Fetch.class);
    var entities = mockTwoEntities();
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1)).accept(entities);
              return null;
            })
        .when(repository)
        .forEachBatched(eq(fetch), any(), eq(10));
    var consumer = mock(Consumer.class);

    decorator.forEachBatched(fetch, consumer, 10);

    verify(repository).forEachBatched(eq(fetch), any(Consumer.class), eq(10));
    verify(consumer).accept(entities);
    verify(publisher)
        .publish(
            "henk",
            ENTITIES_READ,
            Map.of("entityTypeId", "patients", "entityIds", asList("id1", "id2")));
  }

  @SuppressWarnings("unchecked")
  @Test
  void forEachBatchedSystem() {
    withUser("henk");
    onSystemEntityType();

    var fetch = mock(Fetch.class);
    var consumer = mock(Consumer.class);

    decorator.forEachBatched(fetch, consumer, 10);

    verifyNoInteractions(publisher);
    verify(repository).forEachBatched(fetch, consumer, 10);
  }

  @SuppressWarnings("unchecked")
  @Test
  void forEachBatchedWithSystemToken() {
    withSystemToken();

    var fetch = mock(Fetch.class);
    var consumer = mock(Consumer.class);

    decorator.forEachBatched(fetch, consumer, 10);

    verifyNoInteractions(publisher);
    verify(repository).forEachBatched(fetch, consumer, 10);
  }

  @Test
  void count() {
    withUser("henk");
    onDataEntityType();

    when(repository.count()).thenReturn(1L);

    long result = decorator.count();

    verify(publisher).publish("henk", ENTITIES_COUNTED, Map.of("entityTypeId", "patients"));
    verify(repository).count();
    assertEquals(1L, result);
  }

  @Test
  void countSystem() {
    withUser("henk");
    onSystemEntityType();

    when(repository.count()).thenReturn(1L);

    long result = decorator.count();

    verifyNoInteractions(publisher);
    verify(repository).count();
    assertEquals(1L, result);
  }

  @Test
  void countWithSystemToken() {
    withSystemToken();

    when(repository.count()).thenReturn(1L);

    long result = decorator.count();

    verifyNoInteractions(publisher);
    verify(repository).count();
    assertEquals(1L, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  void countQuery() {
    withUser("henk");
    onDataEntityType();

    var query = mock(Query.class);
    when(repository.count(query)).thenReturn(1L);

    long result = decorator.count(query);

    verify(publisher).publish("henk", ENTITIES_COUNTED, Map.of("entityTypeId", "patients"));
    verify(repository).count(query);
    assertEquals(1L, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  void countQuerySystem() {
    withUser("henk");
    onSystemEntityType();

    var query = mock(Query.class);
    when(repository.count(query)).thenReturn(1L);

    long result = decorator.count(query);

    verifyNoInteractions(publisher);
    verify(repository).count(query);
    assertEquals(1L, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  void countQueryWithSystemToken() {
    withSystemToken();

    var query = mock(Query.class);
    when(repository.count(query)).thenReturn(1L);

    long result = decorator.count(query);

    verifyNoInteractions(publisher);
    verify(repository).count(query);
    assertEquals(1L, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  void findAllQuery() {
    withUser("henk");
    onDataEntityType();

    var entities = mockTwoEntities();
    var query = mock(Query.class);
    when(repository.findAll(query)).thenReturn(entities.stream());

    decorator
        .findAll(query)
        .forEach(
            entity -> {
              // om nom nom
            });

    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id2"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void findAllQuerySystem() {
    withUser("henk");
    onSystemEntityType();

    var query = mock(Query.class);

    decorator.findAll(query);

    verify(repository).findAll(query);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void findAllQueryAsSystem() {
    withSystemToken();

    var query = mock(Query.class);

    decorator.findAll(query);

    verify(repository).findAll(query);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings({"unchecked"})
  @Test
  void findOne() {
    withUser("henk");
    onDataEntityType();

    var query = mock(Query.class);
    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");
    when(repository.findOne(query)).thenReturn(entity);

    decorator.findOne(query);

    verify(repository).findOne(query);
    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void findOneSystem() {
    withUser("henk");
    onSystemEntityType();

    var query = mock(Query.class);

    decorator.findOne(query);

    verify(repository).findOne(query);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void findOneAsSystem() {
    withSystemToken();

    var query = mock(Query.class);

    decorator.findOne(query);

    verify(repository).findOne(query);
    verifyNoInteractions(publisher);
  }

  @Test
  void aggregate() {
    withUser("henk");
    onDataEntityType();

    var aggregateQuery = mock(AggregateQuery.class);

    decorator.aggregate(aggregateQuery);

    verify(repository).aggregate(aggregateQuery);
    verify(publisher).publish("henk", ENTITIES_AGGREGATED, Map.of("entityTypeId", "patients"));
  }

  @Test
  void aggregateSystem() {
    withUser("henk");
    onSystemEntityType();

    var aggregateQuery = mock(AggregateQuery.class);

    decorator.aggregate(aggregateQuery);

    verify(repository).aggregate(aggregateQuery);
    verifyNoInteractions(publisher);
  }

  @Test
  void aggregateAsSystem() {
    withSystemToken();

    var aggregateQuery = mock(AggregateQuery.class);

    decorator.aggregate(aggregateQuery);

    verify(repository).aggregate(aggregateQuery);
    verifyNoInteractions(publisher);
  }

  @Test
  void findOneById() {
    withUser("henk");
    onDataEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");
    when(repository.findOneById("id")).thenReturn(entity);

    decorator.findOneById("id");

    verify(repository).findOneById("id");
    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  void findOneByIdSystem() {
    withUser("henk");
    onSystemEntityType();

    decorator.findOneById("id");

    verify(repository).findOneById("id");
    verifyNoInteractions(publisher);
  }

  @Test
  void findOneByIdAsSystem() {
    withSystemToken();

    decorator.findOneById("id");

    verify(repository).findOneById("id");
    verifyNoInteractions(publisher);
  }

  @Test
  void findOneByIdFetch() {
    withUser("henk");
    onDataEntityType();

    var entity = mock(Entity.class);
    var fetch = mock(Fetch.class);
    when(entity.getIdValue()).thenReturn("id");
    when(repository.findOneById("id", fetch)).thenReturn(entity);

    decorator.findOneById("id", fetch);

    verify(repository).findOneById("id", fetch);
    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  void findOneByIdFetchSystem() {
    withUser("henk");
    onSystemEntityType();

    var fetch = mock(Fetch.class);

    decorator.findOneById("id", fetch);

    verify(repository).findOneById("id", fetch);
    verifyNoInteractions(publisher);
  }

  @Test
  void findOneByIdAsFetchSystem() {
    withSystemToken();

    var fetch = mock(Fetch.class);

    decorator.findOneById("id", fetch);

    verify(repository).findOneById("id", fetch);
    verifyNoInteractions(publisher);
  }

  @Test
  void findAllStream() {
    withUser("henk");
    onDataEntityType();

    var entities = mockTwoEntities();
    Stream<Object> entityIds = Stream.of("id1", "id2");
    when(repository.findAll(entityIds)).thenReturn(entities.stream());

    decorator
        .findAll(entityIds)
        .forEach(
            entity -> {
              // om nom nom
            });

    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id2"));
  }

  @Test
  void findAllStreamSystem() {
    withUser("henk");
    onSystemEntityType();

    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.findAll(entityIds);

    verify(repository).findAll(entityIds);
    verifyNoInteractions(publisher);
  }

  @Test
  void findAllStreamAsSystem() {
    withSystemToken();

    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.findAll(entityIds);

    verify(repository).findAll(entityIds);
    verifyNoInteractions(publisher);
  }

  @Test
  void findAllStreamFetch() {
    withUser("henk");
    onDataEntityType();

    var fetch = mock(Fetch.class);
    var entities = mockTwoEntities();
    Stream<Object> entityIds = Stream.of("id1", "id2");
    when(repository.findAll(entityIds, fetch)).thenReturn(entities.stream());

    decorator
        .findAll(entityIds, fetch)
        .forEach(
            entity -> {
              // om nom nom
            });

    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_READ, Map.of("entityTypeId", "patients", "entityId", "id2"));
  }

  @Test
  void findAllStreamFetchSystem() {
    withUser("henk");
    onSystemEntityType();

    var fetch = mock(Fetch.class);
    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.findAll(entityIds, fetch);

    verify(repository).findAll(entityIds, fetch);
    verifyNoInteractions(publisher);
  }

  @Test
  void findAllStreamFetchAsSystem() {
    withSystemToken();

    var fetch = mock(Fetch.class);
    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.findAll(entityIds, fetch);

    verify(repository).findAll(entityIds, fetch);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void deleteAllStream() {
    withUser("henk");
    onDataEntityType();

    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.deleteAll(entityIds);

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).deleteAll(captor.capture());
    captor
        .getValue()
        .forEach(
            (id) -> {
              // consume
            });
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "patients", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "patients", "entityId", "id2"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void deleteAllStreamSystem() {
    withUser("henk");
    onSystemEntityType();

    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.deleteAll(entityIds);

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).deleteAll(captor.capture());
    captor
        .getValue()
        .forEach(
            (id) -> {
              // consume
            });
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "sys", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "sys", "entityId", "id2"));
  }

  @Test
  void deleteAllStreamAsSystem() {
    withSystemToken();

    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.deleteAll(entityIds);

    verify(repository).deleteAll(entityIds);
    verifyNoInteractions(publisher);
  }

  @Test
  void add() {
    withUser("henk");
    onDataEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.add(entity);

    verify(repository).add(entity);
    verify(publisher)
        .publish("henk", ENTITY_CREATED, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  void addSystem() {
    withUser("henk");
    onSystemEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.add(entity);

    verify(repository).add(entity);
    verify(publisher)
        .publish("henk", ENTITY_CREATED, Map.of("entityTypeId", "sys", "entityId", "id"));
  }

  @Test
  void addAsSystem() {
    withSystemToken();

    var entity = mock(Entity.class);

    decorator.add(entity);

    verify(repository).add(entity);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void addStream() {
    withUser("henk");
    onDataEntityType();

    var entities = mockTwoEntities();

    decorator.add(entities.stream());

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).add(captor.capture());
    captor
        .getValue()
        .forEach(
            (id) -> {
              // consume
            });
    verify(publisher)
        .publish("henk", ENTITY_CREATED, Map.of("entityTypeId", "patients", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_CREATED, Map.of("entityTypeId", "patients", "entityId", "id2"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void addStreamSystem() {
    withUser("henk");
    onSystemEntityType();

    var entities = mockTwoEntities();

    decorator.add(entities.stream());

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).add(captor.capture());
    captor
        .getValue()
        .forEach(
            (id) -> {
              // consume
            });
    verify(publisher)
        .publish("henk", ENTITY_CREATED, Map.of("entityTypeId", "sys", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_CREATED, Map.of("entityTypeId", "sys", "entityId", "id2"));
  }

  @Test
  void addStreamAsSystem() {
    withSystemToken();

    var entities = Stream.of(mock(Entity.class), mock(Entity.class));

    decorator.add(entities);

    verify(repository).add(entities);
    verifyNoInteractions(publisher);
  }

  @Test
  void update() {
    withUser("henk");
    onDataEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.update(entity);

    verify(repository).update(entity);
    verify(publisher)
        .publish("henk", ENTITY_UPDATED, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  void updateSystem() {
    withUser("henk");
    onSystemEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.update(entity);

    verify(repository).update(entity);
    verify(publisher)
        .publish("henk", ENTITY_UPDATED, Map.of("entityTypeId", "sys", "entityId", "id"));
  }

  @Test
  void updateAsSystem() {
    withSystemToken();

    var entity = mock(Entity.class);

    decorator.update(entity);

    verify(repository).update(entity);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateStream() {
    withUser("henk");
    onDataEntityType();

    var entities = mockTwoEntities();

    decorator.update(entities.stream());

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).update(captor.capture());
    captor
        .getValue()
        .forEach(
            (id) -> {
              // consume
            });
    verify(publisher)
        .publish("henk", ENTITY_UPDATED, Map.of("entityTypeId", "patients", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_UPDATED, Map.of("entityTypeId", "patients", "entityId", "id2"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateStreamSystem() {
    withUser("henk");
    onSystemEntityType();

    var entities = mockTwoEntities();

    decorator.update(entities.stream());

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).update(captor.capture());
    captor
        .getValue()
        .forEach(
            (id) -> {
              // consume
            });
    verify(publisher)
        .publish("henk", ENTITY_UPDATED, Map.of("entityTypeId", "sys", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_UPDATED, Map.of("entityTypeId", "sys", "entityId", "id2"));
  }

  @Test
  void updateStreamAsSystem() {
    withSystemToken();

    var entities = Stream.of(mock(Entity.class), mock(Entity.class));

    decorator.update(entities);

    verify(repository).update(entities);
    verifyNoInteractions(publisher);
  }

  @Test
  void delete() {
    withUser("henk");
    onDataEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.delete(entity);

    verify(repository).delete(entity);
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  void deleteSystem() {
    withUser("henk");
    onSystemEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.delete(entity);

    verify(repository).delete(entity);
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "sys", "entityId", "id"));
  }

  @Test
  void deleteAsSystem() {
    withSystemToken();

    var entity = mock(Entity.class);

    decorator.delete(entity);

    verify(repository).delete(entity);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void deleteStream() {
    withUser("henk");
    onDataEntityType();

    var entities = mockTwoEntities().stream();

    decorator.delete(entities);

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).delete(captor.capture());
    captor
        .getValue()
        .forEach(
            (id) -> {
              // consume
            });
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "patients", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "patients", "entityId", "id2"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void deleteStreamSystem() {
    withUser("henk");
    onSystemEntityType();

    var entities = mockTwoEntities().stream();

    decorator.delete(entities);

    var captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).delete(captor.capture());
    captor
        .getValue()
        .forEach(
            (id) -> {
              // consume
            });
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "sys", "entityId", "id1"));
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "sys", "entityId", "id2"));
  }

  @Test
  void deleteStreamAsSystem() {
    withSystemToken();

    var entities = Stream.of(mock(Entity.class), mock(Entity.class));

    decorator.delete(entities);

    verify(repository).delete(entities);
    verifyNoInteractions(publisher);
  }

  @Test
  void deleteAll() {
    withUser("henk");
    onDataEntityType();

    decorator.deleteAll();

    verify(repository).deleteAll();
    verify(publisher).publish("henk", ALL_ENTITIES_DELETED, Map.of("entityTypeId", "patients"));
  }

  @Test
  void deleteAllSystem() {
    withUser("henk");
    onSystemEntityType();

    decorator.deleteAll();

    verify(repository).deleteAll();
    verify(publisher).publish("henk", ALL_ENTITIES_DELETED, Map.of("entityTypeId", "sys"));
  }

  @Test
  void deleteAllAsSystem() {
    withSystemToken();

    decorator.deleteAll();

    verify(repository).deleteAll();
    verifyNoInteractions(publisher);
  }

  @Test
  void deleteById() {
    withUser("henk");
    onDataEntityType();

    decorator.deleteById("id");

    verify(repository).deleteById("id");
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  void deleteByIdSystem() {
    withUser("henk");
    onSystemEntityType();

    decorator.deleteById("id");

    verify(repository).deleteById("id");
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "sys", "entityId", "id"));
  }

  @Test
  void deleteByIdAsSystem() {
    withSystemToken();

    decorator.deleteById("id");

    verify(repository).deleteById("id");
    verifyNoInteractions(publisher);
  }

  @Test
  public void auditWithElevatedUser() {
    withElevatedUser("henk");
    onDataEntityType();

    decorator.deleteById("id");

    verify(publisher)
        .publish(
            "henk",
            ENTITY_DELETED,
            Map.of("entityTypeId", "patients", "entityId", "id", "runAs", "SYSTEM"));
  }

  private void onSystemEntityType() {
    var entityType = mock(EntityType.class);
    var pack = mock(Package.class);
    lenient().when(repository.getEntityType()).thenReturn(entityType);
    lenient().when(entityType.getPackage()).thenReturn(pack);
    lenient().when(entityType.getId()).thenReturn("sys");
    lenient().when(pack.getId()).thenReturn(PACKAGE_SYSTEM);
  }

  private void onDataEntityType() {
    var entityType = mock(EntityType.class);
    var pack = mock(Package.class);
    when(repository.getEntityType()).thenReturn(entityType);
    lenient().when(entityType.getPackage()).thenReturn(pack);
    lenient().when(entityType.getId()).thenReturn("patients");
    lenient().when(pack.getId()).thenReturn("cohort");
  }

  private List<Entity> mockTwoEntities() {
    var entity1 = mock(Entity.class);
    var entity2 = mock(Entity.class);
    when(entity1.getIdValue()).thenReturn("id1");
    when(entity2.getIdValue()).thenReturn("id2");
    return asList(entity1, entity2);
  }
}
