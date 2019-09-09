package org.molgenis.data.security.owned;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.EntityPermission.READ;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityAlreadyExistsException;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityPermission;
import org.molgenis.data.security.exception.EntityPermissionDeniedException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@ContextConfiguration(classes = {RowLevelSecurityRepositoryDecoratorTest.Config.class})
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
class RowLevelSecurityRepositoryDecoratorTest extends AbstractMockitoSpringContextTests {
  private static final String USERNAME = "user";

  @Mock private Repository<Entity> delegateRepository;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;
  @Mock private MutableAclService mutableAclService;
  private RowLevelSecurityRepositoryDecorator rowLevelSecurityRepositoryDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    rowLevelSecurityRepositoryDecorator =
        new RowLevelSecurityRepositoryDecorator(
            delegateRepository, userPermissionEvaluator, mutableAclService);
  }

  @Test
  void testRowLevelSecurityRepositoryDecorator() {
    assertThrows(
        NullPointerException.class,
        () -> new RowLevelSecurityRepositoryDecorator(null, null, null));
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testAdd() {
    Entity entity = getEntityMock();
    MutableAcl acl = mock(MutableAcl.class);
    when(mutableAclService.createAcl(new EntityIdentity(entity))).thenReturn(acl);

    rowLevelSecurityRepositoryDecorator.add(entity);

    verify(acl).insertAce(0, PermissionSet.WRITE, new PrincipalSid(USERNAME), true);
    verify(delegateRepository).add(entity);
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testAddStream() {
    Entity entity = getEntityMock();
    MutableAcl acl = mock(MutableAcl.class);
    when(mutableAclService.createAcl(new EntityIdentity(entity))).thenReturn(acl);

    rowLevelSecurityRepositoryDecorator.add(Stream.of(entity));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entity));
    verify(acl).insertAce(0, PermissionSet.WRITE, new PrincipalSid(USERNAME), true);
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testAddAlreadyExists() {
    Entity entity = getEntityMock();
    when(mutableAclService.createAcl(new EntityIdentity(entity)))
        .thenThrow(new AlreadyExistsException(""));

    Exception exception =
        assertThrows(
            EntityAlreadyExistsException.class,
            () -> rowLevelSecurityRepositoryDecorator.add(entity));
    assertThat(exception.getMessage()).containsPattern("type:entityTypeId id:entityId");
  }

  @Test
  void testUpdate() {
    Entity entity = getEntityMock();
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), EntityPermission.UPDATE))
        .thenReturn(true);
    rowLevelSecurityRepositoryDecorator.update(entity);
    verify(delegateRepository).update(entity);
  }

  @Test
  void testUpdatePermissionDenied() {
    Entity entity = getEntityMock();
    Exception exception =
        assertThrows(
            EntityPermissionDeniedException.class,
            () -> rowLevelSecurityRepositoryDecorator.update(entity));
    assertThat(exception.getMessage())
        .containsPattern("permission:UPDATE entityTypeId:entityTypeId entityId:entityId");
  }

  @Test
  void testUpdateStream() {
    Entity entity = getEntityMock();
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), EntityPermission.UPDATE))
        .thenReturn(true);
    rowLevelSecurityRepositoryDecorator.update(Stream.of(entity));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entity));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testUpdateStreamPermissionDenied() {
    Entity entity = getEntityMock();
    doAnswer(answer -> answer.getArgument(0, Stream.class).count())
        .when(delegateRepository)
        .update(any(Stream.class));

    Exception exception =
        assertThrows(
            EntityPermissionDeniedException.class,
            () -> rowLevelSecurityRepositoryDecorator.update(Stream.of(entity)));
    assertThat(exception.getMessage())
        .containsPattern("permission:UPDATE entityTypeId:entityTypeId entityId:entityId");
  }

  @Test
  void testDelete() {
    Entity entity = getEntityMock();
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), EntityPermission.DELETE))
        .thenReturn(true);
    rowLevelSecurityRepositoryDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
    verify(mutableAclService).deleteAcl(new EntityIdentity(entity), true);
  }

  @Test
  void testDeletePermissionDenied() {
    Entity entity = getEntityMock();

    Exception exception =
        assertThrows(
            EntityPermissionDeniedException.class,
            () -> rowLevelSecurityRepositoryDecorator.delete(entity));
    assertThat(exception.getMessage())
        .containsPattern("permission:DELETE entityTypeId:entityTypeId entityId:entityId");
  }

  @Test
  void testDeleteStream() {
    Entity entity = getEntityMock();
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), EntityPermission.DELETE))
        .thenReturn(true);
    rowLevelSecurityRepositoryDecorator.delete(Stream.of(entity));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entity));
    verify(mutableAclService).deleteAcl(new EntityIdentity(entity), true);
  }

  @Test
  void testDeleteStreamPermissionDenied() {
    Entity entity = getEntityMock();
    rowLevelSecurityRepositoryDecorator.delete(Stream.of(entity));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), emptyList());
    verify(mutableAclService, times(0)).deleteAcl(new EntityIdentity(entity), true);
  }

  @Test
  void testDeleteById() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = "entityId";
    when(userPermissionEvaluator.hasPermission(
            new EntityIdentity(entityTypeId, entityId), EntityPermission.DELETE))
        .thenReturn(true);
    rowLevelSecurityRepositoryDecorator.deleteById(entityId);
    verify(delegateRepository).deleteById(entityId);
    verify(mutableAclService).deleteAcl(new EntityIdentity(entityTypeId, entityId), true);
  }

  @Test
  void testDeleteByIdPermissionDenied() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = "entityId";
    rowLevelSecurityRepositoryDecorator.deleteById(entityId);
    verify(delegateRepository, times(0)).deleteById(entityId);
    verify(mutableAclService, times(0)).deleteAcl(new EntityIdentity(entityTypeId, entityId), true);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAll() {
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
    Entity permittedEntity =
        when(mock(Entity.class).getIdValue()).thenReturn("permittedEntityId").getMock();
    when(permittedEntity.getEntityType()).thenReturn(entityType);
    Entity notPermittedEntity =
        when(mock(Entity.class).getIdValue()).thenReturn("notPermittedEntityId").getMock();
    when(notPermittedEntity.getEntityType()).thenReturn(entityType);
    doAnswer(
            invocation -> {
              Consumer<List<Entity>> consumer = invocation.getArgument(0);
              consumer.accept(asList(permittedEntity, notPermittedEntity));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), eq(1000));

    when(userPermissionEvaluator.hasPermission(
            new EntityIdentity(permittedEntity), EntityPermission.DELETE))
        .thenReturn(true);
    rowLevelSecurityRepositoryDecorator.deleteAll();
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(permittedEntity));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAllPermissionDenied() {
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
    Entity permittedEntity =
        when(mock(Entity.class).getIdValue()).thenReturn("permittedEntityId").getMock();
    when(permittedEntity.getEntityType()).thenReturn(entityType);
    Entity notPermittedEntity =
        when(mock(Entity.class).getIdValue()).thenReturn("notPermittedEntityId").getMock();
    when(notPermittedEntity.getEntityType()).thenReturn(entityType);
    doAnswer(
            invocation -> {
              Consumer<List<Entity>> consumer = invocation.getArgument(0);
              consumer.accept(asList(permittedEntity, notPermittedEntity));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), eq(1000));

    rowLevelSecurityRepositoryDecorator.deleteAll();
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), emptyList());
  }

  @Test
  void testDeleteAllStream() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = "entityId";
    when(userPermissionEvaluator.hasPermission(
            new EntityIdentity(entityTypeId, entityId), EntityPermission.DELETE))
        .thenReturn(true);
    rowLevelSecurityRepositoryDecorator.deleteAll(Stream.of(entityId));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(entityId));
    verify(mutableAclService).deleteAcl(new EntityIdentity(entityTypeId, entityId), true);
  }

  @Test
  void testDeleteAllStreamPermissionDenied() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = "entityId";
    rowLevelSecurityRepositoryDecorator.deleteAll(Stream.of(entityId));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), emptyList());
    verify(mutableAclService, times(0)).deleteAcl(new EntityIdentity(entityTypeId, entityId), true);
  }

  @Test
  void testFindOneById() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = "entityId";
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entityTypeId, entityId), READ))
        .thenReturn(true);
    Entity entity = mock(Entity.class);
    when(delegateRepository.findOneById(entityId)).thenReturn(entity);
    assertEquals(rowLevelSecurityRepositoryDecorator.findOneById(entityId), entity);
  }

  @Test
  void testFindOneByIdPermissionDenied() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = "entityId";
    assertNull(rowLevelSecurityRepositoryDecorator.findOneById(entityId));
  }

  @Test
  void testFindOneByIdFetch() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = "entityId";
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entityTypeId, entityId), READ))
        .thenReturn(true);
    Entity entity = mock(Entity.class);
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findOneById(entityId, fetch)).thenReturn(entity);
    assertEquals(rowLevelSecurityRepositoryDecorator.findOneById(entityId, fetch), entity);
  }

  @Test
  void testFindOneByIdFetchPermissionDenied() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = "entityId";
    Fetch fetch = mock(Fetch.class);
    assertNull(rowLevelSecurityRepositoryDecorator.findOneById(entityId, fetch));
  }

  @Test
  void testFindOne() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE)))
        .thenAnswer(invocation -> Stream.of(entity));
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
    assertEquals(entity, rowLevelSecurityRepositoryDecorator.findOne(query));
  }

  @Test
  void testFindOnePermissionDenied() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE)))
        .thenAnswer(invocation -> Stream.of(entity));
    assertNull(rowLevelSecurityRepositoryDecorator.findOne(query));
  }

  @Test
  void testFindAllQuery() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE)))
        .thenAnswer(invocation -> Stream.of(entity));
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
    assertEquals(
        rowLevelSecurityRepositoryDecorator.findAll(query).collect(toList()),
        singletonList(entity));
  }

  @Test
  void testFindAllQueryPermissionDenied() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE)))
        .thenAnswer(invocation -> Stream.of(entity));
    assertEquals(rowLevelSecurityRepositoryDecorator.findAll(query).collect(toList()), emptyList());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAllStream() {
    Object entityId = "entityId";
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(any(Stream.class))).thenAnswer(invocation -> Stream.of(entity));
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
    assertEquals(
        rowLevelSecurityRepositoryDecorator.findAll(Stream.of(entityId)).collect(toList()),
        singletonList(entity));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAllStreamPermissionDenied() {
    Object entityId = "entityId";
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(any(Stream.class))).thenAnswer(invocation -> Stream.of(entity));
    assertEquals(
        rowLevelSecurityRepositoryDecorator.findAll(Stream.of(entityId)).collect(toList()),
        emptyList());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAllStreamFetch() {
    Object entityId = "entityId";
    Entity entity = getEntityMock();
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findAll(any(Stream.class), eq(fetch)))
        .thenAnswer(invocation -> Stream.of(entity));
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
    assertEquals(
        rowLevelSecurityRepositoryDecorator.findAll(Stream.of(entityId), fetch).collect(toList()),
        singletonList(entity));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAllStreamFetchPermissionDenied() {
    Object entityId = "entityId";
    Entity entity = getEntityMock();
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findAll(any(Stream.class), eq(fetch)))
        .thenAnswer(invocation -> Stream.of(entity));
    assertEquals(
        rowLevelSecurityRepositoryDecorator.findAll(Stream.of(entityId), fetch).collect(toList()),
        emptyList());
  }

  @Test
  void testCount() {
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE)))
        .thenAnswer(invocation -> Stream.of(entity));
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), EntityPermission.READ))
        .thenReturn(true);
    assertEquals(rowLevelSecurityRepositoryDecorator.count(), 1L);
  }

  @Test
  void testCountPermissionDenied() {
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE)))
        .thenAnswer(invocation -> Stream.of(entity));
    assertEquals(rowLevelSecurityRepositoryDecorator.count(), 0L);
  }

  @Test
  void testCountQuery() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE)))
        .thenAnswer(invocation -> Stream.of(entity));
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), EntityPermission.READ))
        .thenReturn(true);
    assertEquals(rowLevelSecurityRepositoryDecorator.count(query), 1L);
  }

  @Test
  void testCountQueryPermissionDenied() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Entity entity = getEntityMock();
    when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE)))
        .thenAnswer(invocation -> Stream.of(entity));
    assertEquals(rowLevelSecurityRepositoryDecorator.count(query), 0L);
  }

  @Test
  void testIterator() {
    Entity entity = getEntityMock();
    when(delegateRepository.iterator()).thenReturn(singletonList(entity).iterator());
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
    assertEquals(
        newArrayList(rowLevelSecurityRepositoryDecorator.iterator()), singletonList(entity));
  }

  @Test
  void testIteratorPermissionDenied() {
    Entity entity = getEntityMock();
    when(delegateRepository.iterator()).thenReturn(singletonList(entity).iterator());
    assertEquals(newArrayList(rowLevelSecurityRepositoryDecorator.iterator()), emptyList());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testForEachBatched() {
    Entity entity = getEntityMock();
    Fetch fetch = mock(Fetch.class);
    List<Entity> actualEntities = new ArrayList<>();
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1)).accept(singletonList(entity));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(eq(fetch), any(), eq(1000));
    when(userPermissionEvaluator.hasPermission(new EntityIdentity(entity), READ)).thenReturn(true);
    rowLevelSecurityRepositoryDecorator.forEachBatched(fetch, actualEntities::addAll, 1000);
    assertEquals(actualEntities, singletonList(entity));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testForEachBatchedPermissionDenied() {
    Entity entity = getEntityMock();
    Fetch fetch = mock(Fetch.class);
    List<Entity> actualEntities = new ArrayList<>();
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1)).accept(singletonList(entity));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(eq(fetch), any(), eq(1000));
    rowLevelSecurityRepositoryDecorator.forEachBatched(fetch, actualEntities::addAll, 1000);
    assertEquals(actualEntities, emptyList());
  }

  @WithMockUser(username = USERNAME, roles = "SU")
  @Test
  void testAggregate() {
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    AggregateResult aggregateResponse = mock(AggregateResult.class);
    when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResponse);
    assertEquals(rowLevelSecurityRepositoryDecorator.aggregate(aggregateQuery), aggregateResponse);
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testAggregatePermissionDenied() {
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    assertThrows(
        UnsupportedOperationException.class,
        () -> rowLevelSecurityRepositoryDecorator.aggregate(aggregateQuery));
  }

  private Entity getEntityMock() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("entityTypeId");
    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getIdValue()).thenReturn("entityId");
    return entity;
  }

  static class Config {}
}
