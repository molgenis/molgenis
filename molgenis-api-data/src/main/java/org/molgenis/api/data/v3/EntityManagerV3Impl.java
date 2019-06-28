package org.molgenis.api.data.v3;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.data.util.MolgenisDateFormat.parseLocalDate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Service;

@Service
class EntityManagerV3Impl implements EntityManagerV3 {
  private final EntityManager entityManager;

  EntityManagerV3Impl(EntityManager entityManager) {
    this.entityManager = requireNonNull(entityManager);
  }

  @Override
  public Entity create(EntityType entityType) {
    return entityManager.create(entityType, POPULATE);
  }

  @Override
  public void populate(EntityType entityType, Entity entity, Map<String, Object> requestValues) {
    getModifiableAttributes(entityType)
        .forEach(
            attribute -> {
              String attributeName = attribute.getName();
              if (requestValues.containsKey(attributeName)) {
                Object requestValue = requestValues.get(attributeName);
                Object value = convert(attribute, requestValue);
                entity.set(attributeName, value);
              }
            });
  }

  private Stream<Attribute> getModifiableAttributes(EntityType entityType) {
    return stream(entityType.getAtomicAttributes())
        .filter(attribute -> !attribute.hasExpression() && !attribute.isMappedBy());
  }

  private Object convert(Attribute attribute, Object requestValue) {
    Object value;
    AttributeType attrType = attribute.getDataType();
    switch (attrType) {
      case CATEGORICAL:
      case FILE:
      case XREF:
        value = convertRef(attribute, requestValue);
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        value = convertMref(attribute, requestValue);
        break;
      case DATE:
        value = convertDate(requestValue);
        break;
      case DATE_TIME:
        value = convertDateTime(requestValue);
        break;
      case DECIMAL:
        value = convertDecimal(requestValue);
        break;
      case INT:
        value = convertInt(requestValue);
        break;
      case LONG:
        value = convertLong(attribute, requestValue);
        break;
      case BOOL:
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        value = requestValue;
        break;
      case COMPOUND:
        throw new IllegalAttributeTypeException(attrType);
      default:
        throw new UnexpectedEnumException(attrType);
    }
    return value;
  }

  private Entity convertRef(Attribute attribute, Object requestValue) {
    Entity entityValue;
    if (requestValue != null) {
      EntityType refEntityType = attribute.getRefEntity();
      Attribute refIdAttribute = refEntityType.getIdAttribute();

      Object typedRequestValue = convert(refIdAttribute, requestValue);
      entityValue = entityManager.getReference(refEntityType, typedRequestValue);
    } else {
      entityValue = null;
    }
    return entityValue;
  }

  private Iterable<Entity> convertMref(Attribute attribute, Object requestValue) {
    Iterable<Entity> entities;
    if (requestValue != null) {
      if (!(requestValue instanceof Iterable<?>)) {
        throw new RuntimeException("not a list"); // TODO
      }
      Iterable<?> requestValueList = (Iterable<?>) requestValue;

      EntityType refEntityType = attribute.getRefEntity();
      Attribute refIdAttribute = refEntityType.getIdAttribute();

      Iterable<Object> typedIds =
          () ->
              stream(requestValueList)
                  .map(requestValueListItem -> convert(refIdAttribute, requestValueListItem))
                  .collect(toList())
                  .iterator();
      entities = entityManager.getReferences(refEntityType, typedIds);
    } else {
      throw new RuntimeException("value not an empty list");
    }
    return entities;
  }

  private LocalDate convertDate(Object requestValue) {
    LocalDate localDate;
    if (requestValue != null) {
      if (!(requestValue instanceof String)) {
        throw new RuntimeException("not a string"); // TODO
      }
      localDate = parseLocalDate((String) requestValue);
    } else {
      localDate = null;
    }
    return localDate;
  }

  private Instant convertDateTime(Object requestValue) {
    Instant instant;
    if (requestValue != null) {
      if (!(requestValue instanceof String)) {
        throw new RuntimeException("not a string"); // TODO
      }
      instant = parseInstant((String) requestValue);
    } else {
      instant = null;
    }
    return instant;
  }

  private Double convertDecimal(Object requestValue) {
    Double doubleValue;
    if (requestValue != null) {
      if (requestValue instanceof Double) {
        doubleValue = (Double) requestValue;
      } else if (!(requestValue instanceof Number)) {
        doubleValue = ((Number) requestValue).doubleValue();
      } else {
        throw new RuntimeException("not a number"); // TODO
      }
    } else {
      doubleValue = null;
    }
    return doubleValue;
  }

  private Integer convertInt(Object requestValue) {
    Integer integerValue;
    if (requestValue != null) {
      if (requestValue instanceof Double) {
        integerValue = (Integer) requestValue;
      } else if (!(requestValue instanceof Number)) {
        integerValue = ((Number) requestValue).intValue();
      } else {
        throw new RuntimeException("not a number"); // TODO
      }
    } else {
      integerValue = null;
    }
    return integerValue;
  }

  private Long convertLong(Attribute attribute, Object requestValue) {
    Long longValue;
    if (requestValue != null) {
      if (requestValue instanceof Long) {
        longValue = (Long) requestValue;
      } else if (!(requestValue instanceof Number)) {
        longValue = ((Number) requestValue).longValue();
      } else {
        throw new RuntimeException("not a number"); // TODO
      }
    } else {
      longValue = null;
    }
    return longValue;
  }
}
