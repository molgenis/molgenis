package org.molgenis.data.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;

class QueryValidationRepositoryDecoratorTest {
  private QueryValidationRepositoryDecorator<Entity> queryValidationRepositoryDecorator;
  private EntityType entityType;
  private Repository<Entity> delegateRepository;
  private QueryValidator queryValidator;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    delegateRepository = mock(Repository.class);
    entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    queryValidator = mock(QueryValidator.class);
    queryValidationRepositoryDecorator =
        new QueryValidationRepositoryDecorator<>(delegateRepository, queryValidator);
  }

  @Test
  void testQueryValidationRepositoryDecorator() {
    assertThrows(
        NullPointerException.class, () -> new QueryValidationRepositoryDecorator<>(null, null));
  }

  @Test
  void testCountQueryValid() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    long count = 123L;
    when(delegateRepository.count(query)).thenReturn(count);
    assertEquals(queryValidationRepositoryDecorator.count(query), count);
    verify(queryValidator).validate(query, entityType);
  }

  @Test
  void testCountQueryInvalid() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(queryValidator)
        .validate(query, entityType);
    assertThrows(
        MolgenisValidationException.class, () -> queryValidationRepositoryDecorator.count(query));
  }

  @Test
  void testFindAllQueryValid() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    @SuppressWarnings("unchecked")
    Stream<Entity> entityStream = mock(Stream.class);
    when(delegateRepository.findAll(query)).thenReturn(entityStream);
    assertEquals(queryValidationRepositoryDecorator.findAll(query), entityStream);
    verify(queryValidator).validate(query, entityType);
  }

  @Test
  void testFindAllQueryInvalid() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(queryValidator)
        .validate(query, entityType);
    assertThrows(
        MolgenisValidationException.class, () -> queryValidationRepositoryDecorator.findAll(query));
  }

  @Test
  void testFindOneQueryValid() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    Entity entity = mock(Entity.class);
    when(delegateRepository.findOne(query)).thenReturn(entity);
    assertEquals(queryValidationRepositoryDecorator.findOne(query), entity);
    verify(queryValidator).validate(query, entityType);
  }

  @Test
  void testFindOneQueryInvalid() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    doThrow(mock(MolgenisValidationException.class))
        .when(queryValidator)
        .validate(query, entityType);
    assertThrows(
        MolgenisValidationException.class, () -> queryValidationRepositoryDecorator.findOne(query));
  }
}
