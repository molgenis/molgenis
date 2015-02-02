package org.molgenis.data.mapping;

import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.algorithm.AlgorithmService;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.repository.MappingProjectRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;

public class MappingServiceImpl implements MappingService
{
	@Autowired
	private DataService dataService;
	
	@Autowired
	private AlgorithmService algorithmService;  

	@Autowired
	private ManageableCrudRepositoryCollection manageableCrudRepositoryCollection;

	private MappingProjectRepository mappingProjectRepository;

	public MappingServiceImpl(MappingProjectRepository mappingProjectRepository)
	{
		this.mappingProjectRepository = mappingProjectRepository;
	}

	@Override
	public MappingProject addMappingProject(String projectName, MolgenisUser owner, String target)
	{
		MappingProject mappingProject = new MappingProject(projectName, owner);
		mappingProject.addTarget(dataService.getEntityMetaData(target));
		mappingProjectRepository.add(mappingProject);
		return mappingProject;
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

	@Override
	public void run(String mappingProjectId, String target, String newEntityName)
	{
		MappingProject mappingProject = getMappingProject(mappingProjectId);
		DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(newEntityName, mappingProject.getMappingTarget(target).getTarget());
		newEntityMetaData.addAttribute("source");
		CrudRepository newRepo = manageableCrudRepositoryCollection.add(newEntityMetaData);
		
		//algorithmService.
		//add stuff based on algorithms in the attribute mapping for this mappingProject
		//newEntity.add(entity);
	}

}
