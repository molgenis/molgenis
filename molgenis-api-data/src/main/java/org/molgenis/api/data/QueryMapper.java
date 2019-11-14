package org.molgenis.api.data;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.web.rsql.RSQLValueParser;

public class QueryMapper {
  private final RSQLValueParser rsqlValueParser;

  QueryMapper(RSQLValueParser rsqlValueParser) {
    this.rsqlValueParser = requireNonNull(rsqlValueParser);
  }

  public org.molgenis.data.Query<Entity> map(Query query, Repository<Entity> repository) {
    QueryImpl<Entity> entityQuery = new QueryImpl<>(repository);
    map(query, entityQuery, repository.getEntityType());
    return entityQuery;
  }

  private void map(Query query, QueryImpl<Entity> entityQuery, EntityType entityType) {
    Operator operator = query.getOperator();
    switch (operator) {
      case EQUALS:
        entityQuery.eq(query.getItem(), mapValue(query, entityType));
        break;
      case NOT_EQUALS:
        entityQuery.not().eq(query.getItem(), mapValue(query, entityType));
        break;
      case IN:
        entityQuery.in(query.getItem(), (Iterable<?>) mapValue(query, entityType));
        break;
      case NOT_IN:
        entityQuery.not().in(query.getItem(), (Iterable<?>) mapValue(query, entityType));
        break;
      case MATCHES:
        entityQuery.search(query.getItem(), (String) mapValue(query, entityType));
        break;
      case CONTAINS:
        entityQuery.like(query.getItem(), (String) mapValue(query, entityType));
        break;
      case LESS_THAN:
        entityQuery.lt(query.getItem(), mapValue(query, entityType));
        break;
      case LESS_THAN_OR_EQUAL_TO:
        entityQuery.le(query.getItem(), mapValue(query, entityType));
        break;
      case GREATER_THAN:
        entityQuery.gt(query.getItem(), mapValue(query, entityType));
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        entityQuery.ge(query.getItem(), mapValue(query, entityType));
        break;
      case AND:
        List<Query> subAndQueries = query.getQueryListValue();
        entityQuery.nest();
        for (Iterator<Query> it = subAndQueries.iterator(); it.hasNext(); ) {
          map(it.next(), entityQuery, entityType);
          if (it.hasNext()) {
            entityQuery.and();
          }
        }
        entityQuery.unnest();
        break;
      case OR:
        List<Query> subOrQueries = query.getQueryListValue();
        entityQuery.nest();
        for (Iterator<Query> it = subOrQueries.iterator(); it.hasNext(); ) {
          map(it.next(), entityQuery, entityType);
          if (it.hasNext()) {
            entityQuery.or();
          }
        }
        entityQuery.unnest();
        break;
      default:
        throw new UnexpectedEnumException(operator);
    }
  }

  private Object mapValue(Query query, EntityType entityType) {
    Object mappedValue;

    Operator operator = query.getOperator();
    switch (operator) {
      case EQUALS:
      case NOT_EQUALS:
        mappedValue =
            rsqlValueParser.parse(
                query.getStringValue(), getAttribute(query.getItem(), entityType));
        break;
      case MATCHES:
      case CONTAINS:
        mappedValue = query.getStringValue();
        break;
      case IN:
      case NOT_IN:
        Attribute attribute = getAttribute(query.getItem(), entityType);
        mappedValue =
            query.getStringListValue().stream()
                .map(value -> rsqlValueParser.parse(value, attribute))
                .collect(toList());
        break;
      case LESS_THAN:
      case LESS_THAN_OR_EQUAL_TO:
      case GREATER_THAN:
      case GREATER_THAN_OR_EQUAL_TO:
        Attribute compareAttribute = getAttribute(query.getItem(), entityType);
        switch (compareAttribute.getDataType()) {
          case DATE:
          case DATE_TIME:
          case DECIMAL:
          case INT:
          case LONG:
            break;
          default:
            throw new UnexpectedEnumException(compareAttribute.getDataType());
        }
        mappedValue = rsqlValueParser.parse(query.getStringValue(), compareAttribute);
        break;
      case AND:
      case OR:
      default:
        throw new UnexpectedEnumException(operator);
    }

    return mappedValue;
  }

  private Attribute getAttribute(String item, EntityType entityType) {
    Attribute attribute = entityType.getAttribute(item);
    if (attribute == null) {
      throw new UnknownAttributeException(entityType, item);
    }

    AttributeType attributeType = attribute.getDataType();
    if (attributeType == AttributeType.COMPOUND) {
      throw new UnexpectedEnumException(attributeType);
    }

    return attribute;
  }
}
