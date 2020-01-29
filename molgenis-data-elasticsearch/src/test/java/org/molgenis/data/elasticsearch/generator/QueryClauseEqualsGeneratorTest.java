package org.molgenis.data.elasticsearch.generator;

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

class QueryClauseEqualsGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  private QueryClauseEqualsGenerator queryClauseEqualsGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    queryClauseEqualsGenerator = new QueryClauseEqualsGenerator(documentIdGenerator);
  }

  @Test
  void mapQueryRuleNonNullValues() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn("val");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseEqualsGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("attr.raw", "val"));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleNonNullIntValues() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(0);

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.INT);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseEqualsGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("attr", 0));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleNullValue() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseEqualsGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.constantScoreQuery(
            QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("attr")));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }
}
