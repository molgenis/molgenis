package org.molgenis.data.mapping;

import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.repository.AttributeMappingRepository;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.repository.MappingProjectRepository;

public class MappingServiceImpl implements MappingService
{

	private AttributeMappingRepository attributeMappingRepository;
	private EntityMappingRepository entityMappingRepository;
	private MappingProjectRepository mappingProjectRepository;

	public MappingServiceImpl(AttributeMappingRepository attributeMappingRepository,
			EntityMappingRepository entityMappingRepository, MappingProjectRepository mappingProjectRepository)
	{
		this.attributeMappingRepository = attributeMappingRepository;
		this.entityMappingRepository = entityMappingRepository;
		this.mappingProjectRepository = mappingProjectRepository;
	}

	@Override
	public void addEntityMapping(MappingProject mappingProject, EntityMapping entityMapping)
	{
		entityMappingRepository.addEntityMapping();
	}

	public void updateMappingProject(MappingProject mappingProject)
	{

	}

	@Override
	public void addMappingProject(String projectName, MolgenisUser owner)
	{
		List<EntityMapping> entityMappings = null;
		mappingProjectRepository.add(new MappingProject(projectName, owner.getUsername(), entityMappings));
	}

	@Override
	public List<MappingProject> getAllMappingProjects()
	{
		return mappingProjectRepository.getAllMappingProjects();
	}
}
