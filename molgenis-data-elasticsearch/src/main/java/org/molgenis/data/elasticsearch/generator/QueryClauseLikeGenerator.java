package org.molgenis.data.elasticsearch.generator;

import static java.lang.String.format;
import static org.molgenis.data.QueryRule.Operator.LIKE;
import static org.molgenis.data.QueryUtils.getAttributePathExpanded;
import static org.molgenis.data.elasticsearch.FieldConstants.DEFAULT_ANALYZER;

import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

class QueryClauseLikeGenerator extends BaseQueryClauseGenerator {
  QueryClauseLikeGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, LIKE);
  }

  QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {
    List<Attribute> attributePath = getAttributePathExpanded(queryRule.getField(), entityType);
    Attribute attr = attributePath.get(attributePath.size() - 1);
    Object queryValue = getQueryValue(attr, queryRule.getValue());

    String fieldName = getQueryFieldName(attributePath);
    AttributeType attrType = attr.getDataType();
    switch (attrType) {
      case EMAIL:
      case ENUM:
      case HYPERLINK:
      case STRING:
        return nestedQueryBuilder(
            entityType,
            attributePath,
            QueryBuilders.matchPhrasePrefixQuery(fieldName, queryValue)
                .maxExpansions(50)
                .slop(10)
                .analyzer(DEFAULT_ANALYZER));
      case BOOL:
      case COMPOUND:
      case DATE:
      case DATE_TIME:
      case DECIMAL:
      case INT:
      case LONG:
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case FILE:
      case MREF:
      case ONE_TO_MANY:
      case XREF:
        throw new MolgenisQueryException(
            format("Illegal data type [%s] for operator [%s]", attrType, LIKE));
      case HTML: // due to size would result in large amount of ngrams
      case SCRIPT: // due to size would result in large amount of ngrams
      case TEXT: // due to size would result in large amount of ngrams
        throw new UnsupportedOperationException(
            format("Unsupported data type [%s] for operator [%s]", attrType, LIKE));
      default:
        throw new UnexpectedEnumException(attrType);
    }
  }
}
