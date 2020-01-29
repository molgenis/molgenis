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

class QueryClauseLikeGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  private QueryClauseLikeGenerator queryClauseLikeGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    queryClauseLikeGenerator = new QueryClauseLikeGenerator(documentIdGenerator);
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

    QueryBuilder queryBuilder = queryClauseLikeGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.matchPhrasePrefixQuery("attr", "val")
            .maxExpansions(50)
            .slop(10)
            .analyzer("default");
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleUnsupportedType() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(0);

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.TEXT);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    assertThrows(
        UnsupportedOperationException.class,
        () -> queryClauseLikeGenerator.mapQueryRule(queryRule, entityType));
  }

  @SuppressWarnings("deprecation")
  @Test
  void mapQueryRuleIllegalType() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn(0);

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.BOOL);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    assertThrows(
        MolgenisQueryException.class,
        () -> queryClauseLikeGenerator.mapQueryRule(queryRule, entityType));
  }
}
