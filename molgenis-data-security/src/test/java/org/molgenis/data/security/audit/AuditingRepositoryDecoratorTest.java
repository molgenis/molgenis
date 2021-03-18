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
import org.molgenis.security.core.WithMockSystemUser;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = AuditingRepositoryDecoratorTest.Config.class)
class AuditingRepositoryDecoratorTest extends AbstractMockitoSpringContextTests {

  @Configuration
  static class Config {}

  @Mock private Repository<Entity> repository;
  @Mock private AuditEventPublisher publisher;
  private AuditingRepositoryDecorator decorator;
  private SecurityContext previousContext;

  @BeforeEach
  void setUpBeforeEach() {
    previousContext = SecurityContextHolder.getContext();
    decorator = new AuditingRepositoryDecorator(repository, publisher);
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  @WithMockUser("henk")
  void iterator() {
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
  @WithMockUser("henk")
  void iteratorSystem() {
    onSystemEntityType();

    var iterator = decorator.iterator();

    verify(repository).iterator();
    assertFalse(iterator instanceof AuditingIterator);
  }

  @Test
  @WithMockSystemUser()
  void iteratorAsSystem() {
    var iterator = decorator.iterator();

    verify(repository).iterator();
    assertFalse(iterator instanceof AuditingIterator);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void forEachBatched() {
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
  @WithMockUser("henk")
  void forEachBatchedSystem() {
    onSystemEntityType();

    var fetch = mock(Fetch.class);
    var consumer = mock(Consumer.class);

    decorator.forEachBatched(fetch, consumer, 10);

    verifyNoInteractions(publisher);
    verify(repository).forEachBatched(fetch, consumer, 10);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockSystemUser
  void forEachBatchedWithSystemToken() {
    var fetch = mock(Fetch.class);
    var consumer = mock(Consumer.class);

    decorator.forEachBatched(fetch, consumer, 10);

    verifyNoInteractions(publisher);
    verify(repository).forEachBatched(fetch, consumer, 10);
  }

  @Test
  @WithMockUser("henk")
  void count() {
    onDataEntityType();

    when(repository.count()).thenReturn(1L);

    long result = decorator.count();

    verify(publisher).publish("henk", ENTITIES_COUNTED, Map.of("entityTypeId", "patients"));
    verify(repository).count();
    assertEquals(1L, result);
  }

  @Test
  @WithMockUser("henk")
  void countSystem() {
    onSystemEntityType();

    when(repository.count()).thenReturn(1L);

    long result = decorator.count();

    verifyNoInteractions(publisher);
    verify(repository).count();
    assertEquals(1L, result);
  }

  @Test
  @WithMockSystemUser()
  void countWithSystemToken() {
    when(repository.count()).thenReturn(1L);

    long result = decorator.count();

    verifyNoInteractions(publisher);
    verify(repository).count();
    assertEquals(1L, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void countQuery() {
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
  @WithMockUser("henk")
  void countQuerySystem() {
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
  @WithMockSystemUser
  void countQueryWithSystemToken() {
    var query = mock(Query.class);
    when(repository.count(query)).thenReturn(1L);

    long result = decorator.count(query);

    verifyNoInteractions(publisher);
    verify(repository).count(query);
    assertEquals(1L, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void findAllQuery() {
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

    verify(publisher).publish("henk", ENTITIES_READ, Map.of("entityTypeId", "patients"));
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void findAllQuerySystem() {
    onSystemEntityType();

    var query = mock(Query.class);

    decorator.findAll(query);

    verify(repository).findAll(query);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockSystemUser
  void findAllQueryAsSystem() {
    var query = mock(Query.class);

    decorator.findAll(query);

    verify(repository).findAll(query);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings({"unchecked"})
  @Test
  @WithMockUser("henk")
  void findOne() {
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
  @WithMockUser("henk")
  void findOneSystem() {
    onSystemEntityType();

    var query = mock(Query.class);

    decorator.findOne(query);

    verify(repository).findOne(query);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockSystemUser
  void findOneAsSystem() {
    var query = mock(Query.class);

    decorator.findOne(query);

    verify(repository).findOne(query);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void aggregate() {
    onDataEntityType();

    var aggregateQuery = mock(AggregateQuery.class);

    decorator.aggregate(aggregateQuery);

    verify(repository).aggregate(aggregateQuery);
    verify(publisher).publish("henk", ENTITIES_AGGREGATED, Map.of("entityTypeId", "patients"));
  }

  @Test
  @WithMockUser("henk")
  void aggregateSystem() {
    onSystemEntityType();

    var aggregateQuery = mock(AggregateQuery.class);

    decorator.aggregate(aggregateQuery);

    verify(repository).aggregate(aggregateQuery);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockSystemUser
  void aggregateAsSystem() {
    var aggregateQuery = mock(AggregateQuery.class);

    decorator.aggregate(aggregateQuery);

    verify(repository).aggregate(aggregateQuery);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void findOneById() {
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
  @WithMockUser("henk")
  void findOneByIdSystem() {
    onSystemEntityType();

    decorator.findOneById("id");

    verify(repository).findOneById("id");
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockSystemUser
  void findOneByIdAsSystem() {
    decorator.findOneById("id");

    verify(repository).findOneById("id");
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void findOneByIdFetch() {
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
  @WithMockUser("henk")
  void findOneByIdFetchSystem() {
    onSystemEntityType();

    var fetch = mock(Fetch.class);

    decorator.findOneById("id", fetch);

    verify(repository).findOneById("id", fetch);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockSystemUser
  void findOneByIdAsFetchSystem() {
    var fetch = mock(Fetch.class);

    decorator.findOneById("id", fetch);

    verify(repository).findOneById("id", fetch);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void findAllStream() {
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

    verify(publisher).publish("henk", ENTITIES_READ, Map.of("entityTypeId", "patients"));
  }

  @Test
  @WithMockUser("henk")
  void findAllStreamSystem() {
    onSystemEntityType();

    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.findAll(entityIds);

    verify(repository).findAll(entityIds);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockSystemUser
  void findAllStreamAsSystem() {
    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.findAll(entityIds);

    verify(repository).findAll(entityIds);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void findAllStreamFetch() {
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

    verify(publisher).publish("henk", ENTITIES_READ, Map.of("entityTypeId", "patients"));
  }

  @Test
  @WithMockUser("henk")
  void findAllStreamFetchSystem() {
    onSystemEntityType();

    var fetch = mock(Fetch.class);
    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.findAll(entityIds, fetch);

    verify(repository).findAll(entityIds, fetch);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockSystemUser
  void findAllStreamFetchAsSystem() {
    var fetch = mock(Fetch.class);
    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.findAll(entityIds, fetch);

    verify(repository).findAll(entityIds, fetch);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void deleteAllStream() {
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
  @WithMockUser("henk")
  void deleteAllStreamSystem() {
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
  @WithMockSystemUser
  void deleteAllStreamAsSystem() {
    Stream<Object> entityIds = Stream.of("id1", "id2");

    decorator.deleteAll(entityIds);

    verify(repository).deleteAll(entityIds);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void add() {
    onDataEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.add(entity);

    verify(repository).add(entity);
    verify(publisher)
        .publish("henk", ENTITY_CREATED, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  @WithMockUser("henk")
  void addSystem() {
    onSystemEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.add(entity);

    verify(repository).add(entity);
    verify(publisher)
        .publish("henk", ENTITY_CREATED, Map.of("entityTypeId", "sys", "entityId", "id"));
  }

  @Test
  @WithMockSystemUser
  void addAsSystem() {
    var entity = mock(Entity.class);

    decorator.add(entity);

    verify(repository).add(entity);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void addStream() {
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
  @WithMockUser("henk")
  void addStreamSystem() {
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
  @WithMockSystemUser
  void addStreamAsSystem() {
    var entities = Stream.of(mock(Entity.class), mock(Entity.class));

    decorator.add(entities);

    verify(repository).add(entities);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void update() {
    onDataEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.update(entity);

    verify(repository).update(entity);
    verify(publisher)
        .publish("henk", ENTITY_UPDATED, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  @WithMockUser("henk")
  void updateSystem() {
    onSystemEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.update(entity);

    verify(repository).update(entity);
    verify(publisher)
        .publish("henk", ENTITY_UPDATED, Map.of("entityTypeId", "sys", "entityId", "id"));
  }

  @Test
  @WithMockSystemUser
  void updateAsSystem() {
    var entity = mock(Entity.class);

    decorator.update(entity);

    verify(repository).update(entity);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void updateStream() {
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
  @WithMockUser("henk")
  void updateStreamSystem() {
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
  @WithMockSystemUser
  void updateStreamAsSystem() {
    var entities = Stream.of(mock(Entity.class), mock(Entity.class));

    decorator.update(entities);

    verify(repository).update(entities);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void delete() {
    onDataEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.delete(entity);

    verify(repository).delete(entity);
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  @WithMockUser("henk")
  void deleteSystem() {
    onSystemEntityType();

    var entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("id");

    decorator.delete(entity);

    verify(repository).delete(entity);
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "sys", "entityId", "id"));
  }

  @Test
  @WithMockSystemUser
  void deleteAsSystem() {
    var entity = mock(Entity.class);

    decorator.delete(entity);

    verify(repository).delete(entity);
    verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser("henk")
  void deleteStream() {
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
  @WithMockUser("henk")
  void deleteStreamSystem() {
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
  @WithMockSystemUser
  void deleteStreamAsSystem() {
    var entities = Stream.of(mock(Entity.class), mock(Entity.class));

    decorator.delete(entities);

    verify(repository).delete(entities);
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void deleteAll() {
    onDataEntityType();

    decorator.deleteAll();

    verify(repository).deleteAll();
    verify(publisher).publish("henk", ALL_ENTITIES_DELETED, Map.of("entityTypeId", "patients"));
  }

  @Test
  @WithMockUser("henk")
  void deleteAllSystem() {
    onSystemEntityType();

    decorator.deleteAll();

    verify(repository).deleteAll();
    verify(publisher).publish("henk", ALL_ENTITIES_DELETED, Map.of("entityTypeId", "sys"));
  }

  @Test
  @WithMockSystemUser
  void deleteAllAsSystem() {
    decorator.deleteAll();

    verify(repository).deleteAll();
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockUser("henk")
  void deleteById() {
    onDataEntityType();

    decorator.deleteById("id");

    verify(repository).deleteById("id");
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "patients", "entityId", "id"));
  }

  @Test
  @WithMockUser("henk")
  void deleteByIdSystem() {
    onSystemEntityType();

    decorator.deleteById("id");

    verify(repository).deleteById("id");
    verify(publisher)
        .publish("henk", ENTITY_DELETED, Map.of("entityTypeId", "sys", "entityId", "id"));
  }

  @Test
  @WithMockSystemUser
  void deleteByIdAsSystem() {
    decorator.deleteById("id");

    verify(repository).deleteById("id");
    verifyNoInteractions(publisher);
  }

  @Test
  @WithMockSystemUser(originalUsername = "henk")
  void auditWithElevatedUser() {
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
    lenient().when(entity1.getIdValue()).thenReturn("id1");
    lenient().when(entity2.getIdValue()).thenReturn("id2");
    return asList(entity1, entity2);
  }
}
