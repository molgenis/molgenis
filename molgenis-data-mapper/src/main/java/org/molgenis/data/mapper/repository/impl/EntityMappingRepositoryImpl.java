package org.molgenis.data.mapper.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.mapper.controller.MappingServiceController;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.meta.EntityMappingMetaData;
import org.molgenis.data.mapper.repository.AttributeMappingRepository;
import org.molgenis.data.mapper.repository.EntityMappingRepository;
import org.molgenis.data.support.MapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

/**
 * O/R mapping between EntityMapping Entity and EntityMapping POJO
 */
public class EntityMappingRepositoryImpl implements EntityMappingRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

	public static final EntityMetaData META_DATA = new EntityMappingMetaData();

	@Autowired
	private DataService dataService;

	@Autowired
	private IdGenerator idGenerator;

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

		EntityMetaData targetEntityMetaData;
		try
		{
			targetEntityMetaData = dataService.getEntityMetaData(entityMappingEntity
					.getString(EntityMappingMetaData.TARGETENTITYMETADATA));
		}
		catch (UnknownEntityException uee)
		{
			LOG.error(uee.getMessage());
			targetEntityMetaData = null;
		}

		EntityMetaData sourceEntityMetaData;
		try
		{
			sourceEntityMetaData = dataService.getEntityMetaData(entityMappingEntity
					.getString(EntityMappingMetaData.SOURCEENTITYMETADATA));
		}
		catch (UnknownEntityException uee)
		{
			LOG.error(uee.getMessage());
			sourceEntityMetaData = null;
		}

		List<Entity> attributeMappingEntities = Lists.<Entity> newArrayList(entityMappingEntity
				.getEntities(EntityMappingMetaData.ATTRIBUTEMAPPINGS));
		List<AttributeMapping> attributeMappings = attributeMappingRepository.getAttributeMappings(
				attributeMappingEntities, sourceEntityMetaData, targetEntityMetaData);

		return new EntityMapping(identifier, sourceEntityMetaData, targetEntityMetaData, attributeMappings);
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
			dataService.add(EntityMappingRepositoryImpl.META_DATA.getName(), entityMappingEntity);
		}
		else
		{
			entityMappingEntity = toEntityMappingEntity(entityMapping, attributeMappingEntities);
			dataService.update(EntityMappingRepositoryImpl.META_DATA.getName(), entityMappingEntity);
		}
		return entityMappingEntity;
	}

	private Entity toEntityMappingEntity(EntityMapping entityMapping, List<Entity> attributeMappingEntities)
	{
		Entity entityMappingEntity = new MapEntity(META_DATA);
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, entityMapping.getIdentifier());
		entityMappingEntity.set(EntityMappingMetaData.SOURCEENTITYMETADATA, entityMapping.getName());
		entityMappingEntity
				.set(EntityMappingMetaData.TARGETENTITYMETADATA,
						entityMapping.getTargetEntityMetaData() != null ? entityMapping.getTargetEntityMetaData()
								.getName() : null);
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTEMAPPINGS, attributeMappingEntities);
		return entityMappingEntity;
	}
}
