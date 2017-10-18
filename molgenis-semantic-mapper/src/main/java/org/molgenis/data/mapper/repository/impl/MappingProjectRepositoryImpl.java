package org.molgenis.data.mapper.repository.impl;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.MappingProjectMetaData;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.repository.MappingTargetRepository;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.model.UserEntity;
import org.molgenis.data.security.model.UserFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.security.core.service.UserService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.MAPPING_PROJECT;

public class MappingProjectRepositoryImpl implements MappingProjectRepository
{
	private final DataService dataService;
	private final MappingTargetRepository mappingTargetRepo;
	private final UserService userService;
	private final IdGenerator idGenerator;
	private final MappingProjectMetaData mappingProjectMeta;
	private final UserFactory userFactory;

	public MappingProjectRepositoryImpl(DataService dataService, MappingTargetRepository mappingTargetRepo,
			UserService userService, IdGenerator idGenerator, MappingProjectMetaData mappingProjectMeta,
			UserFactory userFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.mappingTargetRepo = requireNonNull(mappingTargetRepo);
		this.userService = requireNonNull(userService);
		this.idGenerator = requireNonNull(idGenerator);
		this.mappingProjectMeta = requireNonNull(mappingProjectMeta);
		this.userFactory = requireNonNull(userFactory);
	}

	@Override
	@Transactional
	public void add(MappingProject mappingProject)
	{
		if (mappingProject.getIdentifier() != null)
		{
			throw new MolgenisDataException("MappingProject already exists");
		}
		dataService.add(MAPPING_PROJECT, toEntity(mappingProject));
	}

	@Override
	@Transactional
	public void update(MappingProject mappingProject)
	{
		MappingProject existing = getMappingProject(mappingProject.getIdentifier());
		if (existing == null)
		{
			throw new MolgenisDataException("MappingProject does not exist");
		}
		Entity mappingProjectEntity = toEntity(mappingProject);
		dataService.update(MAPPING_PROJECT, mappingProjectEntity);
	}

	@Override
	public MappingProject getMappingProject(String identifier)
	{
		Entity mappingProjectEntity = dataService.findOneById(MAPPING_PROJECT, identifier);
		if (mappingProjectEntity == null)
		{
			return null;
		}
		return toMappingProject(mappingProjectEntity);
	}

	@Override
	public List<MappingProject> getAllMappingProjects()
	{
		List<MappingProject> results = new ArrayList<>();
		dataService.findAll(MAPPING_PROJECT).forEach(entity -> results.add(toMappingProject(entity)));
		return results;
	}

	@Override
	public List<MappingProject> getMappingProjects(Query<Entity> q)
	{
		List<MappingProject> results = new ArrayList<>();
		dataService.findAll(MAPPING_PROJECT, q).forEach(entity -> results.add(toMappingProject(entity)));
		return results;
	}

	/**
	 * Creates a fully reconstructed MappingProject from an Entity retrieved from the repository.
	 *
	 * @param mappingProjectEntity Entity with {@link MappingProjectMetaData} metadata
	 * @return fully reconstructed MappingProject
	 */
	private MappingProject toMappingProject(Entity mappingProjectEntity)
	{
		String identifier = mappingProjectEntity.getString(MappingProjectMetaData.IDENTIFIER);
		String name = mappingProjectEntity.getString(MappingProjectMetaData.NAME);
		String ownerUserName = mappingProjectEntity.getEntity(MappingProjectMetaData.OWNER, UserEntity.class)
												   .getUsername();
		List<Entity> mappingTargetEntities = Lists.newArrayList(
				mappingProjectEntity.getEntities(MappingProjectMetaData.MAPPING_TARGETS));
		List<MappingTarget> mappingTargets = mappingTargetRepo.toMappingTargets(mappingTargetEntities);

		return new MappingProject(identifier, name, userService.findByUsername(ownerUserName), mappingTargets);
	}

	/**
	 * Creates a new Entity for a MappingProject. Upserts the {@link MappingProject}'s {@link MappingTarget}s in the
	 * {@link #mappingTargetRepo}.
	 *
	 * @param mappingProject the {@link MappingProject} used to create an Entity
	 * @return Entity filled with the data from the MappingProject
	 */
	private Entity toEntity(MappingProject mappingProject)
	{
		Entity result = new DynamicEntity(mappingProjectMeta);
		if (mappingProject.getIdentifier() == null)
		{
			mappingProject.setIdentifier(idGenerator.generateId());
		}
		result.set(MappingProjectMetaData.IDENTIFIER, mappingProject.getIdentifier());
		result.set(MappingProjectMetaData.OWNER, userFactory.create().updateFrom(mappingProject.getOwner()));
		result.set(MappingProjectMetaData.NAME, mappingProject.getName());
		List<Entity> mappingTargetEntities = mappingTargetRepo.upsert(mappingProject.getMappingTargets());
		result.set(MappingProjectMetaData.MAPPING_TARGETS, mappingTargetEntities);
		return result;
	}

	@Override
	public void delete(String mappingProjectId)
	{
		dataService.deleteById(MAPPING_PROJECT, mappingProjectId);
	}
}