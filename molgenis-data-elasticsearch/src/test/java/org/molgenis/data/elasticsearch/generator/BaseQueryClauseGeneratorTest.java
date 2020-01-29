package org.molgenis.data.elasticsearch.generator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class BaseQueryClauseGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  private BaseQueryClauseGenerator queryGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    queryGenerator =
        new BaseQueryClauseGenerator(documentIdGenerator, Operator.EQUALS) {
          @Override
          QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {
            return null;
          }
        };
  }

  @Test
  void testNestedQueryBuilderSizeOne() {
    EntityType entityType = when(mock(EntityType.class).getIndexingDepth()).thenReturn(1).getMock();
    String fieldName = "attribute";
    String queryValue = "value";
    QueryBuilder queryBuilder = termQuery(fieldName, queryValue);

    Attribute attribute = mock(Attribute.class);
    QueryBuilder nestedQueryBuilder =
        queryGenerator.nestedQueryBuilder(entityType, singletonList(attribute), queryBuilder);
    assertQueryBuilderEquals(nestedQueryBuilder, queryBuilder);
  }

  @Test
  void testNestedQueryBuilderSizeTwo() {
    EntityType entityType = when(mock(EntityType.class).getIndexingDepth()).thenReturn(1).getMock();
    String parentFieldName = "parent";
    String childFieldName = "child";
    String queryValue = "value";
    QueryBuilder queryBuilder = termQuery(parentFieldName + '.' + childFieldName, queryValue);

    Attribute parentAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(parentAttribute)).thenReturn(parentFieldName);
    Attribute childAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(childAttribute)).thenReturn(childFieldName);
    QueryBuilder nestedQueryBuilder =
        queryGenerator.nestedQueryBuilder(
            entityType, asList(parentAttribute, childAttribute), queryBuilder);

    QueryBuilder expectedQueryBuilder =
        QueryBuilders.nestedQuery(
            parentFieldName,
            termQuery(parentFieldName + '.' + childFieldName, queryValue),
            ScoreMode.Avg);
    assertQueryBuilderEquals(nestedQueryBuilder, expectedQueryBuilder);
  }

  @Test
  void testNestedQueryBuilderSizeThree() {
    EntityType entityType = when(mock(EntityType.class).getIndexingDepth()).thenReturn(2).getMock();
    String grandparentFieldName = "grandparent";
    String parentFieldName = "parent";
    String childFieldName = "child";
    String queryValue = "value";
    QueryBuilder queryBuilder =
        termQuery(grandparentFieldName + '.' + parentFieldName + '.' + childFieldName, queryValue);

    Attribute grandparentAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(grandparentAttribute)).thenReturn(grandparentFieldName);
    Attribute parentAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(parentAttribute)).thenReturn(parentFieldName);
    Attribute childAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(childAttribute)).thenReturn(childFieldName);
    QueryBuilder nestedQueryBuilder =
        queryGenerator.nestedQueryBuilder(
            entityType,
            asList(grandparentAttribute, parentAttribute, childAttribute),
            queryBuilder);

    QueryBuilder expectedQueryBuilder =
        QueryBuilders.nestedQuery(
            grandparentFieldName,
            QueryBuilders.nestedQuery(
                grandparentFieldName + '.' + parentFieldName,
                termQuery(
                    grandparentFieldName + '.' + parentFieldName + '.' + childFieldName,
                    queryValue),
                ScoreMode.Avg),
            ScoreMode.Avg);
    assertQueryBuilderEquals(nestedQueryBuilder, expectedQueryBuilder);
  }

  @Test
  void testNestedQueryBuilderSizeFour() {
    EntityType entityType = when(mock(EntityType.class).getIndexingDepth()).thenReturn(3).getMock();
    String greatGrandparentFieldName = "grandGrandparent";
    String grandparentFieldName = "grandparent";
    String parentFieldName = "parent";
    String childFieldName = "child";
    String queryValue = "value";
    QueryBuilder queryBuilder =
        termQuery(
            greatGrandparentFieldName
                + '.'
                + grandparentFieldName
                + '.'
                + parentFieldName
                + '.'
                + childFieldName,
            queryValue);

    Attribute greatGrandparentAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(greatGrandparentAttribute))
        .thenReturn(greatGrandparentFieldName);
    Attribute grandparentAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(grandparentAttribute)).thenReturn(grandparentFieldName);
    Attribute parentAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(parentAttribute)).thenReturn(parentFieldName);
    Attribute childAttribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(childAttribute)).thenReturn(childFieldName);
    QueryBuilder nestedQueryBuilder =
        queryGenerator.nestedQueryBuilder(
            entityType,
            asList(
                greatGrandparentAttribute, grandparentAttribute, parentAttribute, childAttribute),
            queryBuilder);

    QueryBuilder expectedQueryBuilder =
        QueryBuilders.nestedQuery(
            greatGrandparentFieldName,
            QueryBuilders.nestedQuery(
                greatGrandparentFieldName + '.' + grandparentFieldName,
                QueryBuilders.nestedQuery(
                    greatGrandparentFieldName + '.' + grandparentFieldName + '.' + parentFieldName,
                    termQuery(
                        greatGrandparentFieldName
                            + '.'
                            + grandparentFieldName
                            + '.'
                            + parentFieldName
                            + '.'
                            + childFieldName,
                        queryValue),
                    ScoreMode.Avg),
                ScoreMode.Avg),
            ScoreMode.Avg);
    assertQueryBuilderEquals(nestedQueryBuilder, expectedQueryBuilder);
  }

  private void assertQueryBuilderEquals(QueryBuilder actual, QueryBuilder expected) {
    // QueryBuilder classes do not implement equals
    assertEquals(
        expected.toString().replaceAll("\\s", ""), actual.toString().replaceAll("\\s", ""));
  }
}
