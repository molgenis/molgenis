package org.molgenis.data.elasticsearch.generator;

import static java.lang.String.format;
import static org.molgenis.data.QueryUtils.getAttributePath;

import java.util.Iterator;
import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class QueryClauseRangeGenerator extends BaseQueryClauseGenerator {
  QueryClauseRangeGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.RANGE);
  }

  QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {
    List<Attribute> attributePath = getAttributePath(queryRule.getField(), entityType);
    Attribute attr = attributePath.get(attributePath.size() - 1);
    validateNumericalQueryField(attr);
    String fieldName = getQueryFieldName(attributePath);

    Object queryValue = getQueryValue(attr, queryRule.getValue());
    if (queryValue == null) {
      throw new MolgenisQueryException(QUERY_VALUE_CANNOT_BE_NULL_MSG);
    }
    if (!(queryValue instanceof Iterable<?>)) {
      throw new MolgenisQueryException(
          format(
              "Query value must be a Iterable instead of [%s]",
              queryValue.getClass().getSimpleName()));
    }
    Iterator<?> queryValuesIterator = ((Iterable<?>) queryValue).iterator();
    Object queryValueFrom = getQueryValue(attr, queryValuesIterator.next());
    Object queryValueTo = getQueryValue(attr, queryValuesIterator.next());

    return QueryBuilders.constantScoreQuery(
        nestedQueryBuilder(
            entityType,
            attributePath,
            QueryBuilders.rangeQuery(fieldName).gte(queryValueFrom).lte(queryValueTo)));
  }
}
