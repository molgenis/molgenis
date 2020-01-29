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

class QueryClauseSearchGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  private QueryClauseSearchGenerator queryClauseSearchGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    queryClauseSearchGenerator = new QueryClauseSearchGenerator(documentIdGenerator);
  }

  @Test
  void mapQueryRuleAllAttributeSearch() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getValue()).thenReturn("val");

    EntityType entityType = mock(EntityType.class);

    QueryBuilder queryBuilder = queryClauseSearchGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder = QueryBuilders.matchPhraseQuery("_all", "val").slop(10);
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleAttributeSearch() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn("val");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseSearchGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder = QueryBuilders.matchQuery("attr", "val");
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }
}
