package org.molgenis.semanticmapper.repository.impl;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.meta.EntityMappingMetadata;
import org.molgenis.semanticmapper.repository.AttributeMappingRepository;
import org.molgenis.semanticmapper.repository.EntityMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;

/** O/R mapping between EntityMapping Entity and EntityMapping POJO */
public class EntityMappingRepositoryImpl implements EntityMappingRepository {
  @Autowired private DataService dataService;

  @Autowired private IdGenerator idGenerator;

  @Autowired private EntityMappingMetadata entityMappingMetaData;

  private final AttributeMappingRepository attributeMappingRepository;

  public EntityMappingRepositoryImpl(AttributeMappingRepository attributeMappingRepository) {
    this.attributeMappingRepository = attributeMappingRepository;
  }

  @Override
  public List<EntityMapping> toEntityMappings(List<Entity> entityMappingEntities) {
    return Lists.transform(entityMappingEntities, this::toEntityMapping);
  }

  private EntityMapping toEntityMapping(Entity entityMappingEntity) {
    String identifier = entityMappingEntity.getString(EntityMappingMetadata.IDENTIFIER);

    EntityType targetEntityType =
        dataService.getEntityType(
            entityMappingEntity.getString(EntityMappingMetadata.TARGET_ENTITY_TYPE));

    EntityType sourceEntityType =
        dataService.getEntityType(
            entityMappingEntity.getString(EntityMappingMetadata.SOURCE_ENTITY_TYPE));

    List<Entity> attributeMappingEntities =
        Lists.newArrayList(
            entityMappingEntity.getEntities(EntityMappingMetadata.ATTRIBUTE_MAPPINGS));
    List<AttributeMapping> attributeMappings =
        attributeMappingRepository.getAttributeMappings(
            attributeMappingEntities, sourceEntityType, targetEntityType);

    return new EntityMapping(identifier, sourceEntityType, targetEntityType, attributeMappings);
  }

  @Override
  public List<Entity> upsert(Collection<EntityMapping> entityMappings) {
    return entityMappings.stream().map(this::upsert).collect(Collectors.toList());
  }

  private Entity upsert(EntityMapping entityMapping) {
    List<Entity> attributeMappingEntities =
        attributeMappingRepository.upsert(entityMapping.getAttributeMappings());
    Entity entityMappingEntity;
    if (entityMapping.getIdentifier() == null) {
      entityMapping.setIdentifier(idGenerator.generateId());
      entityMappingEntity = toEntityMappingEntity(entityMapping, attributeMappingEntities);
      dataService.add(entityMappingMetaData.getId(), entityMappingEntity);
    } else {
      entityMappingEntity = toEntityMappingEntity(entityMapping, attributeMappingEntities);
      dataService.update(entityMappingMetaData.getId(), entityMappingEntity);
    }
    return entityMappingEntity;
  }

  private Entity toEntityMappingEntity(
      EntityMapping entityMapping, List<Entity> attributeMappingEntities) {
    Entity entityMappingEntity = new DynamicEntity(entityMappingMetaData);
    entityMappingEntity.set(EntityMappingMetadata.IDENTIFIER, entityMapping.getIdentifier());
    entityMappingEntity.set(EntityMappingMetadata.SOURCE_ENTITY_TYPE, entityMapping.getName());
    entityMappingEntity.set(
        EntityMappingMetadata.TARGET_ENTITY_TYPE,
        entityMapping.getTargetEntityType() != null
            ? entityMapping.getTargetEntityType().getId()
            : null);
    entityMappingEntity.set(EntityMappingMetadata.ATTRIBUTE_MAPPINGS, attributeMappingEntities);
    return entityMappingEntity;
  }
}
