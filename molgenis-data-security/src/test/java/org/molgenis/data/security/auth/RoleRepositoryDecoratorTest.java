package org.molgenis.data.security.auth;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;

class RoleRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Role> delegateRepository;
  @Mock private CachedRoleHierarchy cachedRoleHierarchy;
  private RoleRepositoryDecorator roleRepositoryDecorator;

  @BeforeEach
  void setUpBeforeEach() {
    roleRepositoryDecorator = new RoleRepositoryDecorator(delegateRepository, cachedRoleHierarchy);
  }

  @Test
  void testAddRole() {
    Role role = mock(Role.class);
    roleRepositoryDecorator.add(role);
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    verify(delegateRepository).add(role);
  }

  @Test
  void testAddRoleStream() {
    Role role = mock(Role.class);
    roleRepositoryDecorator.add(Stream.of(role));
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Role>> roleStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(roleStreamCaptor.capture());
    assertEquals(roleStreamCaptor.getValue().collect(toList()), singletonList(role));
  }

  @Test
  void testUpdateRole() {
    Role role = mock(Role.class);
    roleRepositoryDecorator.update(role);
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    verify(delegateRepository).update(role);
  }

  @Test
  void testUpdateRoleStream() {
    Role role = mock(Role.class);
    roleRepositoryDecorator.update(Stream.of(role));
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Role>> roleStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(roleStreamCaptor.capture());
    assertEquals(roleStreamCaptor.getValue().collect(toList()), singletonList(role));
  }

  @Test
  void testDeleteAllStream() {
    Object roleId = mock(Role.class);
    roleRepositoryDecorator.deleteAll(Stream.of(roleId));
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> roleStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(roleStreamCaptor.capture());
    assertEquals(roleStreamCaptor.getValue().collect(toList()), singletonList(roleId));
  }

  @Test
  void testDeleteById() {
    Object roleId = mock(Role.class);
    roleRepositoryDecorator.deleteById(roleId);
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    verify(delegateRepository).deleteById(roleId);
  }

  @Test
  void testDeleteAll() {
    roleRepositoryDecorator.deleteAll();
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    verify(delegateRepository).deleteAll();
  }

  @Test
  void testDeleteRole() {
    Role role = mock(Role.class);
    roleRepositoryDecorator.delete(role);
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    verify(delegateRepository).delete(role);
  }

  @Test
  void testDeleteRoleStream() {
    Role role = mock(Role.class);
    roleRepositoryDecorator.delete(Stream.of(role));
    verify(cachedRoleHierarchy).markRoleHierarchyCacheDirty();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Role>> roleStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(roleStreamCaptor.capture());
    assertEquals(roleStreamCaptor.getValue().collect(toList()), singletonList(role));
  }
}
