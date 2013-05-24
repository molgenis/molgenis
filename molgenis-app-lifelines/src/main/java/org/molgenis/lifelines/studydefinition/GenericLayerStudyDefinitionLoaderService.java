package org.molgenis.lifelines.studydefinition;

import java.util.List;

import org.molgenis.lifelines.resourcemanager.ResourceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenericLayerStudyDefinitionLoaderService implements StudyDefinitionLoaderService
{
	private final ResourceManagerService resourceManagerService;

	@Autowired
	public GenericLayerStudyDefinitionLoaderService(ResourceManagerService resourceManagerService)
	{
		this.resourceManagerService = resourceManagerService;
	}

	/**
	 * Find all studydefinitions
	 * 
	 * @return List of StudyDefinitionInfo
	 */
	@Override
	public List<StudyDefinitionInfo> findStudyDefinitions()
	{
		return resourceManagerService.findStudyDefinitions();
	}

	/**
	 * Get a specific studydefinition and save it in the database
	 * 
	 */
	@Override
	public void loadStudyDefinition(String id) throws UnknownStudyDefinitionException
	{
		// TODO implement by calling Query Service
		System.out.println("Load studydefinition " + id);
	}

}
