package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.roc.MatchQualityRocService;
import org.molgenis.ontology.sorta.MatchInputTermBatchService;
import org.molgenis.ontology.sorta.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.sorta.MatchingTaskEntityMetaData;
import org.molgenis.ontology.sorta.SortaService;
import org.molgenis.ontology.sorta.SortaServiceImpl;
import org.molgenis.ontology.sorta.UploadProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OntologyConfiguration
{
	@Autowired
	private DataService dataService;

	// Declaring these EntityMetaData beans makes sure their repositories are created in the default backend.
	@Bean
	public OntologyMetaData ontologyMetaData()
	{
		return OntologyMetaData.INSTANCE;
	}

	@Bean
	public OntologyTermSynonymMetaData ontologyTermSynonymMetaData()
	{
		return OntologyTermSynonymMetaData.INSTANCE;
	}

	@Bean
	public OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMetaData()
	{
		return OntologyTermDynamicAnnotationMetaData.INSTANCE;
	}

	@Bean
	public OntologyTermNodePathMetaData ontologyTermNodePathMetaData()
	{
		return OntologyTermNodePathMetaData.INSTANCE;
	}

	@Bean
	public OntologyTermMetaData ontologyTermMetaData()
	{
		return OntologyTermMetaData.INSTANCE;
	}

	@Bean
	public MatchingTaskEntityMetaData matchingTaskEntityMetaData()
	{
		return MatchingTaskEntityMetaData.INSTANCE;
	}

	@Bean
	public MatchingTaskContentEntityMetaData matchingTaskContentEntityMetaData()
	{
		return MatchingTaskContentEntityMetaData.INSTANCE;
	}

	@Bean
	public SortaService sortaService()
	{
		return new SortaServiceImpl(dataService, informationContentService());
	}

	@Bean
	public MatchInputTermBatchService processInputTermService()
	{
		return new MatchInputTermBatchService(dataService, uploadProgress(), sortaService());
	}

	@Bean
	public UploadProgress uploadProgress()
	{
		return new UploadProgress();
	}

	@Bean
	public MatchQualityRocService matchQualityRocService()
	{
		return new MatchQualityRocService(dataService, sortaService());
	}

	@Bean
	public InformationContentService informationContentService()
	{
		return new InformationContentService(dataService);
	}

	public OntologyConfiguration()
	{
		System.setProperty("jdk.xml.entityExpansionLimit", "1280000");
	}
}
