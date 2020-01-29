package org.molgenis.data.elasticsearch.generator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.elasticsearch.generator.QueryBuilderAssertions.assertQueryBuilderEquals;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class QueryClauseInGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  private QueryClauseInGenerator queryClauseInGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    queryClauseInGenerator = new QueryClauseInGenerator(documentIdGenerator);
  }

  @Test
  void mapQueryRuleNonNullValues() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(asList("val0", "val1"));

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseInGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.constantScoreQuery(QueryBuilders.termsQuery("attr.raw", "val0", "val1"));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleNonNullIntValues() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(asList(0, 1));

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.INT);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseInGenerator.mapQueryRule(queryRule, entityType);
    @SuppressWarnings("RedundantArrayCreation")
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.constantScoreQuery(QueryBuilders.termsQuery("attr", new int[] {0, 1}));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleNullValue() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(singletonList(null));

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseInGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.constantScoreQuery(
            QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("attr")));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleNonNullValuesAndNullValue() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(asList("val0", "val1", null));

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseInGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.constantScoreQuery(
            QueryBuilders.boolQuery()
                .minimumShouldMatch(1)
                .should(QueryBuilders.termsQuery("attr.raw", "val0", "val1"))
                .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("attr"))));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }
}
