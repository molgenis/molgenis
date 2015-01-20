package org.molgenis.data.repository.impl;

import com.google.common.collect.Lists;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.mapping.MappingProject;
import org.molgenis.data.meta.MappingProjectMetaData;
import org.molgenis.data.repository.MappingProjectRepository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

@Component
public class MappingProjectRepositoryImpl implements MappingProjectRepository
{
	private final CrudRepository repository;
	private MolgenisUserService molgenisUserService;

	@Autowired
	public MappingProjectRepositoryImpl(ManageableCrudRepositoryCollection collection,
			MolgenisUserService molgenisUserService, MappingProjectMetaData mappingProjectMetaData)
	{
		this.repository = collection.add(mappingProjectMetaData);
		this.molgenisUserService = molgenisUserService;
	}

	@Override
	public void add(MappingProject mappingProject)
	{
		MappingProject existing = getMappingProject(mappingProject.getIdentifier());
		if (existing == null)
		{
			repository.add(toProjectMappingEntity(mappingProject));
		}
		else
		{
			throw new MolgenisDataException("MappingProject already exists");
		}
	}

	@Override
	public void update(MappingProject mappingProject)
	{
		MappingProject existing = getMappingProject(mappingProject.getIdentifier());
		if (existing != null)
		{
			repository.update(toProjectMappingEntity(mappingProject));
		}
		else
		{
			throw new MolgenisDataException("MappingProject does not exists");
		}
	}

	private Entity toProjectMappingEntity(MappingProject mappingProject)
	{
		Entity entityMappingEntity = new MapEntity();
		entityMappingEntity.set(MappingProjectMetaData.IDENTIFIER, mappingProject.getIdentifier());
		entityMappingEntity.set(MappingProjectMetaData.ENTITYMAPPINGS, mappingProject.getEntityMappings());
		entityMappingEntity.set(MappingProjectMetaData.OWNER, mappingProject.getOwner());
		return entityMappingEntity;
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

	@Override
	public MappingProject toMappingProject(Entity entityMappingEntity)
	{
		String identifier = entityMappingEntity.getString(MappingProjectMetaData.IDENTIFIER);
		List entityMappings = Lists
				.newArrayList(entityMappingEntity.getEntities(MappingProjectMetaData.ENTITYMAPPINGS));
		MolgenisUser owner = molgenisUserService.getUser(entityMappingEntity.getEntity(MappingProjectMetaData.OWNER)
				.getString(MolgenisUser.USERNAME));
		return new MappingProject(identifier, owner, entityMappings);
	}
}