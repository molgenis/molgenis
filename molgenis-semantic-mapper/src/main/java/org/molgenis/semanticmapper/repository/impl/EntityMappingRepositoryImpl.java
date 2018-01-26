package org.molgenis.semanticmapper.repository.impl;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.semanticmapper.controller.MappingServiceController;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.meta.EntityMappingMetaData;
import org.molgenis.semanticmapper.repository.AttributeMappingRepository;
import org.molgenis.semanticmapper.repository.EntityMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * O/R mapping between EntityMapping Entity and EntityMapping POJO
 */
public class EntityMappingRepositoryImpl implements EntityMappingRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

	@Autowired
	private DataService dataService;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private EntityMappingMetaData entityMappingMetaData;

	private final AttributeMappingRepository attributeMappingRepository;

	public EntityMappingRepositoryImpl(AttributeMappingRepository attributeMappingRepository)
	{
		this.attributeMappingRepository = attributeMappingRepository;
	}

	@Override
	public List<EntityMapping> toEntityMappings(List<Entity> entityMappingEntities)
	{
		return Lists.transform(entityMappingEntities, this::toEntityMapping);
	}

	private EntityMapping toEntityMapping(Entity entityMappingEntity)
	{
		String identifier = entityMappingEntity.getString(EntityMappingMetaData.IDENTIFIER);

		EntityType targetEntityType;
		try
		{
			targetEntityType = dataService.getEntityType(
					entityMappingEntity.getString(EntityMappingMetaData.TARGET_ENTITY_TYPE));
		}
		catch (UnknownEntityException uee)
		{
			LOG.error(uee.getMessage());
			targetEntityType = null;
		}

		EntityType sourceEntityType;
		try
		{
			sourceEntityType = dataService.getEntityType(
					entityMappingEntity.getString(EntityMappingMetaData.SOURCE_ENTITY_TYPE));
		}
		catch (UnknownEntityException uee)
		{
			LOG.error(uee.getMessage());
			sourceEntityType = null;
		}

		List<Entity> attributeMappingEntities = Lists.newArrayList(
				entityMappingEntity.getEntities(EntityMappingMetaData.ATTRIBUTE_MAPPINGS));
		List<AttributeMapping> attributeMappings = attributeMappingRepository.getAttributeMappings(
				attributeMappingEntities, sourceEntityType, targetEntityType);

		return new EntityMapping(identifier, sourceEntityType, targetEntityType, attributeMappings);
	}

	@Override
	public List<Entity> upsert(Collection<EntityMapping> entityMappings)
	{
		return entityMappings.stream().map(this::upsert).collect(Collectors.toList());
	}

	private Entity upsert(EntityMapping entityMapping)
	{
		List<Entity> attributeMappingEntities = attributeMappingRepository.upsert(entityMapping.getAttributeMappings());
		Entity entityMappingEntity;
		if (entityMapping.getIdentifier() == null)
		{
			entityMapping.setIdentifier(idGenerator.generateId());
			entityMappingEntity = toEntityMappingEntity(entityMapping, attributeMappingEntities);
			dataService.add(entityMappingMetaData.getId(), entityMappingEntity);
		}
		else
		{
			entityMappingEntity = toEntityMappingEntity(entityMapping, attributeMappingEntities);
			dataService.update(entityMappingMetaData.getId(), entityMappingEntity);
		}
		return entityMappingEntity;
	}

	private Entity toEntityMappingEntity(EntityMapping entityMapping, List<Entity> attributeMappingEntities)
	{
		Entity entityMappingEntity = new DynamicEntity(entityMappingMetaData);
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, entityMapping.getIdentifier());
		entityMappingEntity.set(EntityMappingMetaData.SOURCE_ENTITY_TYPE, entityMapping.getName());
		entityMappingEntity.set(EntityMappingMetaData.TARGET_ENTITY_TYPE,
				entityMapping.getTargetEntityType() != null ? entityMapping.getTargetEntityType().getId() : null);
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTE_MAPPINGS, attributeMappingEntities);
		return entityMappingEntity;
	}
}
