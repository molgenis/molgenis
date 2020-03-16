package org.molgenis.data.elasticsearch.generator;

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

    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
    switch (getOperator()) {
      case GREATER:
        rangeQueryBuilder.gt(queryValue);
        break;
      case GREATER_EQUAL:
        rangeQueryBuilder.gte(queryValue);
        break;
      case LESS:
        rangeQueryBuilder.lt(queryValue);
        break;
      case LESS_EQUAL:
        rangeQueryBuilder.lte(queryValue);
        break;
      default:
        throw new UnexpectedEnumException(getOperator());
    }
    return QueryBuilders.constantScoreQuery(
        nestedQueryBuilder(entityType, attributePath, rangeQueryBuilder));
  }
}
