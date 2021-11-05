package org.molgenis.data.elasticsearch.generator;

import static org.elasticsearch.index.query.Operator.AND;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryStringQuery;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.molgenis.data.elasticsearch.generator.QueryBuilderAssertions.assertQueryBuilderEquals;
import static org.molgenis.data.meta.AttributeType.STRING;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

@ExtendWith(MockitoExtension.class)
class QueryClauseSearchQueryGeneratorTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  @Mock QueryRule queryRule;
  @Mock EntityType entityType;
  @Mock Attribute attribute;
  @Mock EntityType refEntityType;
  @Mock Attribute refIdAttribute;

  private QueryClauseSearchQueryGenerator searchQueryGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    searchQueryGenerator = new QueryClauseSearchQueryGenerator(documentIdGenerator);
  }

  @Test
  void mapQueryRuleAllAttributeSearch() {
    when(queryRule.getValue()).thenReturn("val");
    QueryBuilder queryBuilder = searchQueryGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder = simpleQueryStringQuery("val").defaultOperator(AND);
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @SuppressWarnings("deprecation")
  @Test
  void mapQueryRuleAllAttributeSearchNullValue() {
    assertThrows(
        MolgenisQueryException.class,
        () -> searchQueryGenerator.mapQueryRule(queryRule, entityType));
  }

  @Test
  void mapQueryRuleAttributeSearch() {
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn("val");

    when(entityType.getAttributeByName("attr")).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    QueryBuilder queryBuilder = searchQueryGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        simpleQueryStringQuery("val").field("attr").defaultOperator(AND);
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleAttributeSearchReferencedAttribute() {
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn("val");

    when(entityType.getIndexingDepth()).thenReturn(1);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);
    when(attribute.hasRefEntity()).thenReturn(true);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(refIdAttribute.getDataType()).thenReturn(STRING);
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);
    when(documentIdGenerator.generateId(refIdAttribute)).thenReturn("refAttr");
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    QueryBuilder queryBuilder = searchQueryGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.nestedQuery(
            "attr",
            simpleQueryStringQuery("val").field("attr.refAttr").defaultOperator(AND),
            ScoreMode.Avg);
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }
}
