package org.molgenis.data.elasticsearch.generator;

import static java.lang.String.format;
import static org.molgenis.data.QueryUtils.getAttributePathExpanded;

import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

abstract class BaseQueryClauseRangeGenerator extends BaseQueryClauseGenerator {
  BaseQueryClauseRangeGenerator(DocumentIdGenerator documentIdGenerator, Operator operator) {
    super(documentIdGenerator, operator);
  }

  QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {
    List<Attribute> attributePath = getAttributePathExpanded(queryRule.getField(), entityType);
    Attribute attr = attributePath.get(attributePath.size() - 1);
    validateNumericalQueryField(attr);
    String fieldName = getQueryFieldName(attributePath);

    Object queryValue = getQueryValue(attr, queryRule.getValue());
    if (queryValue == null) {
      throw new MolgenisQueryException(QUERY_VALUE_CANNOT_BE_NULL_MSG);
    }

    RangeQueryBuilder filterBuilder = QueryBuilders.rangeQuery(fieldName);
    QueryRule.Operator operator = queryRule.getOperator();
    switch (operator) {
      case GREATER:
        filterBuilder = filterBuilder.gt(queryValue);
        break;
      case GREATER_EQUAL:
        filterBuilder = filterBuilder.gte(queryValue);
        break;
      case LESS:
        filterBuilder = filterBuilder.lt(queryValue);
        break;
      case LESS_EQUAL:
        filterBuilder = filterBuilder.lte(queryValue);
        break;
      case AND:
      case DIS_MAX:
      case EQUALS:
      case FUZZY_MATCH:
      case FUZZY_MATCH_NGRAM:
      case IN:
      case LIKE:
      case NESTED:
      case NOT:
      case OR:
      case RANGE:
      case SEARCH:
      case SHOULD:
        throw new MolgenisQueryException(
            format("Illegal query rule operator [%s]", operator.toString()));
      default:
        throw new UnexpectedEnumException(operator);
    }

    return QueryBuilders.constantScoreQuery(
        nestedQueryBuilder(entityType, attributePath, filterBuilder));
  }
}
