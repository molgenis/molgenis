package org.molgenis.data.elasticsearch.generator;

import static java.lang.String.format;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.molgenis.data.QueryUtils.getAttributePathExpanded;
import static org.molgenis.data.util.EntityTypeUtils.isReferenceType;

import java.util.List;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

class QueryClauseSearchGenerator extends BaseQueryClauseGenerator {
  QueryClauseSearchGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.SEARCH);
  }

  QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {
    if (queryRule.getValue() == null) {
      throw new MolgenisQueryException(QUERY_VALUE_CANNOT_BE_NULL_MSG);
    }

    if (queryRule.getField() == null) {
      return createQueryClauseSearchAll(queryRule, entityType);
    } else {
      return createQueryClauseSearchAttribute(queryRule, entityType);
    }
  }

  private QueryBuilder createQueryClauseSearchAll(QueryRule queryRule, EntityType entityType) {
    var builder = QueryBuilders.boolQuery().should(multiMatchQuery(queryRule.getValue()));
    var clauses = builder.should();
    for (Attribute child : entityType.getAtomicAttributes()) {
      if (isReferenceType(child.getDataType())) {
        var fieldName = getQueryFieldName(child);
        clauses.add(nestedQuery(fieldName, multiMatchQuery(queryRule.getValue()), ScoreMode.Max));
      }
    }
    return builder;
  }

  private QueryBuilder createQueryClauseSearchAttribute(
      QueryRule queryRule, EntityType entityType) {
    List<Attribute> attributePath = getAttributePathExpanded(queryRule.getField(), entityType);
    Attribute attr = attributePath.get(attributePath.size() - 1);
    Object queryValue = getQueryValue(attr, queryRule.getValue());

    String fieldName = getQueryFieldName(attributePath);
    AttributeType dataType = attr.getDataType();
    switch (dataType) {
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
            entityType, attributePath, QueryBuilders.matchQuery(fieldName, queryValue));
      case BOOL:
        throw new MolgenisQueryException(
            "Cannot execute search query on [" + dataType + "] attribute");
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case XREF:
      case MREF:
      case FILE:
      case ONE_TO_MANY:
      case COMPOUND:
        throw new MolgenisQueryException(
            format(
                "Illegal data type [%s] for operator [%s]", dataType, QueryRule.Operator.SEARCH));
      default:
        throw new UnexpectedEnumException(dataType);
    }
  }
}
