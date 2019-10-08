package org.molgenis.data.decorator;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class PermissionCheckingDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Entity> delegateRepository;
  @Mock private PermissionChecker<Entity> permissionChecker;

  private PermissionCheckingDecorator<Entity> permissionCheckingDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    permissionCheckingDecorator =
        new PermissionCheckingDecorator<>(delegateRepository, permissionChecker);
  }

  @Test
  void testPermissionCheckingDecorator() {
    assertThrows(NullPointerException.class, () -> new PermissionCheckingDecorator<>(null, null));
  }

  @Test
  void testIterator() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    when(delegateRepository.iterator())
        .thenReturn(asList(permittedEntity, notPermittedEntity).iterator());
    when(permissionChecker.isReadAllowed(permittedEntity)).thenReturn(true);
    assertEquals(
        singletonList(permittedEntity), newArrayList(permissionCheckingDecorator.iterator()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testForEachBatched() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    when(permissionChecker.isReadAllowed(permittedEntity)).thenReturn(true);
    Fetch fetch = mock(Fetch.class);
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1))
                  .accept(asList(permittedEntity, notPermittedEntity));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(eq(fetch), any(), eq(1000));
    List<Entity> actualEntities = new ArrayList<>();
    permissionCheckingDecorator.forEachBatched(fetch, actualEntities::addAll, 1000);
    assertEquals(singletonList(permittedEntity), actualEntities);
  }

  @Test
  void testCount() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    when(delegateRepository.findAll(new QueryImpl<>().offset(0).pageSize(Integer.MAX_VALUE)))
        .thenReturn(Stream.of(permittedEntity, notPermittedEntity));
    when(permissionChecker.isCountAllowed(permittedEntity)).thenReturn(true);
    assertEquals(1L, permissionCheckingDecorator.count());
  }

  @Test
  void testCountQuery() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    Query<Entity> query = new QueryImpl<>();
    when(delegateRepository.findAll(new QueryImpl<>().offset(0).pageSize(Integer.MAX_VALUE)))
        .thenReturn(Stream.of(permittedEntity, notPermittedEntity));
    when(permissionChecker.isCountAllowed(permittedEntity)).thenReturn(true);
    assertEquals(1L, permissionCheckingDecorator.count(query));
  }

  @Test
  void testFindAllQuery() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    when(delegateRepository.findAll(new QueryImpl<>().offset(0).pageSize(Integer.MAX_VALUE)))
        .thenReturn(Stream.of(permittedEntity, notPermittedEntity));
    when(permissionChecker.isReadAllowed(permittedEntity)).thenReturn(true);
    assertEquals(
        singletonList(permittedEntity),
        permissionCheckingDecorator.findAll(new QueryImpl<>()).collect(toList()));
  }

  @Test
  void testFindAllQuerySkipLimit() {
    Entity permittedEntity0 = mock(Entity.class);
    Entity notPermittedEntity0 = mock(Entity.class);
    Entity permittedEntity1 = mock(Entity.class);
    Entity notPermittedEntity1 = mock(Entity.class);
    Entity permittedEntity2 = mock(Entity.class);
    when(delegateRepository.findAll(new QueryImpl<>().offset(0).pageSize(Integer.MAX_VALUE)))
        .thenReturn(
            Stream.of(
                permittedEntity0,
                notPermittedEntity0,
                permittedEntity1,
                notPermittedEntity1,
                permittedEntity2));
    doReturn(true).when(permissionChecker).isReadAllowed(permittedEntity0);
    doReturn(false).when(permissionChecker).isReadAllowed(notPermittedEntity0);
    doReturn(true).when(permissionChecker).isReadAllowed(permittedEntity1);
    assertEquals(
        singletonList(permittedEntity1),
        permissionCheckingDecorator
            .findAll(new QueryImpl<>().offset(1).pageSize(1))
            .collect(toList()));
  }

  @Test
  void testFindOneQuery() {
    Entity notPermittedEntity = mock(Entity.class);
    Entity permittedEntity = mock(Entity.class);
    when(delegateRepository.findAll(new QueryImpl<>().offset(0).pageSize(Integer.MAX_VALUE)))
        .thenReturn(Stream.of(notPermittedEntity, permittedEntity));
    doReturn(false).when(permissionChecker).isReadAllowed(notPermittedEntity);
    doReturn(true).when(permissionChecker).isReadAllowed(permittedEntity);
    assertEquals(permittedEntity, permissionCheckingDecorator.findOne(new QueryImpl<>()));
  }

  @Test
  void testFindOneByIdPermitted() {
    Entity permittedEntity = mock(Entity.class);
    when(delegateRepository.findOneById("permittedId")).thenReturn(permittedEntity);
    when(permissionChecker.isReadAllowed(permittedEntity)).thenReturn(true);
    assertEquals(permittedEntity, permissionCheckingDecorator.findOneById("permittedId"));
  }

  @Test
  void testFindOneByIdNotPermitted() {
    assertNull(permissionCheckingDecorator.findOneById("notPermittedId"));
  }

  @Test
  void testFindOneByIdFetchPermitted() {
    Entity permittedEntity = mock(Entity.class);
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findOneById("permittedId", fetch)).thenReturn(permittedEntity);
    when(permissionChecker.isReadAllowed(permittedEntity)).thenReturn(true);
    assertEquals(permittedEntity, permissionCheckingDecorator.findOneById("permittedId", fetch));
  }

  @Test
  void testFindOneByIdFetchNotPermitted() {
    Fetch fetch = mock(Fetch.class);
    assertNull(permissionCheckingDecorator.findOneById("notPermittedId", fetch));
  }

  @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
  @Test
  void testFindAllStream() {
    Entity permittedEntity = mock(Entity.class);
    when(delegateRepository.findAll(any(Stream.class)))
        .thenAnswer(
            invocation -> {
              ((Stream<Object>) invocation.getArguments()[0]).count(); // consume stream
              return Stream.of(permittedEntity);
            });
    when(permissionChecker.isReadAllowed(permittedEntity)).thenReturn(true);
    assertEquals(
        singletonList(permittedEntity),
        permissionCheckingDecorator.findAll(of("permittedId", "notPermittedId")).collect(toList()));
  }

  @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
  @Test
  void testFindAllStreamFetch() {
    Entity permittedEntity = mock(Entity.class);
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findAll(any(Stream.class), eq(fetch)))
        .thenAnswer(
            invocation -> {
              ((Stream<Object>) invocation.getArguments()[0]).count(); // consume stream
              return Stream.of(permittedEntity);
            });
    when(permissionChecker.isReadAllowed(permittedEntity)).thenReturn(true);
    assertEquals(
        singletonList(permittedEntity),
        permissionCheckingDecorator
            .findAll(of("permittedId", "notPermittedId"), fetch)
            .collect(toList()));
  }

  @Test
  void testUpdateEntityPermitted() {
    Entity permittedEntity = mock(Entity.class);
    when(permissionChecker.isUpdateAllowed(permittedEntity)).thenReturn(true);
    permissionCheckingDecorator.update(permittedEntity);
    verify(delegateRepository).update(permittedEntity);
  }

  @Test
  void testUpdateEntityNotPermitted() {
    Entity entity = mock(Entity.class);
    permissionCheckingDecorator.update(entity);
    verifyZeroInteractions(delegateRepository);
  }

  @Test
  void testUpdateStream() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    when(permissionChecker.isUpdateAllowed(permittedEntity)).thenReturn(true);
    permissionCheckingDecorator.update(Stream.of(permittedEntity, notPermittedEntity));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(entityStreamCaptor.capture());
    assertEquals(singletonList(permittedEntity), entityStreamCaptor.getValue().collect(toList()));
  }

  @Test
  void testDeleteEntityPermitted() {
    Entity entity = mock(Entity.class);
    when(permissionChecker.isDeleteAllowed(entity)).thenReturn(true);
    permissionCheckingDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
  }

  @Test
  void testDeleteEntityNotPermitted() {
    Entity entity = mock(Entity.class);
    permissionCheckingDecorator.delete(entity);
    verifyZeroInteractions(delegateRepository);
  }

  @Test
  void testDeleteStream() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    when(permissionChecker.isDeleteAllowed(permittedEntity)).thenReturn(true);
    permissionCheckingDecorator.delete(Stream.of(permittedEntity, notPermittedEntity));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(singletonList(permittedEntity), entityStreamCaptor.getValue().collect(toList()));
  }

  @Test
  void testDeleteByIdPermitted() {
    Entity permittedEntity = mock(Entity.class);
    when(delegateRepository.findOneById("permittedId")).thenReturn(permittedEntity);
    when(permissionChecker.isDeleteAllowed(permittedEntity)).thenReturn(true);
    permissionCheckingDecorator.deleteById("permittedId");
    verify(delegateRepository).deleteById("permittedId");
  }

  @Test
  void testDeleteByIdNotPermitted() {
    Entity permittedEntity = mock(Entity.class);
    when(delegateRepository.findOneById("permittedId")).thenReturn(permittedEntity);
    permissionCheckingDecorator.deleteById("permittedId");
    verifyZeroInteractions(delegateRepository);
  }

  @Test
  void testDeleteByIdUnknownEntity() {
    permissionCheckingDecorator.deleteById("unknownEntityId");
    verify(delegateRepository).deleteById("unknownEntityId");
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAll() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    doAnswer(
            invocation -> {
              Consumer<List<Entity>> consumer = invocation.getArgument(0);
              consumer.accept(asList(permittedEntity, notPermittedEntity));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), eq(1000));

    when(permissionChecker.isDeleteAllowed(permittedEntity)).thenReturn(true);
    permissionCheckingDecorator.deleteAll();
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(singletonList(permittedEntity), entityStreamCaptor.getValue().collect(toList()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAllStream() {
    Entity permittedEntity =
        when(mock(Entity.class).getIdValue()).thenReturn("permittedId").getMock();
    Entity notPermittedEntity = mock(Entity.class);
    when(permissionChecker.isDeleteAllowed(permittedEntity)).thenReturn(true);
    when(delegateRepository.findAll(any(Stream.class)))
        .thenReturn(Stream.of(permittedEntity, notPermittedEntity));

    permissionCheckingDecorator.deleteAll(Stream.of("permittedId", "notPermittedId"));

    ArgumentCaptor<Stream<Object>> entityIdStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(entityIdStreamCaptor.capture());
    assertEquals(singletonList("permittedId"), entityIdStreamCaptor.getValue().collect(toList()));
  }

  @Test
  void testAddEntityPermitted() {
    Entity entity = mock(Entity.class);
    when(permissionChecker.isAddAllowed(entity)).thenReturn(true);
    permissionCheckingDecorator.add(entity);
    verify(delegateRepository).add(entity);
  }

  @Test
  void testAddEntityNotPermitted() {
    Entity entity = mock(Entity.class);
    permissionCheckingDecorator.add(entity);
    verifyZeroInteractions(delegateRepository);
  }

  @Test
  void testAddStream() {
    Entity permittedEntity = mock(Entity.class);
    Entity notPermittedEntity = mock(Entity.class);
    when(permissionChecker.isAddAllowed(permittedEntity)).thenReturn(true);
    permissionCheckingDecorator.add(Stream.of(permittedEntity, notPermittedEntity));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Entity>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(entityStreamCaptor.capture());
    assertEquals(singletonList(permittedEntity), entityStreamCaptor.getValue().collect(toList()));
  }

  @Test
  void testAggregateNonSystemOrSuperuser() {
    SecurityContext originalSecurityContext = SecurityContextHolder.getContext();
    try {
      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(
          new UsernamePasswordAuthenticationToken("principal", "credentials", emptySet()));
      AggregateQuery aggregateQuery = mock(AggregateQuery.class);
      assertThrows(
          UnsupportedOperationException.class,
          () -> permissionCheckingDecorator.aggregate(aggregateQuery));
    } finally {
      SecurityContextHolder.setContext(originalSecurityContext);
    }
  }

  @Test
  void testAggregateSystemUser() {
    SecurityContext originalSecurityContext = SecurityContextHolder.getContext();
    try {
      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(
          new UsernamePasswordAuthenticationToken(
              "principal", "credentials", singleton(new SimpleGrantedAuthority("ROLE_SYSTEM"))));
      SecurityContextHolder.setContext(securityContext);

      AggregateQuery aggregateQuery = mock(AggregateQuery.class);
      permissionCheckingDecorator.aggregate(aggregateQuery);
      verify(delegateRepository).aggregate(aggregateQuery);
    } finally {
      SecurityContextHolder.setContext(originalSecurityContext);
    }
  }
}
