package org.molgenis.data.elasticsearch.generator;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.Streams;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

abstract class BaseQueryClauseGenerator implements QueryClauseGenerator {
  private final Operator operator;

  static final String ATTRIBUTE_SEPARATOR = ".";

  static final String CANNOT_FILTER_DEEP_REFERENCE_MSG =
      "Can not filter on references deeper than %d.";
  static final String QUERY_VALUE_CANNOT_BE_NULL_MSG = "Query value cannot be null";
  private DocumentIdGenerator documentIdGenerator;

  BaseQueryClauseGenerator(DocumentIdGenerator documentIdGenerator, Operator operator) {
    this.documentIdGenerator = requireNonNull(documentIdGenerator);
    this.operator = requireNonNull(operator);
  }

  @Override
  public Operator getOperator() {
    return operator;
  }

  @Override
  public QueryBuilder createQueryClause(QueryRule queryRule, EntityType entityType) {
    if (operator != queryRule.getOperator()) {
      throw new IllegalArgumentException(
          format(
              "Illegal query operator '%s' does not equal '%s'",
              queryRule.getOperator(), operator));
    }
    return mapQueryRule(queryRule, entityType);
  }

  abstract QueryBuilder mapQueryRule(QueryRule queryRule, EntityType entityType);

  String getQueryFieldName(List<Attribute> attributePath) {
    return attributePath.stream()
        .map(this::getQueryFieldName)
        .collect(joining(ATTRIBUTE_SEPARATOR));
  }

  String getQueryFieldName(Attribute attribute) {
    return documentIdGenerator.generateId(attribute);
  }

  boolean useNotAnalyzedField(Attribute attr) {
    AttributeType attrType = attr.getDataType();
    switch (attrType) {
      case BOOL:
      case DATE:
      case DATE_TIME:
      case DECIMAL:
      case INT:
      case LONG:
        return false;
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        return true;
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case XREF:
      case MREF:
      case FILE:
      case ONE_TO_MANY:
        return useNotAnalyzedField(attr.getRefEntity().getIdAttribute());
      case COMPOUND:
        throw new MolgenisQueryException(new IllegalAttributeTypeException(attrType));
      default:
        throw new UnexpectedEnumException(attrType);
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  List<Object> getQueryValues(Attribute attr, Object queryRuleValue) {
    if (queryRuleValue == null) {
      throw new MolgenisQueryException(QUERY_VALUE_CANNOT_BE_NULL_MSG);
    }
    if (!(queryRuleValue instanceof Iterable<?>)) {
      throw new MolgenisQueryException(
          "Query value must be a Iterable instead of ["
              + queryRuleValue.getClass().getSimpleName()
              + "]");
    }
    return Streams.stream((Iterable<?>) queryRuleValue)
        .map(aQueryRuleValue -> getQueryValue(attr, aQueryRuleValue))
        .collect(toList());
  }

  Object getQueryValue(Attribute attribute, Object queryRuleValue) {
    AttributeType attrType = attribute.getDataType();
    switch (attrType) {
      case BOOL:
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
        return queryRuleValue;
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case FILE:
      case MREF:
      case ONE_TO_MANY:
      case XREF:
        return queryRuleValue instanceof Entity
            ? ((Entity) queryRuleValue).getIdValue()
            : queryRuleValue;
      case DATE:
        if (queryRuleValue instanceof LocalDate) {
          return queryRuleValue.toString();
        } else if (queryRuleValue instanceof String) {
          return queryRuleValue;
        } else {
          throw new MolgenisQueryException(
              format(
                  "Query value must be of type LocalDate instead of [%s]",
                  queryRuleValue.getClass().getSimpleName()));
        }
      case DATE_TIME:
        if (queryRuleValue instanceof Instant) {
          return queryRuleValue.toString();
        } else if (queryRuleValue instanceof String) {
          return queryRuleValue;
        } else {
          throw new MolgenisQueryException(
              format(
                  "Query value must be of type Instant instead of [%s]",
                  queryRuleValue.getClass().getSimpleName()));
        }
      case COMPOUND:
        throw new MolgenisQueryException(new IllegalAttributeTypeException(attrType));
      default:
        throw new UnexpectedEnumException(attrType);
    }
  }

  /**
   * Wraps the query in a nested query when a query is done on a reference entity. Returns the
   * original query when it is applied to the current entity.
   *
   * <p>Package-private for testability
   */
  QueryBuilder nestedQueryBuilder(
      EntityType entityType, List<Attribute> attributePath, QueryBuilder queryBuilder) {
    validateIndexingDepth(entityType, attributePath);

    QueryBuilder nestedQueryBuilder = queryBuilder;
    if (attributePath.size() > 1) {
      List<String> fieldNamePath =
          attributePath.stream().map(this::getQueryFieldName).collect(toList());

      for (int i = attributePath.size() - 1; i > 0; --i) {
        String path = String.join(".", fieldNamePath.subList(0, i));
        nestedQueryBuilder = QueryBuilders.nestedQuery(path, nestedQueryBuilder, ScoreMode.Avg);
      }
    }

    return nestedQueryBuilder;
  }

  private void validateIndexingDepth(EntityType entityType, List<Attribute> attributePath) {
    int requiredIndexingDepth = attributePath.size() - 1;
    if (requiredIndexingDepth > 1) {
      Attribute lastAttribute = attributePath.get(attributePath.size() - 1);
      if (lastAttribute.isIdAttribute()) {
        requiredIndexingDepth -= 1;
      }
    }

    if (requiredIndexingDepth > entityType.getIndexingDepth()) {
      throw new UnsupportedOperationException(
          format(CANNOT_FILTER_DEEP_REFERENCE_MSG, entityType.getIndexingDepth()));
    }
  }

  void validateNumericalQueryField(Attribute attr) {
    AttributeType dataType = attr.getDataType();

    switch (dataType) {
      case DATE:
      case DATE_TIME:
      case DECIMAL:
      case INT:
      case LONG:
        break;
      case BOOL:
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case COMPOUND:
      case EMAIL:
      case ENUM:
      case FILE:
      case HTML:
      case HYPERLINK:
      case MREF:
      case ONE_TO_MANY:
      case SCRIPT:
      case STRING:
      case TEXT:
      case XREF:
        throw new MolgenisQueryException("Range query not allowed for type [" + dataType + "]");
      default:
        throw new UnexpectedEnumException(dataType);
    }
  }
}
