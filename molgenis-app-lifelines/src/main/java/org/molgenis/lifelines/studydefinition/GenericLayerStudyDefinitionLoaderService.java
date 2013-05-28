package org.molgenis.lifelines.studydefinition;

import java.util.List;

import org.molgenis.lifelines.catalogue.CatalogLoaderService;
import org.molgenis.lifelines.catalogue.UnknownCatalogException;
import org.molgenis.lifelines.hl7.jaxb.QualityMeasureDocument;
import org.molgenis.lifelines.plugins.DataQueryService;
import org.molgenis.lifelines.resourcemanager.ResourceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenericLayerStudyDefinitionLoaderService implements StudyDefinitionLoaderService
{
	private final ResourceManagerService resourceManagerService;
	private final CatalogLoaderService catalogLoaderService;
	private final DataQueryService dataQueryService;

	@Autowired
	public GenericLayerStudyDefinitionLoaderService(ResourceManagerService resourceManagerService,
			CatalogLoaderService catalogLoaderService, DataQueryService dataQueryService)
	{
		this.resourceManagerService = resourceManagerService;
		this.catalogLoaderService = catalogLoaderService;
		this.dataQueryService = dataQueryService;
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
	 */
	@Override
	public void loadStudyDefinition(String id) throws UnknownStudyDefinitionException
	{
		try
		{
			catalogLoaderService.loadCatalog(id);
		}
		catch (UnknownCatalogException e)
		{
			throw new UnknownStudyDefinitionException(e);
		}

		QualityMeasureDocument studyDefinition = resourceManagerService.findStudyDefinition(id);

		dataQueryService.loadStudyDefinitionData(studyDefinition);
	}
}
