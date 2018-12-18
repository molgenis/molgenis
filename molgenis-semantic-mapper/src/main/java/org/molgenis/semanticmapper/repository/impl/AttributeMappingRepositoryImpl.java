package org.molgenis.semanticmapper.repository.impl;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetaData.ALGORITHM;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetaData.ALGORITHM_STATE;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetaData.IDENTIFIER;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetaData.SOURCE_ATTRIBUTES;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetaData.TARGET_ATTRIBUTE;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.meta.AttributeMappingMetaData;
import org.molgenis.semanticmapper.repository.AttributeMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class AttributeMappingRepositoryImpl implements AttributeMappingRepository {
  private final AttributeMappingMetaData attributeMappingMetaData;

  @Autowired private IdGenerator idGenerator;

  private final DataService dataService;

  public AttributeMappingRepositoryImpl(
      DataService dataService, AttributeMappingMetaData attributeMappingMetaData) {
    this.dataService = requireNonNull(dataService);
    this.attributeMappingMetaData = requireNonNull(attributeMappingMetaData);
  }

  @Override
  public List<Entity> upsert(Collection<AttributeMapping> attributeMappings) {
    List<Entity> result = Lists.newArrayList();
    for (AttributeMapping attributeMapping : attributeMappings) {
      result.add(upsert(attributeMapping));
    }
    return result;
  }

  private Entity upsert(AttributeMapping attributeMapping) {
    Entity result;
    if (attributeMapping.getIdentifier() == null) {
      attributeMapping.setIdentifier(idGenerator.generateId());
      result = toAttributeMappingEntity(attributeMapping);
      dataService.add(attributeMappingMetaData.getId(), result);
    } else {
      result = toAttributeMappingEntity(attributeMapping);
      dataService.update(attributeMappingMetaData.getId(), result);
    }
    return result;
  }

  @Override
  public List<AttributeMapping> getAttributeMappings(
      List<Entity> attributeMappingEntities,
      @Nullable EntityType sourceEntityType,
      @Nullable EntityType targetEntityType) {
    return Lists.transform(
        attributeMappingEntities,
        attributeMappingEntity ->
            toAttributeMapping(attributeMappingEntity, sourceEntityType, targetEntityType));
  }

  /**
   * Returns attributes for the source attribute names in the given entity. Ignores attribute names
   * for which no attribute exists in the source entity type due to (see
   * https://github.com/molgenis/molgenis/issues/8051).
   *
   * <p>package-private for testability
   */
  List<Attribute> getAlgorithmSourceAttributes(
      Entity attributeMappingEntity, EntityType sourceEntityType) {
    List<Attribute> attributes;

    String sourceAttributesString = attributeMappingEntity.getString(SOURCE_ATTRIBUTES);
    if (sourceAttributesString != null) {
      attributes = new ArrayList<>();
      for (String sourceAttributeStr : sourceAttributesString.split(",")) {
        Attribute sourceAttribute = sourceEntityType.getAttribute(sourceAttributeStr);
        if (sourceAttribute != null) {
          attributes.add(sourceAttribute);
        }
      }
    } else {
      attributes = emptyList();
    }
    return attributes;
  }

  private AttributeMapping toAttributeMapping(
      Entity attributeMappingEntity,
      @Nullable EntityType sourceEntityType,
      @Nullable EntityType targetEntityType) {
    String identifier = attributeMappingEntity.getString(IDENTIFIER);
    String targetAttributeName = attributeMappingEntity.getString(TARGET_ATTRIBUTE);
    Attribute targetAttribute =
        targetEntityType != null ? targetEntityType.getAttribute(targetAttributeName) : null;
    String algorithm = attributeMappingEntity.getString(ALGORITHM);
    String algorithmState = attributeMappingEntity.getString(ALGORITHM_STATE);
    List<Attribute> sourceAttributes =
        sourceEntityType != null
            ? getAlgorithmSourceAttributes(attributeMappingEntity, sourceEntityType)
            : emptyList();

    return new AttributeMapping(
        identifier,
        targetAttributeName,
        targetAttribute,
        algorithm,
        sourceAttributes,
        algorithmState);
  }

  private Entity toAttributeMappingEntity(AttributeMapping attributeMapping) {
    Entity attributeMappingEntity = new DynamicEntity(attributeMappingMetaData);
    attributeMappingEntity.set(IDENTIFIER, attributeMapping.getIdentifier());
    attributeMappingEntity.set(TARGET_ATTRIBUTE, attributeMapping.getTargetAttributeName());
    attributeMappingEntity.set(ALGORITHM, attributeMapping.getAlgorithm());
    attributeMappingEntity.set(
        SOURCE_ATTRIBUTES,
        attributeMapping
            .getSourceAttributes()
            .stream()
            .map(Attribute::getName)
            .collect(Collectors.joining(",")));
    attributeMappingEntity.set(ALGORITHM_STATE, attributeMapping.getAlgorithmState().toString());
    return attributeMappingEntity;
  }
}
