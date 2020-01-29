package org.molgenis.data.elasticsearch.generator;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.QueryUtils.getAttributePathExpanded;
import static org.molgenis.data.elasticsearch.FieldConstants.FIELD_NOT_ANALYZED;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

class QueryClauseInGenerator extends BaseQueryClauseGenerator {
  QueryClauseInGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.IN);
  }

  QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType) {

    List<Attribute> attributePath = getAttributePathExpanded(queryRule.getField(), entityType);
    Attribute attr = attributePath.get(attributePath.size() - 1);

    String fieldName = getQueryFieldName(attributePath);
    List<Object> queryValues = getQueryValues(attr, queryRule.getValue());
    List<Object> queryValuesWithoutNulls =
        queryValues.stream().filter(Objects::nonNull).collect(toList());

    Optional<QueryBuilder> nonNullQueryBuilder;
    if (!queryValuesWithoutNulls.isEmpty()) {
      nonNullQueryBuilder =
          createNonNullValuesQueryBuilder(
              entityType, attributePath, attr, fieldName, queryValuesWithoutNulls);
    } else {
      nonNullQueryBuilder = Optional.empty();
    }

    Optional<QueryBuilder> nullValueQueryBuilder;
    if (queryValuesWithoutNulls.size() < queryValues.size()) {
      nullValueQueryBuilder = createNullValuesQueryBuilder(entityType, attributePath, fieldName);
    } else {
      nullValueQueryBuilder = Optional.empty();
    }

    QueryBuilder queryBuilder;
    if (nonNullQueryBuilder.isPresent()) {
      if (nullValueQueryBuilder.isPresent()) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
        boolQueryBuilder.should(nonNullQueryBuilder.get());
        boolQueryBuilder.should(nullValueQueryBuilder.get());
        queryBuilder = boolQueryBuilder;
      } else {
        queryBuilder = nonNullQueryBuilder.get();
      }
    } else {
      if (nullValueQueryBuilder.isPresent()) {
        queryBuilder = nullValueQueryBuilder.get();
      } else {
        throw new MolgenisQueryException(QUERY_VALUE_CANNOT_BE_NULL_MSG);
      }
    }

    return QueryBuilders.constantScoreQuery(queryBuilder);
  }

  private Optional<QueryBuilder> createNullValuesQueryBuilder(
      EntityType entityType, List<Attribute> attributePath, String fieldName) {
    Optional<QueryBuilder> nullValueQueryBuilder;
    nullValueQueryBuilder =
        Optional.of(
            QueryBuilders.boolQuery()
                .mustNot(
                    nestedQueryBuilder(
                        entityType, attributePath, QueryBuilders.existsQuery(fieldName))));
    return nullValueQueryBuilder;
  }

  private Optional<QueryBuilder> createNonNullValuesQueryBuilder(
      EntityType entityType,
      List<Attribute> attributePath,
      Attribute attr,
      String fieldName,
      List<Object> queryValuesWithoutNulls) {
    Optional<QueryBuilder> nonNullQueryBuilder;
    AttributeType dataType = attr.getDataType();
    switch (dataType) {
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
        String termsQueryFieldName = fieldName;
        if (useNotAnalyzedField(attr)) {
          termsQueryFieldName += '.' + FIELD_NOT_ANALYZED;
        }

        QueryBuilder termsQueryBuilder =
            QueryBuilders.termsQuery(termsQueryFieldName, queryValuesWithoutNulls.toArray());
        nonNullQueryBuilder =
            Optional.of(nestedQueryBuilder(entityType, attributePath, termsQueryBuilder));
        break;
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case MREF:
      case XREF:
      case FILE:
      case ONE_TO_MANY:
      case COMPOUND:
        throw new MolgenisQueryException(
            format("Illegal data type [%s] for operator [%s]", dataType, Operator.IN));
      default:
        throw new UnexpectedEnumException(dataType);
    }
    return nonNullQueryBuilder;
  }
}
