package org.molgenis.data.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapping.model.EntityMapping;
import org.molgenis.data.mapping.model.MappingTarget;
import org.molgenis.data.meta.MappingProjectMetaData;
import org.molgenis.data.meta.MappingTargetMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.repository.MappingTargetRepository;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.IdGenerator;

import com.google.common.collect.Lists;

public class MappingTargetRepositoryImpl implements MappingTargetRepository
{
	public static final EntityMetaData META_DATA = new MappingTargetMetaData();

	private final CrudRepository repository;
	@Autowired
	private IdGenerator idGenerator;

	private EntityMappingRepository entityMappingRepository;

	@Autowired
	private MetaDataService metaDataService;

	public MappingTargetRepositoryImpl(CrudRepository repository, EntityMappingRepository entityMappingRepository)
	{
		this.repository = repository;
		this.entityMappingRepository = entityMappingRepository;
	}

	@Override
	public List<Entity> upsert(Collection<MappingTarget> collection)
	{
		return collection.stream().map(this::upsert).collect(Collectors.toList());
	}

	private Entity upsert(MappingTarget mappingTarget)
	{
		Entity mappingTargetEntity = toMappingTargetEntity(mappingTarget);
		Map<String, EntityMapping> entityMappings = mappingTarget.getEntityMappings();
		List<Entity> entityMappingEntities = entityMappingRepository.upsert(entityMappings.values());
		mappingTargetEntity.set(MappingTargetMetaData.ENTITYMAPPINGS, entityMappingEntities);
		repository.add(mappingTargetEntity);
		return mappingTargetEntity;
	}

	/**
	 * Creates a new {@link MapEntity} for this MappingProject. Doesn't yet fill the {@link EntityMapping}s.
	 */
	private Entity toMappingTargetEntity(MappingTarget mappingTarget)
	{
		Entity mappingTargetEntity = new MapEntity(MappingProjectRepositoryImpl.META_DATA);
		if (mappingTarget.getIdentifier() == null)
		{
			mappingTargetEntity.set(MappingProjectMetaData.IDENTIFIER, idGenerator.generateId().toString());
		}
		else
		{
			mappingTargetEntity.set(MappingProjectMetaData.IDENTIFIER, mappingTarget.getIdentifier());
		}
		mappingTargetEntity.set(MappingTargetMetaData.ENTITY_NAME, mappingTarget.getTarget().getName());
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
	 * @param mappingProjectEntity
	 *            Entity with {@link MappingProjectMetaData} metadata
	 * @return fully reconstructed MappingProject
	 */
	private MappingTarget toMappingTarget(Entity mappingProjectEntity)
	{
		String identifier = mappingProjectEntity.getString(MappingTargetMetaData.IDENTIFIER);
		EntityMetaData target = metaDataService.getEntityMetaData(mappingProjectEntity
				.getString(MappingTargetMetaData.TARGET));
		List<Entity> entityMappingEntities = Lists.newArrayList(mappingProjectEntity
				.getEntities(MappingTargetMetaData.ENTITYMAPPINGS));
		List<EntityMapping> entityMappings = entityMappingRepository.toEntityMappings(entityMappingEntities);
		return new MappingTarget(identifier, target, entityMappings);
	}
}