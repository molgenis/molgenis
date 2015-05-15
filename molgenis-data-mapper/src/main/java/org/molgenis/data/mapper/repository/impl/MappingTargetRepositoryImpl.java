package org.molgenis.data.mapper.repository.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.MappingProjectMetaData;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
import org.molgenis.data.mapper.repository.EntityMappingRepository;
import org.molgenis.data.mapper.repository.MappingTargetRepository;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

public class MappingTargetRepositoryImpl implements MappingTargetRepository
{
	public static final EntityMetaData META_DATA = new MappingTargetMetaData();

	@Autowired
	private IdGenerator idGenerator;

	private final EntityMappingRepository entityMappingRepository;

	@Autowired
	private DataService dataService;

	public MappingTargetRepositoryImpl(EntityMappingRepository entityMappingRepository)
	{
		this.entityMappingRepository = entityMappingRepository;
	}

	@Override
	public List<Entity> upsert(Collection<MappingTarget> collection)
	{
		return collection.stream().map(this::upsert).collect(Collectors.toList());
	}

	private Entity upsert(MappingTarget mappingTarget)
	{
		List<Entity> entityMappingEntities = entityMappingRepository.upsert(mappingTarget.getEntityMappings());
		Entity mappingTargetEntity;
		if (mappingTarget.getIdentifier() == null)
		{
			mappingTarget.setIdentifier(idGenerator.generateId());
			mappingTargetEntity = toMappingTargetEntity(mappingTarget, entityMappingEntities);
			dataService.add(META_DATA.getName(), mappingTargetEntity);
		}
		else
		{
			mappingTargetEntity = toMappingTargetEntity(mappingTarget, entityMappingEntities);
			dataService.update(META_DATA.getName(), mappingTargetEntity);
		}
		return mappingTargetEntity;
	}

	/**
	 * Creates a new {@link MapEntity} for this MappingProject. Doesn't yet fill the {@link EntityMapping}s.
	 */
	private Entity toMappingTargetEntity(MappingTarget mappingTarget, List<Entity> entityMappingEntities)
	{
		Entity mappingTargetEntity = new MapEntity(META_DATA);
		mappingTargetEntity.set(MappingProjectMetaData.IDENTIFIER, mappingTarget.getIdentifier());
		mappingTargetEntity.set(MappingTargetMetaData.TARGET, mappingTarget.getTarget().getName());
		mappingTargetEntity.set(MappingTargetMetaData.ENTITYMAPPINGS, entityMappingEntities);
		return mappingTargetEntity;
	}

	@Override
	public List<MappingTarget> toMappingTargets(List<Entity> mappingTargetEntities)
	{
		return mappingTargetEntities.stream().map(this::toMappingTarget).collect(Collectors.toList());
	}

	/**
	 * Creates a fully reconstructed MappingProject from an Entity retrieved from the repository.
	 * 
	 * @param mappingTargetEntity
	 *            Entity with {@link MappingProjectMetaData} metadata
	 * @return fully reconstructed MappingProject
	 */
	private MappingTarget toMappingTarget(Entity mappingTargetEntity)
	{
		List<EntityMapping> entityMappings = Collections.emptyList();
		String identifier = mappingTargetEntity.getString(MappingTargetMetaData.IDENTIFIER);

		if (!dataService.hasRepository(mappingTargetEntity.getString(MappingTargetMetaData.TARGET)))
		{
			return null;
		}

		EntityMetaData target = dataService.getEntityMetaData(mappingTargetEntity
				.getString(MappingTargetMetaData.TARGET));

		if (mappingTargetEntity.getEntities(MappingTargetMetaData.ENTITYMAPPINGS) != null)
		{
			List<Entity> entityMappingEntities = Lists.newArrayList(mappingTargetEntity
					.getEntities(MappingTargetMetaData.ENTITYMAPPINGS));
			entityMappings = entityMappingRepository.toEntityMappings(entityMappingEntities);
		}

		return new MappingTarget(identifier, target, entityMappings);
	}
}