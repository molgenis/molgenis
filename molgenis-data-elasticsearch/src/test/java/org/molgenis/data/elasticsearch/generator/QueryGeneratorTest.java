package org.molgenis.data.elasticsearch.generator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.DIS_MAX;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.NESTED;
import static org.molgenis.data.QueryRule.Operator.NOT;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.data.QueryRule.Operator.SHOULD;

import java.util.EnumMap;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

/** See {@link QueryGeneratorIT} */
class QueryGeneratorTest extends AbstractMockitoTest {
  @Mock private DocumentIdGenerator documentIdGenerator;
  @Mock private QueryClauseGenerator queryClauseGenerator;
  private QueryGenerator queryGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    queryGenerator = new QueryGenerator(documentIdGenerator);

    EnumMap<Operator, QueryClauseGenerator> queryClauseGeneratorMap = new EnumMap<>(Operator.class);
    queryClauseGeneratorMap.put(EQUALS, queryClauseGenerator);
    queryGenerator.setQueryClauseGeneratorMap(queryClauseGeneratorMap);
  }

  @Test
  void testCreateQueryBuilderEquals() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getOperator()).thenReturn(EQUALS);
    when(query.getRules()).thenReturn(singletonList(queryRule));
    EntityType entityType = mock(EntityType.class);
    QueryBuilder queryBuilder = mock(QueryBuilder.class);
    when(queryClauseGenerator.createQueryClause(queryRule, entityType)).thenReturn(queryBuilder);
    assertEquals(queryBuilder, queryGenerator.createQueryBuilder(query, entityType));
  }

  @Test
  void testCreateQueryBuilderShould() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    QueryRule nestedQueryRule = mock(QueryRule.class);
    when(nestedQueryRule.getOperator()).thenReturn(EQUALS);
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getOperator()).thenReturn(SHOULD);
    when(queryRule.getNestedRules()).thenReturn(singletonList(nestedQueryRule));
    when(query.getRules()).thenReturn(singletonList(queryRule));
    EntityType entityType = mock(EntityType.class);
    QueryBuilder queryBuilder = mock(QueryBuilder.class);
    when(queryClauseGenerator.createQueryClause(nestedQueryRule, entityType))
        .thenReturn(queryBuilder);
    assertEquals(
        QueryBuilders.boolQuery().should(queryBuilder),
        queryGenerator.createQueryBuilder(query, entityType));
  }

  @Test
  void testCreateQueryBuilderDisMax() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    QueryRule nestedQueryRule = mock(QueryRule.class);
    when(nestedQueryRule.getOperator()).thenReturn(EQUALS);
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getOperator()).thenReturn(DIS_MAX);
    when(queryRule.getNestedRules()).thenReturn(singletonList(nestedQueryRule));
    when(query.getRules()).thenReturn(singletonList(queryRule));
    EntityType entityType = mock(EntityType.class);
    QueryBuilder queryBuilder = mock(QueryBuilder.class);
    when(queryClauseGenerator.createQueryClause(nestedQueryRule, entityType))
        .thenReturn(queryBuilder);
    assertEquals(
        QueryBuilders.disMaxQuery().add(queryBuilder),
        queryGenerator.createQueryBuilder(query, entityType));
  }

  @Test
  void testCreateQueryBuilderDisMaxBoost() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    QueryRule nestedQueryRule = mock(QueryRule.class);
    when(nestedQueryRule.getOperator()).thenReturn(EQUALS);
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getOperator()).thenReturn(DIS_MAX);
    when(queryRule.getValue()).thenReturn(1.23f);
    when(queryRule.getNestedRules()).thenReturn(singletonList(nestedQueryRule));
    when(query.getRules()).thenReturn(singletonList(queryRule));
    EntityType entityType = mock(EntityType.class);
    QueryBuilder queryBuilder = mock(QueryBuilder.class);
    when(queryClauseGenerator.createQueryClause(nestedQueryRule, entityType))
        .thenReturn(queryBuilder);
    assertEquals(
        QueryBuilders.disMaxQuery().boost(1.23f).add(queryBuilder),
        queryGenerator.createQueryBuilder(query, entityType));
  }

  @Test
  void testCreateQueryBuilderNested() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    QueryRule nestedQueryRule = mock(QueryRule.class);
    when(nestedQueryRule.getOperator()).thenReturn(EQUALS);
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getOperator()).thenReturn(NESTED);
    when(queryRule.getNestedRules()).thenReturn(singletonList(nestedQueryRule));
    when(query.getRules()).thenReturn(singletonList(queryRule));
    EntityType entityType = mock(EntityType.class);
    QueryBuilder queryBuilder = mock(QueryBuilder.class);
    when(queryClauseGenerator.createQueryClause(nestedQueryRule, entityType))
        .thenReturn(queryBuilder);
    assertEquals(queryBuilder, queryGenerator.createQueryBuilder(query, entityType));
  }

  @Test
  void testCreateQueryBuilderAnd() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    QueryRule queryRule0 = mock(QueryRule.class);
    when(queryRule0.getOperator()).thenReturn(EQUALS);
    QueryRule queryRule1 = mock(QueryRule.class);
    when(queryRule1.getOperator()).thenReturn(EQUALS);
    QueryRule andQueryRule = mock(QueryRule.class);
    when(andQueryRule.getOperator()).thenReturn(AND);
    when(query.getRules()).thenReturn(asList(queryRule0, andQueryRule, queryRule1));
    EntityType entityType = mock(EntityType.class);
    QueryBuilder queryBuilder0 = mock(QueryBuilder.class);
    doReturn(queryBuilder0).when(queryClauseGenerator).createQueryClause(queryRule0, entityType);
    QueryBuilder queryBuilder1 = mock(QueryBuilder.class);
    doReturn(queryBuilder1).when(queryClauseGenerator).createQueryClause(queryRule1, entityType);
    assertEquals(
        QueryBuilders.boolQuery().must(queryBuilder0).must(queryBuilder1),
        queryGenerator.createQueryBuilder(query, entityType));
  }

  @Test
  void testCreateQueryBuilderOr() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    QueryRule queryRule0 = mock(QueryRule.class);
    when(queryRule0.getOperator()).thenReturn(EQUALS);
    QueryRule queryRule1 = mock(QueryRule.class);
    when(queryRule1.getOperator()).thenReturn(EQUALS);
    QueryRule orQueryRule = mock(QueryRule.class);
    when(orQueryRule.getOperator()).thenReturn(OR);
    when(query.getRules()).thenReturn(asList(queryRule0, orQueryRule, queryRule1));
    EntityType entityType = mock(EntityType.class);
    QueryBuilder queryBuilder0 = mock(QueryBuilder.class);
    doReturn(queryBuilder0).when(queryClauseGenerator).createQueryClause(queryRule0, entityType);
    QueryBuilder queryBuilder1 = mock(QueryBuilder.class);
    doReturn(queryBuilder1).when(queryClauseGenerator).createQueryClause(queryRule1, entityType);
    assertEquals(
        QueryBuilders.boolQuery().minimumShouldMatch(1).should(queryBuilder0).should(queryBuilder1),
        queryGenerator.createQueryBuilder(query, entityType));
  }

  @Test
  void testCreateQueryBuilderNot() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getOperator()).thenReturn(EQUALS);
    QueryRule notQueryRule = mock(QueryRule.class);
    when(notQueryRule.getOperator()).thenReturn(NOT);
    when(query.getRules()).thenReturn(asList(notQueryRule, queryRule));
    EntityType entityType = mock(EntityType.class);
    QueryBuilder queryBuilder = mock(QueryBuilder.class);
    doReturn(queryBuilder).when(queryClauseGenerator).createQueryClause(queryRule, entityType);
    assertEquals(
        QueryBuilders.boolQuery().mustNot(queryBuilder),
        queryGenerator.createQueryBuilder(query, entityType));
  }
}
