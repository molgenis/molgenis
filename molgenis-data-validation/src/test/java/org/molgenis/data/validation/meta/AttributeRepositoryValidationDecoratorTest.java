package org.molgenis.data.validation.meta;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;

class AttributeRepositoryValidationDecoratorTest {
  private AttributeRepositoryValidationDecorator attributeRepoValidationDecorator;
  private Repository<Attribute> delegateRepository;
  private AttributeValidator attributeValidator;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    delegateRepository = mock(Repository.class);
    attributeValidator = mock(AttributeValidator.class);
    attributeRepoValidationDecorator =
        new AttributeRepositoryValidationDecorator(delegateRepository, attributeValidator);
  }

  @Test
  void attributeRepositoryValidationDecorator() {
    assertThrows(
        NullPointerException.class, () -> new AttributeRepositoryValidationDecorator(null, null));
  }

  @Test
  void updateAttributeValid() {
    Attribute attribute = mock(Attribute.class);
    doNothing().when(attributeValidator).validate(attribute, ValidationMode.UPDATE);
    attributeRepoValidationDecorator.update(attribute);
    verify(attributeValidator, times(1)).validate(attribute, ValidationMode.UPDATE);
    verify(delegateRepository, times(1)).update(attribute);
  }

  @Test
  void updateEntityInvalid() throws Exception {
    Attribute attribute = mock(Attribute.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(attributeValidator)
        .validate(attribute, ValidationMode.UPDATE);
    assertThrows(
        MolgenisValidationException.class,
        () -> attributeRepoValidationDecorator.update(attribute));
  }

  @Test
  void updateEntityStreamValid() {
    Attribute attribute0 = mock(Attribute.class);
    Attribute attribute1 = mock(Attribute.class);
    doNothing().when(attributeValidator).validate(attribute0, ValidationMode.UPDATE);
    doNothing().when(attributeValidator).validate(attribute1, ValidationMode.UPDATE);
    attributeRepoValidationDecorator.update(Stream.of(attribute0, attribute1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(captor.capture());
    captor.getValue().count(); // process all entities in stream
    verify(attributeValidator, times(2)).validate(any(Attribute.class), eq(ValidationMode.UPDATE));
  }

  @Test
  void updateEntityStreamInvalid() {
    Attribute attribute0 = mock(Attribute.class);
    Attribute attribute1 = mock(Attribute.class);
    doNothing().when(attributeValidator).validate(attribute0, ValidationMode.UPDATE);
    doThrow(mock(MolgenisValidationException.class))
        .when(attributeValidator)
        .validate(attribute1, ValidationMode.UPDATE);
    attributeRepoValidationDecorator.update(Stream.of(attribute0, attribute1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(captor.capture());
    assertThrows(
        MolgenisValidationException.class,
        () -> captor.getValue().count()); // process all entities in stream
  }

  @Test
  void addEntityValid() {
    Attribute attribute = mock(Attribute.class);
    doNothing().when(attributeValidator).validate(attribute, ValidationMode.ADD);
    attributeRepoValidationDecorator.add(attribute);
    verify(attributeValidator, times(1)).validate(attribute, ValidationMode.ADD);
  }

  @Test
  void addEntityInvalid() {
    Attribute attribute = mock(Attribute.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(attributeValidator)
        .validate(attribute, ValidationMode.ADD);
    assertThrows(
        MolgenisValidationException.class, () -> attributeRepoValidationDecorator.add(attribute));
    verify(attributeValidator, times(1)).validate(attribute, ValidationMode.ADD);
  }

  @Test
  void addEntityStreamValid() {
    Attribute attribute0 = mock(Attribute.class);
    Attribute attribute1 = mock(Attribute.class);
    doNothing().when(attributeValidator).validate(attribute0, ValidationMode.ADD);
    doNothing().when(attributeValidator).validate(attribute1, ValidationMode.ADD);
    attributeRepoValidationDecorator.add(Stream.of(attribute0, attribute1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(captor.capture());
    captor.getValue().count(); // process all entities in stream
    verify(attributeValidator, times(2)).validate(any(Attribute.class), eq(ValidationMode.ADD));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void addEntityStreamInvalid() {
    Attribute attribute0 = mock(Attribute.class);
    Attribute attribute1 = mock(Attribute.class);
    doNothing().when(attributeValidator).validate(attribute0, ValidationMode.ADD);
    doThrow(mock(MolgenisValidationException.class))
        .when(attributeValidator)
        .validate(attribute1, ValidationMode.ADD);
    attributeRepoValidationDecorator.add(Stream.of(attribute0, attribute1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Attribute>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(captor.capture());
    assertThrows(
        MolgenisValidationException.class,
        () -> captor.getValue().count()); // process all entities in stream
  }
}
