package org.molgenis.data.security.auth;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.exception.GroupPackageDowngradeException;
import org.molgenis.test.AbstractMockitoTest;

class GroupPackageRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Package> delegateRepository;
  @Mock private GroupPackageService groupPackageService;

  GroupPackageRepositoryDecorator groupPackageRepositoryDecorator;

  @BeforeEach
  void setUp() {
    groupPackageRepositoryDecorator =
        new GroupPackageRepositoryDecorator(delegateRepository, groupPackageService);
  }

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> new GroupPackageRepositoryDecorator(null, null));
  }

  @Test
  void testAdd() {
    Package groupPackage = mockGroupPackage("group");

    groupPackageRepositoryDecorator.add(groupPackage);

    assertAll(
        () -> verify(delegateRepository).add(groupPackage),
        () -> verify(groupPackageService).createGroup(groupPackage));
  }

  @Test
  void testAddNonGroupPackage() {
    Package nonGroupPackage = mockNonGroupPackage();

    groupPackageRepositoryDecorator.add(nonGroupPackage);

    assertAll(
        () -> verify(delegateRepository).add(nonGroupPackage),
        () -> verifyNoInteractions(groupPackageService));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testAddStream() {
    Package groupPackage = mockGroupPackage("test");
    Package nonGroupPackage = mockNonGroupPackage();
    Stream<Package> packages = Stream.of(groupPackage, nonGroupPackage);

    groupPackageRepositoryDecorator.add(packages);

    ArgumentCaptor<Stream<Package>> streamCaptor = ArgumentCaptor.forClass(Stream.class);
    assertAll(
        () -> verify(delegateRepository).add(streamCaptor.capture()),
        () ->
            assertEquals(
                asList(groupPackage, nonGroupPackage),
                streamCaptor.getValue().collect(Collectors.toList())),
        () -> verify(groupPackageService).createGroups(singletonList(groupPackage)),
        () -> verifyNoMoreInteractions(groupPackageService));
  }

  @Test
  void testUpdate() {
    Package existingGroupPackage = mockGroupPackage("test");
    Package newGroupPackage = mock(Package.class);
    when(newGroupPackage.getId()).thenReturn("test");
    when(delegateRepository.findOneById("test")).thenReturn(existingGroupPackage);

    groupPackageRepositoryDecorator.update(newGroupPackage);

    assertAll(
        () -> verify(delegateRepository).update(newGroupPackage),
        () -> verifyNoInteractions(groupPackageService));
  }

  @Test
  void testUpdateToGroupPackage() {
    Package existingNonGroupPackage = mockNonGroupPackage();
    Package newGroupPackage = mockGroupPackage("test");
    when(delegateRepository.findOneById("test")).thenReturn(existingNonGroupPackage);

    groupPackageRepositoryDecorator.update(newGroupPackage);

    assertAll(
        () -> verify(delegateRepository).update(newGroupPackage),
        () -> verify(groupPackageService).createGroup(newGroupPackage));
  }

  @Test
  void testUpdateToNonGroupPackage() {
    Package existingGroupPackage = mockGroupPackage("test");
    Package newGroupPackage = mockNonGroupPackage("test");
    when(delegateRepository.findOneById("test")).thenReturn(existingGroupPackage);

    assertThrows(
        GroupPackageDowngradeException.class,
        () -> groupPackageRepositoryDecorator.update(newGroupPackage));
  }

  @Test
  void testUpdateUnknownPackage() {
    Package groupPackage = mock(Package.class);
    when(groupPackage.getId()).thenReturn("group");
    when(delegateRepository.findOneById("group")).thenReturn(null);

    assertThrows(
        UnknownPackageException.class, () -> groupPackageRepositoryDecorator.update(groupPackage));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testUpdateStream() {
    Package existingGroupPackage = mockGroupPackage("group");
    Package newGroupPackage = mockGroupPackage("group");

    Package existingNonGroupPackage = mockNonGroupPackage("nonGroup");
    Package newNonGroupPackage = mockNonGroupPackage("nonGroup");

    Package existingUpdateToGroupPackage = mockNonGroupPackage("updateToGroup");
    Package newUpdateToGroupPackage = mockGroupPackage("updateToGroup");

    when(delegateRepository.findAll(any(Stream.class)))
        .thenReturn(
            Stream.of(existingGroupPackage, existingNonGroupPackage, existingUpdateToGroupPackage));

    Stream<Package> packages =
        Stream.of(newGroupPackage, newNonGroupPackage, newUpdateToGroupPackage);

    groupPackageRepositoryDecorator.update(packages);

    ArgumentCaptor<Stream<Package>> streamCaptor = ArgumentCaptor.forClass(Stream.class);
    assertAll(
        () -> verify(delegateRepository).update(streamCaptor.capture()),
        () ->
            assertEquals(
                asList(newGroupPackage, newNonGroupPackage, newUpdateToGroupPackage),
                streamCaptor.getValue().collect(Collectors.toList())),
        () -> verify(groupPackageService).createGroup(newUpdateToGroupPackage),
        () -> verifyNoMoreInteractions(groupPackageService));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testUpdateStreamToNonGroupPackage() {
    Package existingGroupPackage = mockGroupPackage("test");
    Package newGroupPackage = mockNonGroupPackage("test");
    when(delegateRepository.findAll(any(Stream.class))).thenReturn(Stream.of(existingGroupPackage));
    Stream<Package> packages = Stream.of(newGroupPackage);

    assertThrows(
        GroupPackageDowngradeException.class,
        () -> groupPackageRepositoryDecorator.update(packages));
  }

  @Test
  void delete() {
    Package groupPackage = mockGroupPackage("test");

    groupPackageRepositoryDecorator.delete(groupPackage);

    assertAll(
        () -> verify(groupPackageService).deleteGroup(groupPackage),
        () -> verify(delegateRepository).delete(groupPackage));
  }

  @Test
  void deleteNonGroupPackage() {
    Package groupPackage = mockNonGroupPackage();

    groupPackageRepositoryDecorator.delete(groupPackage);

    assertAll(
        () -> verifyNoInteractions(groupPackageService),
        () -> verify(delegateRepository).delete(groupPackage));
  }

  @Test
  void deleteById() {
    Package groupPackage = mockGroupPackage("test");
    when(delegateRepository.findOneById("test")).thenReturn(groupPackage);

    groupPackageRepositoryDecorator.deleteById("test");

    assertAll(
        () -> verify(groupPackageService).deleteGroup(groupPackage),
        () -> verify(delegateRepository).findOneById("test"),
        () -> verify(delegateRepository).delete(groupPackage));
  }

  @Test
  void deleteByIdNonGroupPackage() {
    Package groupPackage = mockNonGroupPackage();
    when(delegateRepository.findOneById("test")).thenReturn(groupPackage);

    groupPackageRepositoryDecorator.deleteById("test");

    assertAll(
        () -> verifyNoInteractions(groupPackageService),
        () -> verify(delegateRepository).findOneById("test"),
        () -> verify(delegateRepository).delete(groupPackage));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAll() {
    Package groupPackage = mockGroupPackage("test");
    Package nonGroupPackage = mockNonGroupPackage();

    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1))
                  .accept(asList(groupPackage, nonGroupPackage));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(eq(null), any(), eq(1000));

    groupPackageRepositoryDecorator.deleteAll();

    ArgumentCaptor<Stream<Package>> streamCaptor = ArgumentCaptor.forClass(Stream.class);
    assertAll(
        () -> verify(delegateRepository).delete(streamCaptor.capture()),
        () ->
            assertEquals(
                asList(groupPackage, nonGroupPackage),
                streamCaptor.getValue().collect(Collectors.toList())),
        () -> verify(groupPackageService).deleteGroup(groupPackage),
        () -> verifyNoMoreInteractions(groupPackageService));
  }

  @SuppressWarnings("unchecked")
  @Test
  void deleteStream() {
    Package groupPackage = mockGroupPackage("group");
    Package nonGroupPackage = mockNonGroupPackage();
    Stream<Package> packages = Stream.of(groupPackage, nonGroupPackage);

    groupPackageRepositoryDecorator.delete(packages);

    ArgumentCaptor<Stream<Package>> streamCaptor = ArgumentCaptor.forClass(Stream.class);
    assertAll(
        () -> verify(delegateRepository).delete(streamCaptor.capture()),
        () ->
            assertEquals(
                asList(groupPackage, nonGroupPackage),
                streamCaptor.getValue().collect(Collectors.toList())),
        () -> verify(groupPackageService).deleteGroup(groupPackage),
        () -> verifyNoMoreInteractions(groupPackageService));
  }

  @SuppressWarnings("unchecked")
  @Test
  void deleteAllStream() {
    Package groupPackage = mockGroupPackage("group");
    when(delegateRepository.findOneById("group")).thenReturn(groupPackage);
    Package nonGroupPackage = mockNonGroupPackage();
    when(delegateRepository.findOneById("nonGroup")).thenReturn(nonGroupPackage);
    Stream<Object> packageIds = Stream.of("group", "nonGroup");

    groupPackageRepositoryDecorator.deleteAll(packageIds);

    ArgumentCaptor<Stream<Object>> streamCaptor = ArgumentCaptor.forClass(Stream.class);
    assertAll(
        () -> verify(delegateRepository).deleteAll(streamCaptor.capture()),
        () ->
            assertEquals(
                asList("group", "nonGroup"), streamCaptor.getValue().collect(Collectors.toList())),
        () -> verify(groupPackageService).deleteGroup(groupPackage),
        () -> verifyNoMoreInteractions(groupPackageService));
  }

  private static Package mockGroupPackage(String id) {
    Package pack = mock(Package.class);
    when(pack.getParent()).thenReturn(null);
    when(pack.getRootPackage()).thenReturn(pack);
    when(pack.getId()).thenReturn(id);
    return pack;
  }

  private static Package mockNonGroupPackage() {
    Package pack = mock(Package.class);
    when(pack.getParent()).thenReturn(mock(Package.class));
    return pack;
  }

  private static Package mockNonGroupPackage(String id) {
    Package pack = mockNonGroupPackage();
    when(pack.getId()).thenReturn(id);
    return pack;
  }
}
