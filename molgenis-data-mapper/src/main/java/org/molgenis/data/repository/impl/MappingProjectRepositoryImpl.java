package org.molgenis.data.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.data.mapping.model.MappingTarget;
import org.molgenis.data.meta.MappingProjectMetaData;
import org.molgenis.data.repository.MappingProjectRepository;
import org.molgenis.data.repository.MappingTargetRepository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

import com.google.common.collect.Lists;

public class MappingProjectRepositoryImpl implements MappingProjectRepository
{
	public static final MappingProjectMetaData META_DATA = new MappingProjectMetaData();
	private final CrudRepository repository;
	@Autowired
	private MolgenisUserService molgenisUserService;
	@Autowired
	private IdGenerator idGenerator;
	private MappingTargetRepository mappingTargetRepository;

	public MappingProjectRepositoryImpl(CrudRepository repository, MappingTargetRepository mappingTargetRepository)
	{
		this.repository = repository;
		this.mappingTargetRepository = mappingTargetRepository;
	}

	@Override
	@Transactional
	public void add(MappingProject mappingProject)
	{
		if (mappingProject.getIdentifier() != null)
		{
			throw new MolgenisDataException("MappingProject already exists");
		}
		repository.add(toEntity(mappingProject));
	}

	@Override
	@Transactional
	public void update(MappingProject mappingProject)
	{
		MappingProject existing = getMappingProject(mappingProject.getIdentifier());
		if (existing == null)
		{
			throw new MolgenisDataException("MappingProject does not exists");
		}
		Entity mappingProjectEntity = toEntity(mappingProject);
		repository.update(mappingProjectEntity);
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
		MolgenisUser owner = molgenisUserService.getUser(mappingProjectEntity.getString(MappingProjectMetaData.OWNER));
		List<Entity> mappingTargetEntities = Lists.newArrayList(mappingProjectEntity
				.getEntities(MappingProjectMetaData.MAPPINGTARGETS));
		List<MappingTarget> mappingTargets = mappingTargetRepository.toMappingTargets(mappingTargetEntities);
		return new MappingProject(identifier, name, owner, mappingTargets);
	}

	/**
	 * Creates a new Entity for a MappingProject. Upserts the {@link MappingProject}'s {@link MappingTarget}s in the
	 * {@link #mappingTargetRepository}.
	 * 
	 * @param mappingProject
	 *            the {@link MappingProject} used to create an Entity
	 * @return Entity filled with the data from the MappingProject
	 */
	private Entity toEntity(MappingProject mappingProject)
	{
		Entity result = new MapEntity(META_DATA);
		if (mappingProject.getIdentifier() == null)
		{
			mappingProject.setIdentifier(idGenerator.generateId().toString());
		}
		result.set(MappingProjectMetaData.IDENTIFIER, mappingProject.getIdentifier());
		// FIXME: Once cross-repo references allow it, change this to mappingProject.getOwner()
		result.set(MappingProjectMetaData.OWNER, mappingProject.getOwner().getUsername());
		result.set(MappingProjectMetaData.NAME, mappingProject.getName());
		List<Entity> mappingTargetEntities = mappingTargetRepository.upsert(mappingProject.getMappingTargets());
		result.set(MappingProjectMetaData.MAPPINGTARGETS, mappingTargetEntities);
		return result;
	}

	@Override
	public void delete(String mappingProjectId)
	{
		repository.deleteById(mappingProjectId);
	}
}