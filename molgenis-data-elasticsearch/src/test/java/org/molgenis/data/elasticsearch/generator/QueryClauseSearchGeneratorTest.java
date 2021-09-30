package org.molgenis.data.elasticsearch.generator;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.elasticsearch.generator.QueryBuilderAssertions.assertQueryBuilderEquals;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.util.List;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class QueryClauseSearchGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  @Mock Attribute attribute1;
  @Mock Attribute attribute2;
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
    QueryBuilder expectedQueryBuilder = boolQuery().should(multiMatchQuery("val"));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleAllAttributeSearchesChildren() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getValue()).thenReturn("val");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAtomicAttributes()).thenReturn(List.of(attribute1, attribute2));
    when(attribute1.getDataType()).thenReturn(STRING);
    when(attribute2.getDataType()).thenReturn(XREF);
    when(documentIdGenerator.generateId(attribute2)).thenReturn("child");

    QueryBuilder queryBuilder = queryClauseSearchGenerator.mapQueryRule(queryRule, entityType);
    var expectedQueryBuilder = boolQuery().should(multiMatchQuery("val"));
    expectedQueryBuilder.should().add(nestedQuery("child", multiMatchQuery("val"), ScoreMode.Max));
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @SuppressWarnings("deprecation")
  @Test
  void mapQueryRuleAllAttributeSearchNullValue() {
    QueryRule queryRule = mock(QueryRule.class);
    EntityType entityType = mock(EntityType.class);
    assertThrows(
        MolgenisQueryException.class,
        () -> queryClauseSearchGenerator.mapQueryRule(queryRule, entityType));
  }

  @Test
  void mapQueryRuleAttributeSearch() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn("val");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(STRING);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseSearchGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder = QueryBuilders.matchQuery("attr", "val");
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }

  @Test
  void mapQueryRuleAttributeSearchReferencedAttribute() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");
    when(queryRule.getValue()).thenReturn("val");

    Attribute refIdAttribute = mock(Attribute.class);
    when(refIdAttribute.getDataType()).thenReturn(STRING);
    EntityType refEntityType = mock(EntityType.class);
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);
    when(documentIdGenerator.generateId(refIdAttribute)).thenReturn("refAttr");
    Attribute attribute = mock(Attribute.class);
    when(attribute.hasRefEntity()).thenReturn(true);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIndexingDepth()).thenReturn(1);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    QueryBuilder queryBuilder = queryClauseSearchGenerator.mapQueryRule(queryRule, entityType);
    QueryBuilder expectedQueryBuilder =
        QueryBuilders.nestedQuery(
            "attr", QueryBuilders.matchQuery("attr.refAttr", "val"), ScoreMode.Avg);
    assertQueryBuilderEquals(expectedQueryBuilder, queryBuilder);
  }
}
