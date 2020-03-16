package org.molgenis.data.elasticsearch.generator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.elasticsearch.generator.QueryBuilderAssertions.assertQueryBuilderEquals;

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

class QueryClauseFuzzyMatchGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  private QueryClauseFuzzyMatchGenerator queryClauseFuzzyMatchGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    queryClauseFuzzyMatchGenerator = new QueryClauseFuzzyMatchGenerator(documentIdGenerator);
  }

  @Test
  void mapQueryRule() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn("val");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseFuzzyMatchGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder = QueryBuilders.queryStringQuery("attr:(val)");
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @SuppressWarnings("deprecation")
  @Test
  void mapQueryRuleIllegalAttributeType() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn("val");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.BOOL);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    assertThrows(
        MolgenisQueryException.class,
        () -> queryClauseFuzzyMatchGenerator.mapQueryRule(queryRule, entityType));
  }
}
