package org.molgenis.data.elasticsearch.generator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.time.Instant;
import java.time.LocalDate;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
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
  void testGetOperator() {
    assertEquals(Operator.EQUALS, queryGenerator.getOperator());
  }

  @Test
  void testGetQueryFieldName() {
    String fieldName = "MyFieldId";
    Attribute attribute = mock(Attribute.class);
    when(documentIdGenerator.generateId(attribute)).thenReturn(fieldName);
    assertEquals(fieldName, queryGenerator.getQueryFieldName(attribute));
  }

  @Test
  void testGetQueryFieldNameList() {
    String fieldName0 = "MyFieldId1";
    Attribute attribute0 = mock(Attribute.class);
    doReturn(fieldName0).when(documentIdGenerator).generateId(attribute0);
    String fieldName1 = "MyFieldId0";
    Attribute attribute1 = mock(Attribute.class);
    doReturn(fieldName1).when(documentIdGenerator).generateId(attribute1);
    assertEquals(
        "MyFieldId1.MyFieldId0", queryGenerator.getQueryFieldName(asList(attribute0, attribute1)));
  }

  @Test
  void testUseNotAnalyzedFieldFalse() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    assertFalse(queryGenerator.useNotAnalyzedField(attribute));
  }

  @Test
  void testUseNotAnalyzedFieldTrue() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    assertTrue(queryGenerator.useNotAnalyzedField(attribute));
  }

  @Test
  void testUseNotAnalyzedFieldXrefFalse() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    EntityType refEntityType = mock(EntityType.class);
    Attribute refIdAttribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    assertFalse(queryGenerator.useNotAnalyzedField(attribute));
  }

  @Test
  void testUseNotAnalyzedFieldXrefTrue() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    EntityType refEntityType = mock(EntityType.class);
    Attribute refIdAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    assertTrue(queryGenerator.useNotAnalyzedField(attribute));
  }

  @SuppressWarnings("deprecation")
  @Test
  void testUseNotAnalyzedFieldCompound() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    assertThrows(MolgenisQueryException.class, () -> queryGenerator.useNotAnalyzedField(attribute));
  }

  @Test
  void testQueryValue() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    assertEquals("string", queryGenerator.getQueryValue(attribute, "string"));
  }

  @Test
  void testQueryValueXref() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    Entity entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn("MyId");
    assertEquals("MyId", queryGenerator.getQueryValue(attribute, entity));
  }

  @Test
  void testQueryValueDate() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(DATE).getMock();
    LocalDate localDate = LocalDate.now();
    assertEquals(localDate.toString(), queryGenerator.getQueryValue(attribute, localDate));
  }

  @Test
  void testQueryValueDateTime() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(DATE_TIME).getMock();
    Instant instant = Instant.now();
    assertEquals(instant.toString(), queryGenerator.getQueryValue(attribute, instant));
  }

  @SuppressWarnings("deprecation")
  @Test
  void testQueryValueCompound() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    assertThrows(MolgenisQueryException.class, () -> queryGenerator.getQueryValue(attribute, "x"));
  }

  @Test
  void testQueryValues() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    assertEquals(
        asList("string0", "string1"),
        queryGenerator.getQueryValues(attribute, asList("string0", "string1")));
  }

  @Test
  void testValidateNumericalQueryFieldInt() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    assertDoesNotThrow(() -> queryGenerator.validateNumericalQueryField(attribute));
  }

  @SuppressWarnings("deprecation")
  @Test
  void testValidateNumericalQueryFieldBool() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.validateNumericalQueryField(attribute));
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
