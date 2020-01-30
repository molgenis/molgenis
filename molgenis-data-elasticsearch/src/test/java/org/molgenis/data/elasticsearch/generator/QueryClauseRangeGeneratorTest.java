package org.molgenis.data.elasticsearch.generator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.elasticsearch.generator.QueryBuilderAssertions.assertQueryBuilderEquals;

import com.google.common.collect.ImmutableList;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class QueryClauseRangeGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  private QueryClauseRangeGenerator queryClauseRangeGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    queryClauseRangeGenerator = new QueryClauseRangeGenerator(documentIdGenerator);
  }

  @Test
  void mapQueryRule() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(ImmutableList.of(2, 4));

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.INT);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseRangeGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.constantScoreQuery(QueryBuilders.rangeQuery("attr").from(2).to(4));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @SuppressWarnings("deprecation")
  @Test
  void mapQueryRuleNullValue() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.INT);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    assertThrows(
        MolgenisQueryException.class,
        () -> queryClauseRangeGenerator.mapQueryRule(queryRule, entityType));
  }

  @SuppressWarnings("deprecation")
  @Test
  void mapQueryRuleNonRangeValue() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(2);

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.INT);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    assertThrows(
        MolgenisQueryException.class,
        () -> queryClauseRangeGenerator.mapQueryRule(queryRule, entityType));
  }
}
