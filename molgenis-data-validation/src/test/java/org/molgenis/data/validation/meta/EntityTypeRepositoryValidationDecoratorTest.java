package org.molgenis.data.validation.meta;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.MolgenisValidationException;

class EntityTypeRepositoryValidationDecoratorTest {
  private EntityTypeRepositoryValidationDecorator entityTypeRepoValidationDecorator;
  private Repository<EntityType> delegateRepository;
  private EntityTypeValidator entityTypeValidator;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    delegateRepository = mock(Repository.class);
    entityTypeValidator = mock(EntityTypeValidator.class);
    entityTypeRepoValidationDecorator =
        new EntityTypeRepositoryValidationDecorator(delegateRepository, entityTypeValidator);
  }

  @Test
  void EntityTypeRepositoryValidationDecorator() {
    assertThrows(
        NullPointerException.class, () -> new EntityTypeRepositoryValidationDecorator(null, null));
  }

  @Test
  void updateEntityValid() {
    EntityType entityType = mock(EntityType.class);
    doNothing().when(entityTypeValidator).validate(entityType);
    entityTypeRepoValidationDecorator.update(entityType);
  }

  @Test
  void updateEntityInvalid() throws Exception {
    EntityType entityType = mock(EntityType.class);
    doThrow(mock(MolgenisValidationException.class)).when(entityTypeValidator).validate(entityType);
    assertThrows(
        MolgenisValidationException.class,
        () -> entityTypeRepoValidationDecorator.update(entityType));
  }

  @Test
  void updateEntityStreamValid() {
    EntityType entityType0 = mock(EntityType.class);
    EntityType entityType1 = mock(EntityType.class);
    doNothing().when(entityTypeValidator).validate(entityType0);
    doNothing().when(entityTypeValidator).validate(entityType1);
    entityTypeRepoValidationDecorator.update(Stream.of(entityType0, entityType1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(captor.capture());
    captor.getValue().count(); // process all entities in stream
  }

  @Test
  void updateEntityStreamInvalid() {
    EntityType entityType0 = mock(EntityType.class);
    EntityType entityType1 = mock(EntityType.class);
    doNothing().when(entityTypeValidator).validate(entityType0);
    doThrow(mock(MolgenisValidationException.class))
        .when(entityTypeValidator)
        .validate(entityType1);
    entityTypeRepoValidationDecorator.update(Stream.of(entityType0, entityType1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(captor.capture());
    assertThrows(
        MolgenisValidationException.class,
        () -> captor.getValue().count()); // process all entities in stream
  }

  @Test
  void addEntityValid() {
    EntityType entityType = mock(EntityType.class);
    doNothing().when(entityTypeValidator).validate(entityType);
    entityTypeRepoValidationDecorator.add(entityType);
  }

  @Test
  void addEntityInvalid() {
    EntityType entityType = mock(EntityType.class);
    doThrow(mock(MolgenisValidationException.class)).when(entityTypeValidator).validate(entityType);
    assertThrows(
        MolgenisValidationException.class, () -> entityTypeRepoValidationDecorator.add(entityType));
  }

  @Test
  void addEntityStreamValid() {
    EntityType entityType0 = mock(EntityType.class);
    EntityType entityType1 = mock(EntityType.class);
    doNothing().when(entityTypeValidator).validate(entityType0);
    doNothing().when(entityTypeValidator).validate(entityType1);
    entityTypeRepoValidationDecorator.add(Stream.of(entityType0, entityType1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(captor.capture());
    captor.getValue().count(); // process all entities in stream
  }

  @Test
  void addEntityStreamInvalid() {
    EntityType entityType0 = mock(EntityType.class);
    EntityType entityType1 = mock(EntityType.class);
    doNothing().when(entityTypeValidator).validate(entityType0);
    doThrow(mock(MolgenisValidationException.class))
        .when(entityTypeValidator)
        .validate(entityType1);
    entityTypeRepoValidationDecorator.add(Stream.of(entityType0, entityType1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<EntityType>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(captor.capture());
    assertThrows(
        MolgenisValidationException.class,
        () -> captor.getValue().count()); // process all entities in stream
  }
}
