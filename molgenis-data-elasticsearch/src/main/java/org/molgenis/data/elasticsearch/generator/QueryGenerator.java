package org.molgenis.data.elasticsearch.generator;

import static java.lang.String.format;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH_NGRAM;
import static org.molgenis.data.QueryRule.Operator.GREATER;
import static org.molgenis.data.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.data.QueryRule.Operator.IN;
import static org.molgenis.data.QueryRule.Operator.LESS;
import static org.molgenis.data.QueryRule.Operator.LESS_EQUAL;
import static org.molgenis.data.QueryRule.Operator.LIKE;
import static org.molgenis.data.QueryRule.Operator.RANGE;
import static org.molgenis.data.QueryRule.Operator.SEARCH;

import java.util.EnumMap;
import java.util.List;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

/** Generates Elasticsearch queries from MOLGENIS queries. */
@Component
public class QueryGenerator {
  static final String ATTRIBUTE_SEPARATOR = ".";

  private final EnumMap<Operator, QueryClauseGenerator> queryClauseGeneratorMap;

  public QueryGenerator(DocumentIdGenerator documentIdGenerator) {
    queryClauseGeneratorMap = new EnumMap<>(Operator.class);
    queryClauseGeneratorMap.put(EQUALS, new QueryClauseEqualsGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(
        FUZZY_MATCH, new QueryClauseFuzzyMatchGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(
        FUZZY_MATCH_NGRAM, new QueryClauseFuzzyMatchNgramGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(GREATER, new QueryClauseGreaterGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(
        GREATER_EQUAL, new QueryClauseGreaterEqualGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(IN, new QueryClauseInGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(LESS, new QueryClauseLessGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(LESS_EQUAL, new QueryClauseLessEqualGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(LIKE, new QueryClauseLikeGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(RANGE, new QueryClauseRangeGenerator(documentIdGenerator));
    queryClauseGeneratorMap.put(SEARCH, new QueryClauseSearchGenerator(documentIdGenerator));
  }

  QueryBuilder createQueryBuilder(Query<Entity> query, EntityType entityType) {
    return createQueryBuilder(query.getRules(), entityType);
  }

  private QueryBuilder createQueryBuilder(List<QueryRule> queryRules, EntityType entityType) {
    QueryBuilder queryBuilder;

    final int nrQueryRules = queryRules.size();
    if (nrQueryRules == 1) {
      // simple query consisting of one query clause
      queryBuilder = createQueryClause(queryRules.get(0), entityType);
    } else {
      // boolean query consisting of combination of query clauses
      QueryRule.Operator occur = null;
      BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

      for (int i = 0; i < nrQueryRules; i += 2) {
        QueryRule queryRule = queryRules.get(i);

        // determine whether this query is a 'not' query
        if (queryRule.getOperator() == QueryRule.Operator.NOT) {
          occur = QueryRule.Operator.NOT;
          queryRule = queryRules.get(i + 1);
          i += 1;
        } else if (i + 1 < nrQueryRules) {
          QueryRule occurQueryRule = queryRules.get(i + 1);
          QueryRule.Operator occurOperator = occurQueryRule.getOperator();
          if (occurOperator == null)
            throw new MolgenisQueryException("Missing expected occur operator");

          //noinspection EnumSwitchStatementWhichMissesCases
          switch (occurOperator) {
            case AND:
            case OR:
              if (occur != null && occurOperator != occur) {
                throw new MolgenisQueryException(
                    "Mixing query operators not allowed, use nested queries");
              }
              occur = occurOperator;
              break;
            default:
              throw new MolgenisQueryException(
                  "Expected query occur operator instead of [" + occurOperator + "]");
          }
        }

        QueryBuilder queryPartBuilder = createQueryClause(queryRule, entityType);
        if (queryPartBuilder == null) continue; // skip SHOULD and DIS_MAX query rules

        //noinspection ConstantConditions
        if (occur == null) {
          throw new IllegalStateException("occur shouldn't be null");
        }

        // add query part to query
        switch (occur) {
          case AND:
            boolQuery.must(queryPartBuilder);
            break;
          case OR:
            boolQuery.should(queryPartBuilder).minimumShouldMatch(1);
            break;
          case NOT:
            boolQuery.mustNot(queryPartBuilder);
            break;
          default:
            throw new UnexpectedEnumException(occur);
        }
      }
      queryBuilder = boolQuery;
    }
    return queryBuilder;
  }

  private QueryBuilder createQueryClause(QueryRule queryRule, EntityType entityType) {
    QueryRule.Operator queryOperator = queryRule.getOperator();
    switch (queryOperator) {
      case DIS_MAX:
        return createQueryClauseDisMax(queryRule, entityType);
      case NESTED:
        return createQueryClauseNested(queryRule, entityType);
      case SHOULD:
        return createQueryClauseShould(queryRule, entityType);
      case AND:
      case OR:
      case NOT:
        throw new MolgenisQueryException(
            format("Unexpected query operator [%s]", queryOperator.toString()));
      default:
        QueryClauseGenerator queryClauseGenerator = queryClauseGeneratorMap.get(queryOperator);
        if (queryClauseGenerator == null) {
          throw new UnexpectedEnumException(queryOperator);
        }
        return queryClauseGenerator.createQueryClause(queryRule, entityType);
    }
  }

  private QueryBuilder createQueryClauseNested(QueryRule queryRule, EntityType entityType) {
    List<QueryRule> nestedQueryRules = queryRule.getNestedRules();
    if (nestedQueryRules == null || nestedQueryRules.isEmpty()) {
      throw new MolgenisQueryException("Missing nested rules for nested query");
    }
    return createQueryBuilder(nestedQueryRules, entityType);
  }

  private QueryBuilder createQueryClauseShould(QueryRule queryRule, EntityType entityType) {
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    for (QueryRule subQuery : queryRule.getNestedRules()) {
      boolQueryBuilder.should(createQueryClause(subQuery, entityType));
    }
    return boolQueryBuilder;
  }

  private QueryBuilder createQueryClauseDisMax(QueryRule queryRule, EntityType entityType) {
    DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
    for (QueryRule nestedQueryRule : queryRule.getNestedRules()) {
      disMaxQueryBuilder.add(createQueryClause(nestedQueryRule, entityType));
    }
    disMaxQueryBuilder.tieBreaker((float) 0.0);
    if (queryRule.getValue() != null) {
      disMaxQueryBuilder.boost(Float.parseFloat(queryRule.getValue().toString()));
    }
    return disMaxQueryBuilder;
  }
}
