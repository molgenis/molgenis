package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.ic.OntologyTermFrequencyServiceImpl;
import org.molgenis.ontology.ic.TermFrequencyEntityMetaData;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.molgenis.ontology.matching.MatchInputTermBatchService;
import org.molgenis.ontology.matching.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.matching.MatchingTaskEntityMetaData;
import org.molgenis.ontology.matching.OntologyService;
import org.molgenis.ontology.matching.OntologyServiceImpl;
import org.molgenis.ontology.matching.UploadProgress;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.roc.MatchQualityRocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OntologyConfiguration
{
	@Autowired
	private SearchService searchService;

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
	public TermFrequencyEntityMetaData termFrequencyEntityMetaData()
	{
		return TermFrequencyEntityMetaData.INSTANCE;
	}

	@Bean
	public TermFrequencyService termFrequencyService()
	{
		return new OntologyTermFrequencyServiceImpl(dataService);
	}

	@Bean
	public OntologyService ontologyMatchingService()
	{
		return new OntologyServiceImpl(dataService, searchService, informationContentService());
	}

	@Bean
	public MatchInputTermBatchService processInputTermService()
	{
		return new MatchInputTermBatchService(dataService, uploadProgress(), ontologyMatchingService());
	}

	@Bean
	public UploadProgress uploadProgress()
	{
		return new UploadProgress();
	}

	@Bean
	public MatchQualityRocService matchQualityRocService()
	{
		return new MatchQualityRocService(dataService, ontologyMatchingService());
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
