package org.molgenis.data.elasticsearch.generator;

import static java.lang.String.format;
import static org.molgenis.data.QueryUtils.getAttributePathExpanded;

import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

class QueryClauseFuzzyMatchGenerator extends BaseQueryClauseGenerator {
  QueryClauseFuzzyMatchGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.FUZZY_MATCH);
  }

  QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {
    List<Attribute> attributePath =
        getAttributePathExpanded(queryRule.getField(), entityType, true);
    Attribute attr = attributePath.get(attributePath.size() - 1);
    Object queryValue = getQueryValue(attr, queryRule.getValue());

    String fieldName = getQueryFieldName(attributePath);
    AttributeType attrType = attr.getDataType();

    switch (attrType) {
      case DATE:
      case DATE_TIME:
      case DECIMAL:
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case INT:
      case LONG:
      case SCRIPT:
      case STRING:
      case TEXT:
        return nestedQueryBuilder(
            entityType,
            attributePath,
            QueryBuilders.queryStringQuery(fieldName + ":(" + queryValue + ")"));
      case BOOL:
      case COMPOUND:
        throw new MolgenisQueryException(
            format(
                "Illegal data type [%s] for operator [%s]",
                attrType, QueryRule.Operator.FUZZY_MATCH));
      default:
        throw new UnexpectedEnumException(attrType);
    }
  }
}
