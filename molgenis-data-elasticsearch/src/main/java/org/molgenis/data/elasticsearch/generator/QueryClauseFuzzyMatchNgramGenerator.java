package org.molgenis.data.elasticsearch.generator;

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

class QueryClauseFuzzyMatchNgramGenerator extends BaseQueryClauseGenerator {
  QueryClauseFuzzyMatchNgramGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.FUZZY_MATCH_NGRAM);
  }

  QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {
    String queryField = queryRule.getField();
    Object queryValue = queryRule.getValue();

    QueryBuilder queryBuilder;
    if (queryValue == null) throw new MolgenisQueryException(QUERY_VALUE_CANNOT_BE_NULL_MSG);

    if (queryField == null) {
      queryBuilder = QueryBuilders.matchQuery("_all", queryValue);
    } else {
      Attribute attr = entityType.getAttributeByName(queryField);
      // construct query part
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
          queryField = getQueryFieldName(attr) + ".ngram";
          queryBuilder = QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")");
          break;
        case MREF:
        case XREF:
          queryField =
              getQueryFieldName(attr)
                  + "."
                  + getQueryFieldName(attr.getRefEntity().getLabelAttribute())
                  + ".ngram";
          queryBuilder =
              QueryBuilders.nestedQuery(
                  getQueryFieldName(attr),
                  QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")"),
                  ScoreMode.Max);
          break;
        default:
          throw new UnexpectedEnumException(dataType);
      }
    }
    return queryBuilder;
  }
}
