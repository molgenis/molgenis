package org.molgenis.data.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.EntityTypePermission.ADD_DATA;
import static org.molgenis.data.security.EntityTypePermission.AGGREGATE_DATA;
import static org.molgenis.data.security.EntityTypePermission.COUNT_DATA;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;

class RepositorySecurityDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Entity> delegateRepository;
  @Mock private UserPermissionEvaluator permissionService;

  private RepositorySecurityDecorator repositorySecurityDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    repositorySecurityDecorator =
        new RepositorySecurityDecorator(delegateRepository, permissionService);
  }

  @Test
  void testRepositorySecurityDecorator() {
    assertThrows(NullPointerException.class, () -> new RepositorySecurityDecorator(null, null));
  }

  @Test
  void testAddPermissionGranted() {
    initPermissionServiceMock(ADD_DATA, true);
    Entity entity = mock(Entity.class);
    repositorySecurityDecorator.add(entity);
    verify(delegateRepository).add(entity);
  }

  @Test
  void testAddPermissionDenied() {
    initPermissionServiceMock(ADD_DATA, false);
    Entity entity = mock(Entity.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.add(entity));
    assertThat(exception.getMessage())
        .containsPattern("permission:ADD_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testAddStreamPermissionGranted() {
    initPermissionServiceMock(ADD_DATA, true);
    Stream<Entity> entityStream = Stream.empty();
    repositorySecurityDecorator.add(entityStream);
    verify(delegateRepository).add(entityStream);
  }

  @Test
  void testAddStreamPermissionDenied() {
    initPermissionServiceMock(ADD_DATA, false);
    Stream<Entity> entityStream = Stream.empty();
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.add(entityStream));
    assertThat(exception.getMessage())
        .containsPattern("permission:ADD_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testAggregatePermissionGranted() {
    initPermissionServiceMock(AGGREGATE_DATA, true);
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    repositorySecurityDecorator.aggregate(aggregateQuery);
    verify(delegateRepository).aggregate(aggregateQuery);
  }

  @Test
  void testAggregatePermissionDenied() {
    initPermissionServiceMock(AGGREGATE_DATA, false);
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.aggregate(aggregateQuery));
    assertThat(exception.getMessage())
        .containsPattern("permission:AGGREGATE_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testCloseNoPermissionsNeeded() throws IOException {
    repositorySecurityDecorator.close();
    verify(delegateRepository).close();
    verifyZeroInteractions(permissionService);
  }

  @Test
  void testCountPermissionGranted() {
    initPermissionServiceMock(COUNT_DATA, true);
    repositorySecurityDecorator.count();
    verify(delegateRepository).count();
  }

  @Test
  void testCountPermissionDenied() {
    initPermissionServiceMock(COUNT_DATA, false);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class, () -> repositorySecurityDecorator.count());
    assertThat(exception.getMessage())
        .containsPattern("permission:COUNT_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testCountQueryPermissionGranted() {
    initPermissionServiceMock(COUNT_DATA, true);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    repositorySecurityDecorator.count(query);
    verify(delegateRepository).count(query);
  }

  @Test
  void testCountQueryPermissionDenied() {
    initPermissionServiceMock(COUNT_DATA, false);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.count(query));
    assertThat(exception.getMessage())
        .containsPattern("permission:COUNT_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testDeletePermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
    Entity entity = mock(Entity.class);
    repositorySecurityDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
  }

  @Test
  void testDeletePermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
    Entity entity = mock(Entity.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.delete(entity));
    assertThat(exception.getMessage())
        .containsPattern("permission:DELETE_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testDeleteStreamPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
    Stream<Entity> entityStream = Stream.empty();
    repositorySecurityDecorator.delete(entityStream);
    verify(delegateRepository).delete(entityStream);
  }

  @Test
  void testDeleteStreamPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
    Stream<Entity> entityStream = Stream.empty();
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.delete(entityStream));
    assertThat(exception.getMessage())
        .containsPattern("permission:DELETE_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testDeleteAllPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
    repositorySecurityDecorator.deleteAll();
    verify(delegateRepository).deleteAll();
  }

  @Test
  void testDeleteAllPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.deleteAll());
    assertThat(exception.getMessage())
        .containsPattern("permission:DELETE_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testDeleteAllStreamPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
    Stream<Object> entityIdStream = Stream.empty();
    repositorySecurityDecorator.deleteAll(entityIdStream);
    verify(delegateRepository).deleteAll(entityIdStream);
  }

  @Test
  void testDeleteAllStreamPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
    Stream<Object> entityIdStream = Stream.empty();
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.deleteAll(entityIdStream));
    assertThat(exception.getMessage())
        .containsPattern("permission:DELETE_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testDeleteByIdPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, true);
    Object entityId = mock(Object.class);
    repositorySecurityDecorator.deleteById(entityId);
    verify(delegateRepository).deleteById(entityId);
  }

  @Test
  void testDeleteByIdPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.DELETE_DATA, false);
    Object entityId = mock(Object.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.deleteById(entityId));
    assertThat(exception.getMessage())
        .containsPattern("permission:DELETE_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testFindAllQueryPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    repositorySecurityDecorator.findAll(query);
    verify(delegateRepository).findAll(query);
  }

  @Test
  void testFindAllQueryPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.findAll(query));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testFindAllStreamPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
    Stream<Object> entityIdStream = Stream.empty();
    repositorySecurityDecorator.findAll(entityIdStream);
    verify(delegateRepository).findAll(entityIdStream);
  }

  @Test
  void testFindAllStreamPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
    Stream<Object> entityIdStream = Stream.empty();
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.findAll(entityIdStream));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testFindAllStreamFetchPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
    Stream<Object> entityIdStream = Stream.empty();
    Fetch fetch = mock(Fetch.class);
    repositorySecurityDecorator.findAll(entityIdStream, fetch);
    verify(delegateRepository).findAll(entityIdStream, fetch);
  }

  @Test
  void testFindAllStreamFetchPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
    Stream<Object> entityIdStream = Stream.empty();
    Fetch fetch = mock(Fetch.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.findAll(entityIdStream, fetch));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testFindOneQueryPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    repositorySecurityDecorator.findOne(query);
    verify(delegateRepository).findOne(query);
  }

  @Test
  void testFindOneQueryPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.findOne(query));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testFindOneByIdPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
    Object entityId = mock(Object.class);
    repositorySecurityDecorator.findOneById(entityId);
    verify(delegateRepository).findOneById(entityId);
  }

  @Test
  void testFindOneByIdPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
    Object entityId = mock(Object.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.findOneById(entityId));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testFindOneByIdFetchPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
    Object entityId = mock(Object.class);
    Fetch fetch = mock(Fetch.class);
    repositorySecurityDecorator.findOneById(entityId, fetch);
    verify(delegateRepository).findOneById(entityId, fetch);
  }

  @Test
  void testFindOneByIdFetchPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
    Object entityId = mock(Object.class);
    Fetch fetch = mock(Fetch.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.findOneById(entityId, fetch));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testForEachBatchedPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
    Fetch fetch = mock(Fetch.class);
    @SuppressWarnings("unchecked")
    Consumer<List<Entity>> consumer = mock(Consumer.class);
    int batchSize = 10;
    repositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize);
    verify(delegateRepository).forEachBatched(fetch, consumer, batchSize);
  }

  @Test
  void testForEachBatchedPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
    Fetch fetch = mock(Fetch.class);
    @SuppressWarnings("unchecked")
    Consumer<List<Entity>> consumer = mock(Consumer.class);
    int batchSize = 10;
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testIteratorPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, true);
    repositorySecurityDecorator.iterator();
    verify(delegateRepository).iterator();
  }

  @Test
  void testIteratorPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.READ_DATA, false);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.iterator());
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testUpdatePermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.UPDATE_DATA, true);
    Entity entity = mock(Entity.class);
    repositorySecurityDecorator.update(entity);
    verify(delegateRepository).update(entity);
  }

  @Test
  void testUpdatePermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.UPDATE_DATA, false);
    Entity entity = mock(Entity.class);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.update(entity));
    assertThat(exception.getMessage())
        .containsPattern("permission:UPDATE_DATA entityTypeId:entityTypeId");
  }

  @Test
  void testUpdateStreamPermissionGranted() {
    initPermissionServiceMock(EntityTypePermission.UPDATE_DATA, true);
    Stream<Entity> entityStream = Stream.empty();
    repositorySecurityDecorator.update(entityStream);
    verify(delegateRepository).update(entityStream);
  }

  @Test
  void testUpdateStreamPermissionDenied() {
    initPermissionServiceMock(EntityTypePermission.UPDATE_DATA, false);
    Stream<Entity> entityStream = Stream.empty();
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> repositorySecurityDecorator.update(entityStream));
    assertThat(exception.getMessage())
        .containsPattern("permission:UPDATE_DATA entityTypeId:entityTypeId");
  }

  private void initPermissionServiceMock(EntityTypePermission permission, boolean hasPermission) {
    EntityType entityType = mock(EntityType.class);
    String entityTypeId = "entityTypeId";
    when(entityType.getId()).thenReturn(entityTypeId);
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), permission))
        .thenReturn(hasPermission);
  }
}
