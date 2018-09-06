package org.molgenis.data.icd10;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Icd10ExpanderDecoratorTest extends AbstractMockitoTest {
  static final String ICD10_ENTITY_TYPE_ID = "icd10Entity";
  static final String EXPAND_ATTRIBUTE = "expandAttribute";

  @Mock private Repository<Entity> decoratedRepository;
  @Mock private CollectionsQueryTransformer queryTransformer;
  @Mock private Query<Entity> query;
  @Mock private Query<Entity> transformedQuery;

  private Icd10ExpanderDecorator icd10ExpanderDecorator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    when(queryTransformer.transformQuery(query, ICD10_ENTITY_TYPE_ID, EXPAND_ATTRIBUTE))
        .thenReturn(transformedQuery);
    icd10ExpanderDecorator =
        new Icd10ExpanderDecorator(
            decoratedRepository, queryTransformer, ICD10_ENTITY_TYPE_ID, EXPAND_ATTRIBUTE);
  }

  @Test
  public void testCount() {
    when(decoratedRepository.count(transformedQuery)).thenReturn(123L);
    assertEquals(icd10ExpanderDecorator.count(query), 123L);
  }

  @Test
  public void testFindOne() {
    Entity entity = mock(Entity.class);
    when(decoratedRepository.findOne(transformedQuery)).thenReturn(entity);
    assertEquals(icd10ExpanderDecorator.findOne(query), entity);
  }

  @Test
  public void testFindAll() {
    Stream<Entity> entities = Stream.empty();
    when(decoratedRepository.findAll(transformedQuery)).thenReturn(entities);
    assertEquals(icd10ExpanderDecorator.findAll(query), entities);
  }
}
