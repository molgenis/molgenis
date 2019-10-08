package org.molgenis.data.icd10;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.icd10.Icd10ExpanderDecoratorTest.EXPAND_ATTRIBUTE;
import static org.molgenis.data.icd10.Icd10ExpanderDecoratorTest.ICD10_ENTITY_TYPE_ID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;

// squids: nested code blocks should be extracted & using the same literal string multiple times
@SuppressWarnings({"squid:S1199", "squid:S1192"})
class CollectionsQueryTransformerImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;

  @Mock private Icd10ClassExpander icd10ClassExpander;

  private CollectionsQueryTransformerImpl collectionsQueryTransformerImpl;
  private static List<Entity> expandedDiseaseEntities;
  private static Entity diseaseEntity;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    collectionsQueryTransformerImpl =
        new CollectionsQueryTransformerImpl(icd10ClassExpander, dataService);
  }

  /**
   * Data providers are triggered before the @BeforeEach so this can be used to set up the required
   * mocks
   */
  private static void setExpandedDiseaseEntities() {
    diseaseEntity = mock(Entity.class);
    Entity expandedDiseaseEntity = mock(Entity.class);
    expandedDiseaseEntities = asList(diseaseEntity, expandedDiseaseEntity);
  }

  @Test
  void testBbmriEricCollectionsQueryTransformerImpl() {
    assertThrows(NullPointerException.class, () -> new CollectionsQueryTransformerImpl(null, null));
  }

  @SuppressWarnings("UnnecessaryLocalVariable")
  // squids: nested code blocks should be extracted & using the same literal string multiple times
  static Iterator<Object[]> nonTransformableQueryProvider() {
    setExpandedDiseaseEntities();

    List<Object[]> dataList = new ArrayList<>();

    {
      Query query = new QueryImpl<>().eq("field", "value");
      dataList.add(new Object[] {query, query});
    }
    {
      Query query = new QueryImpl<>().search("value").or().eq("field", "value");
      dataList.add(new Object[] {query, query});
    }
    {
      Query query = new QueryImpl<>().search("disease");
      dataList.add(new Object[] {query, query});
    }

    return dataList.iterator();
  }

  @ParameterizedTest
  @MethodSource("nonTransformableQueryProvider")
  void testNonTransformableQueries(Query<Entity> query, Query<Entity> expectedTransformedQuery) {
    Query<Entity> transformedQuery =
        collectionsQueryTransformerImpl.transformQuery(
            query, ICD10_ENTITY_TYPE_ID, EXPAND_ATTRIBUTE);
    assertEquals(expectedTransformedQuery, transformedQuery);
  }

  static Iterator<Object[]> transformableQueryProvider() {
    setExpandedDiseaseEntities();

    List<Object[]> dataList = new ArrayList<>();

    {
      Query query = new QueryImpl<>().eq(EXPAND_ATTRIBUTE, "disease");
      Query expected = new QueryImpl<>().in(EXPAND_ATTRIBUTE, expandedDiseaseEntities);
      dataList.add(new Object[] {query, expected});
    }
    {
      Query query = new QueryImpl<>().in(EXPAND_ATTRIBUTE, singletonList("disease"));
      Query expected = new QueryImpl<>().in(EXPAND_ATTRIBUTE, expandedDiseaseEntities);
      dataList.add(new Object[] {query, expected});
    }
    {
      Query query = new QueryImpl<>().in(EXPAND_ATTRIBUTE, asList("disease", "disease2"));
      Query expected = new QueryImpl<>().in(EXPAND_ATTRIBUTE, expandedDiseaseEntities);
      dataList.add(new Object[] {query, expected});
    }
    {
      Query query = new QueryImpl<>().in(EXPAND_ATTRIBUTE, asList("disease", "unknown disease"));
      Query expected = new QueryImpl<>().in(EXPAND_ATTRIBUTE, expandedDiseaseEntities);
      dataList.add(new Object[] {query, expected});
    }
    {
      Query query = new QueryImpl<>().eq(EXPAND_ATTRIBUTE, "disease").and().eq("otherAttr", "test");
      Query expected =
          new QueryImpl<>()
              .in(EXPAND_ATTRIBUTE, expandedDiseaseEntities)
              .and()
              .eq("otherAttr", "test");
      dataList.add(new Object[] {query, expected});
    }
    {
      Query query =
          new QueryImpl<>()
              .in(EXPAND_ATTRIBUTE, singletonList("disease"))
              .or()
              .in("otherAttr", singletonList("test"));
      Query expected =
          new QueryImpl<>()
              .in(EXPAND_ATTRIBUTE, expandedDiseaseEntities)
              .or()
              .in("otherAttr", singletonList("test"));
      dataList.add(new Object[] {query, expected});
    }
    {
      Query query =
          new QueryImpl<>().in(EXPAND_ATTRIBUTE, singletonList("disease")).and().search("test");
      Query expected =
          new QueryImpl<>().in(EXPAND_ATTRIBUTE, expandedDiseaseEntities).and().search("test");
      dataList.add(new Object[] {query, expected});
    }

    return dataList.iterator();
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @MethodSource("transformableQueryProvider")
  void testTransformableQueries(Query<Entity> query, Query<Entity> expectedTransformedQuery) {
    when(dataService.findAll(eq(ICD10_ENTITY_TYPE_ID), any(Stream.class)))
        .thenReturn(Stream.of(diseaseEntity));
    when(icd10ClassExpander.expandClasses(singletonList(diseaseEntity)))
        .thenReturn(expandedDiseaseEntities);

    Query<Entity> transformedQuery =
        collectionsQueryTransformerImpl.transformQuery(
            query, ICD10_ENTITY_TYPE_ID, EXPAND_ATTRIBUTE);
    assertEquals(expectedTransformedQuery, transformedQuery);
  }
}
