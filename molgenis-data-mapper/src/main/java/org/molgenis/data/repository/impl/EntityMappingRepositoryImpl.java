package org.molgenis.data.repository.impl;

import java.util.List;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.mapping.EntityMapping;
import org.molgenis.data.meta.EntityMappingMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;

import bsh.StringUtil;

import com.google.common.collect.Lists;

public class EntityMappingRepositoryImpl implements EntityMappingRepository
{
	@Autowired
	private MetaDataService metaDataService;

	public static final EntityMetaData META_DATA = new EntityMappingMetaData();
	private final CrudRepository repository;

	public EntityMappingRepositoryImpl(CrudRepository repository)
	{
		this.repository = repository;
	}

	@Override
	public void add(List<EntityMapping> entityMappings)
	{
		for (EntityMapping entityMapping : entityMappings)
		{
			add(entityMapping);
		}
	}

	@Override
	public void add(EntityMapping entityMapping)
	{
		EntityMapping existing = getEntityMapping(entityMapping.getIdentifier());
		if (existing == null)
		{
			repository.add(toEntityMappingEntity(entityMapping));
		}
		else
		{
			throw new MolgenisDataException("EntityMapping already exists");
		}
	}

	public EntityMapping getEntityMapping(String identifier)
	{
		Entity entityMappingEntity = repository.findOne(identifier);
		if (entityMappingEntity == null)
		{
			return null;
		}
		return toEntityMappingEntity(entityMappingEntity);
	}

	@Override
	public EntityMapping toEntityMappingEntity(Entity entityMappingEntity)
	{
		String identifier = entityMappingEntity.getString(EntityMappingMetaData.IDENTIFIER);
		EntityMetaData targetEntityMetaData = metaDataService.getEntityMetaData(entityMappingEntity
				.getString(EntityMappingMetaData.TARGETENTITYMETADATA));
		EntityMetaData sourceEntityMetaData = metaDataService.getEntityMetaData(entityMappingEntity
				.getString(EntityMappingMetaData.SOURCEENTITYMETADATA));
		List attributeMappings = Lists.newArrayList(entityMappingEntity
				.getEntities(EntityMappingMetaData.ATTRIBUTEMAPPINGS));

		return new EntityMapping(identifier, sourceEntityMetaData, targetEntityMetaData, attributeMappings);
	}

	private Entity toEntityMappingEntity(EntityMapping entityMapping)
	{

		Entity entityMappingEntity = new MapEntity();
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, entityMapping.getIdentifier());

		entityMappingEntity
				.set(EntityMappingMetaData.SOURCEENTITYMETADATA,
						entityMapping.getSourceEntityMetaData() != null ? entityMapping.getSourceEntityMetaData()
								.getName() : null);
		entityMappingEntity
				.set(EntityMappingMetaData.TARGETENTITYMETADATA,
						entityMapping.getTargetEntityMetaData() != null ? entityMapping.getTargetEntityMetaData()
								.getName() : null);
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTEMAPPINGS, entityMapping.getAttributeMappings());
		return entityMappingEntity;
	}

}
