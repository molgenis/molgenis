package org.molgenis.data.mapping;

import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.repository.MappingProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class MappingServiceImpl implements MappingService
{
	@Autowired
	private MetaDataService metaDataService;
	private MappingProjectRepository mappingProjectRepository;

	public MappingServiceImpl(MappingProjectRepository mappingProjectRepository)
	{
		this.mappingProjectRepository = mappingProjectRepository;
	}

	@Override
	public void addMappingProject(String projectName, MolgenisUser owner, String target)
	{
		MappingProject mappingProject = new MappingProject(projectName, owner.getUsername());
		mappingProject.addTarget(metaDataService.getEntityMetaData(target));
		mappingProjectRepository.add(mappingProject);
	}

	@Override
	public List<MappingProject> getAllMappingProjects()
	{
		return mappingProjectRepository.getAllMappingProjects();
	}

	@Override
	public void updateMappingProject(MappingProject mappingProject)
	{
		mappingProjectRepository.update(mappingProject);
	}

	@Override
	public MappingProject getMappingProject(String identifier)
	{
		return mappingProjectRepository.getMappingProject(identifier);
	}

}
