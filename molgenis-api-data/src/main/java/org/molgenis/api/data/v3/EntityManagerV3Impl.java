package org.molgenis.api.data.v3;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;

import java.util.Map;
import java.util.stream.Stream;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
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

  private Object convert(Attribute attribute, Object value) {
    // TODO implement (something along the lines of RestService.toEntity)
    return value;
  }
}
