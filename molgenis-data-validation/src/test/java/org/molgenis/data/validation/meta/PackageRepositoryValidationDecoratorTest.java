package org.molgenis.data.validation.meta;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.data.validation.meta.PackageValidator.ValidationMode;
import org.molgenis.test.AbstractMockitoTest;

class PackageRepositoryValidationDecoratorTest extends AbstractMockitoTest {
  private PackageRepositoryValidationDecorator packageRepositoryValidationDecorator;
  private Repository<Package> delegateRepository;
  private PackageValidator packageValidator;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    delegateRepository = mock(Repository.class);
    packageValidator = mock(PackageValidator.class);
    packageRepositoryValidationDecorator =
        new PackageRepositoryValidationDecorator(delegateRepository, packageValidator);
  }

  @Test
  void testAddValid() {
    Package package_ = mock(Package.class);
    packageRepositoryValidationDecorator.add(package_);
    verify(delegateRepository).add(package_);
  }

  @Test
  void testAddInvalid() {
    Package package_ = mock(Package.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.ADD);
    assertThrows(
        MolgenisValidationException.class,
        () -> packageRepositoryValidationDecorator.add(package_));
  }

  @Test
  void testAddStreamValid() {
    Package package_ = mock(Package.class);
    packageRepositoryValidationDecorator.add(Stream.of(package_));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(packageCaptor.capture());
    assertEquals(singletonList(package_), packageCaptor.getValue().collect(toList()));
    verify(packageValidator).validate(package_, ValidationMode.ADD);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testAddStreamInvalid() {
    Package package_ = mock(Package.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.ADD);
    packageRepositoryValidationDecorator.add(Stream.of(package_));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(packageCaptor.capture());
    assertThrows(MolgenisValidationException.class, () -> packageCaptor.getValue().count());
  }

  @Test
  void testUpdateValid() {
    Package package_ = mock(Package.class);
    packageRepositoryValidationDecorator.update(package_);
    verify(packageValidator).validate(package_, ValidationMode.UPDATE);
    verify(delegateRepository).update(package_);
  }

  @Test
  void testUpdateInvalid() {
    Package package_ = mock(Package.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.UPDATE);
    assertThrows(
        MolgenisValidationException.class,
        () -> packageRepositoryValidationDecorator.update(package_));
  }

  @Test
  void testUpdateStreamValid() {
    Package package_ = mock(Package.class);
    packageRepositoryValidationDecorator.update(Stream.of(package_));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(packageCaptor.capture());
    assertEquals(singletonList(package_), packageCaptor.getValue().collect(toList()));
    verify(packageValidator).validate(package_, ValidationMode.UPDATE);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testUpdateStreamInvalid() {
    Package package_ = mock(Package.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.UPDATE);
    packageRepositoryValidationDecorator.update(Stream.of(package_));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(packageCaptor.capture());
    assertThrows(MolgenisValidationException.class, () -> packageCaptor.getValue().count());
  }

  @Test
  void testDeleteValid() {
    Package package_ = mock(Package.class);
    packageRepositoryValidationDecorator.delete(package_);
    verify(packageValidator).validate(package_, ValidationMode.DELETE);
    verify(delegateRepository).delete(package_);
  }

  @Test
  void testDeleteInvalid() {
    Package package_ = mock(Package.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.DELETE);
    assertThrows(
        MolgenisValidationException.class,
        () -> packageRepositoryValidationDecorator.delete(package_));
  }

  @Test
  void testDeleteStreamValid() {
    Package package_ = mock(Package.class);
    packageRepositoryValidationDecorator.delete(Stream.of(package_));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(packageCaptor.capture());
    assertEquals(singletonList(package_), packageCaptor.getValue().collect(toList()));
    verify(packageValidator).validate(package_, ValidationMode.DELETE);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testDeleteStreamInvalid() {
    Package package_ = mock(Package.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.DELETE);
    packageRepositoryValidationDecorator.delete(Stream.of(package_));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(packageCaptor.capture());
    assertThrows(MolgenisValidationException.class, () -> packageCaptor.getValue().count());
  }

  @Test
  void testDeleteByIdValid() {
    Package package_ = mock(Package.class);
    Object id = mock(Object.class);
    when(delegateRepository.findOneById(id)).thenReturn(package_);
    packageRepositoryValidationDecorator.deleteById(id);
    verify(packageValidator).validate(package_, ValidationMode.DELETE);
    verify(delegateRepository).deleteById(id);
  }

  @Test
  void testDeleteByIdInvalid() {
    Package package_ = mock(Package.class);
    Object id = mock(Object.class);
    when(delegateRepository.findOneById(id)).thenReturn(package_);
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.DELETE);
    assertThrows(
        MolgenisValidationException.class,
        () -> packageRepositoryValidationDecorator.deleteById(id));
  }

  @Test
  void testDeleteAllValid() {
    Package package_ = mock(Package.class);
    when(delegateRepository.iterator()).thenReturn(singletonList(package_).iterator());
    packageRepositoryValidationDecorator.deleteAll();
    verify(packageValidator).validate(package_, ValidationMode.DELETE);
    verify(delegateRepository).deleteAll();
  }

  @Test
  void testDeleteAllInvalid() {
    Package package_ = mock(Package.class);
    when(delegateRepository.iterator()).thenReturn(singletonList(package_).iterator());
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.DELETE);
    assertThrows(
        MolgenisValidationException.class, () -> packageRepositoryValidationDecorator.deleteAll());
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testDeleteAllStreamValid() {
    Package package_ = mock(Package.class);
    Object id = mock(Object.class);
    when(delegateRepository.findOneById(id)).thenReturn(package_);
    packageRepositoryValidationDecorator.deleteAll(Stream.of(id));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(packageIdCaptor.capture());
    packageIdCaptor.getValue().count();
    verify(packageValidator).validate(package_, ValidationMode.DELETE);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testDeleteAllStreamInvalid() {
    Package package_ = mock(Package.class);
    Object id = mock(Object.class);
    when(delegateRepository.findOneById(id)).thenReturn(package_);
    doThrow(mock(MolgenisValidationException.class))
        .when(packageValidator)
        .validate(package_, ValidationMode.DELETE);
    packageRepositoryValidationDecorator.deleteAll(Stream.of(id));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(packageIdCaptor.capture());
    assertThrows(MolgenisValidationException.class, () -> packageIdCaptor.getValue().count());
  }
}
