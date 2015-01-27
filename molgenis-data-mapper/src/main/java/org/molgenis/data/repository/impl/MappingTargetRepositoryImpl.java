package org.molgenis.data.repository.impl;

import com.google.common.collect.Lists;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.mapping.EntityMapping;
import org.molgenis.data.mapping.MappingProject;
import org.molgenis.data.mapping.MappingTarget;
import org.molgenis.data.meta.MappingProjectMetaData;
import org.molgenis.data.meta.MappingTargetMetaData;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.repository.MappingProjectRepository;
import org.molgenis.data.repository.MappingTargetRepository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

import java.util.List;
import java.util.ArrayList;

public class MappingTargetRepositoryImpl implements MappingTargetRepository
{
	public static final EntityMetaData META_DATA = new MappingTargetMetaData();

	private final CrudRepository repository;
	@Autowired
	private IdGenerator idGenerator;

	private EntityMappingRepository entityMappingRepository;

	public MappingTargetRepositoryImpl(CrudRepository repository, EntityMappingRepository entityMappingRepository)
	{
		this.repository = repository;
		this.entityMappingRepository = entityMappingRepository;
	}

	@Override
	@Transactional
	public void upsert(MappingTarget mappingTarget)
	{
		Entity mappingProjectEntity = toMappingTargetEntity(mappingTarget);
		List<EntityMapping> entityMappings = mappingProject.getEntityMappings();
		List<Entity> entityMappingEntities = entityMappingRepository.upsert(entityMappings);
		mappingProjectEntity.set(MappingProjectMetaData.ENTITYMAPPINGS, entityMappingEntities);
		repository.add(mappingProjectEntity);
	}

	@Override
	@Transactional
	public void update(MappingProject mappingProject)
	{
		MappingProject existing = getMappingProject(mappingProject.getIdentifier());
		if (existing != null)
		{
			Entity mappingProjectEntity = toMappingProjectEntity(mappingProject);
			List<EntityMapping> entityMappings = mappingProject.getEntityMappings();
			List<Entity> entityMappingEntities = entityMappingRepository.upsert(entityMappings);
			mappingProjectEntity.set(MappingProjectMetaData.ENTITYMAPPINGS, entityMappingEntities);
			repository.update(mappingProjectEntity);
		}
		else
		{
			throw new MolgenisDataException("MappingProject does not exists");
		}
	}

	@Override
	public MappingProject getMappingProject(String identifier)
	{
		Entity mappingProjectEntity = repository.findOne(identifier);
		if (mappingProjectEntity == null)
		{
			return null;
		}
		return toMappingProject(mappingProjectEntity);
	}

	@Override
	public List<MappingProject> getAllMappingProjects()
	{
		List<MappingProject> results = new ArrayList<MappingProject>();
		for (Entity entity : repository.findAll(new QueryImpl()))
		{
			results.add(toMappingProject(entity));
		}
		return results;
	}

	@Override
	public List<MappingProject> getMappingProjects(Query q)
	{
		List<MappingProject> results = new ArrayList<>();
		for (Entity entity : repository.findAll(q))
		{
			results.add(toMappingProject(entity));
		}
		return results;
	}

	/**
	 * Creates a fully reconstructed MappingProject from an Entity retrieved from the repository.
	 * 
	 * @param mappingProjectEntity
	 *            Entity with {@link MappingProjectMetaData} metadata
	 * @return fully reconstructed MappingProject
	 */
	private MappingProject toMappingProject(Entity mappingProjectEntity)
	{
		String identifier = mappingProjectEntity.getString(MappingProjectMetaData.IDENTIFIER);
		String name = mappingProjectEntity.getString(MappingProjectMetaData.NAME);
		// FIXME When xref to molgenis user change this to MolgenisUser object
		String owner = molgenisUserService.getUser(SecurityUtils.getCurrentUsername()).getUsername();
		List<Entity> entityMappingEntities = Lists.newArrayList(mappingProjectEntity
				.getEntities(MappingProjectMetaData.ENTITYMAPPINGS));
		List<EntityMapping> entityMappings = entityMappingRepository.toEntityMappings(entityMappingEntities);
		return new MappingProject(identifier, name, owner, entityMappings);
	}

	/**
	 * Creates a new {@link MapEntity} for this MappingProject. Doesn't yet fill the {@link EntityMapping}s.
	 */
	private Entity toMappingTargetEntity(MappingTarget mappingTarget)
	{
		Entity mappingProjectEntity = new MapEntity();
		if (mappingTarget.getIdentifier() == null)
		{
			mappingProjectEntity.set(MappingProjectMetaData.IDENTIFIER, idGenerator.generateId().toString());
		}
		else
		{
			mappingProjectEntity.set(MappingProjectMetaData.IDENTIFIER, mappingTarget.getIdentifier());
		}
		mappingProjectEntity.set(MappingTargetMetaData.ENTITY_NAME);
		return mappingProjectEntity;
	}
}