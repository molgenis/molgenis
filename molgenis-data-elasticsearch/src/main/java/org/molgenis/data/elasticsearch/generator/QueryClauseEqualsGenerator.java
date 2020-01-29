package org.molgenis.data.elasticsearch.generator;

import static org.molgenis.data.QueryUtils.getAttributePathExpanded;
import static org.molgenis.data.elasticsearch.FieldConstants.FIELD_NOT_ANALYZED;

import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

class QueryClauseEqualsGenerator extends BaseQueryClauseGenerator {
  QueryClauseEqualsGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.EQUALS);
  }

  QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {
    List<Attribute> attributePath = getAttributePathExpanded(queryRule.getField(), entityType);
    Attribute attr = attributePath.get(attributePath.size() - 1);

    QueryBuilder queryBuilder;

    AttributeType attrType = attr.getDataType();
    switch (attrType) {
      case BOOL:
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
        String fieldName = getQueryFieldName(attributePath);
        if (queryRule.getValue() != null) {
          Object queryValue = getQueryValue(attr, queryRule.getValue());
          if (useNotAnalyzedField(attr)) {
            fieldName = fieldName + '.' + FIELD_NOT_ANALYZED;
          }

          queryBuilder =
              nestedQueryBuilder(
                  entityType, attributePath, QueryBuilders.termQuery(fieldName, queryValue));
        } else {
          queryBuilder =
              QueryBuilders.boolQuery()
                  .mustNot(
                      nestedQueryBuilder(
                          entityType, attributePath, QueryBuilders.existsQuery(fieldName)));
        }
        break;
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case XREF:
      case MREF:
      case FILE:
      case ONE_TO_MANY:
      case COMPOUND:
        throw new MolgenisQueryException(new IllegalAttributeTypeException(attrType));
      default:
        throw new UnexpectedEnumException(attrType);
    }

    return QueryBuilders.constantScoreQuery(queryBuilder);
  }
}
