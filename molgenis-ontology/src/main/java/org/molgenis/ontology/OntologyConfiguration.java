package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.ontology.matching.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.matching.MatchingTaskEntityMetaData;
import org.molgenis.ontology.matching.OntologyMatchingService;
import org.molgenis.ontology.matching.OntologyMatchingServiceImpl;
import org.molgenis.ontology.matching.ProcessInputTermService;
import org.molgenis.ontology.matching.UploadProgress;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OntologyConfiguration
{
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

	@Autowired
	private SearchService searchService;

	@Autowired
	private EmxImportService emxImportService;

	@Autowired
	private DataService dataService;

	@Bean
	public OntologyMatchingService ontologyMatchingService()
	{
		return new OntologyMatchingServiceImpl(dataService, searchService);
	}

	@Bean
	public ProcessInputTermService processInputTermService()
	{
		return new ProcessInputTermService(emxImportService, dataService, uploadProgress(), ontologyMatchingService());
	}

	//
	// /**
	// * Get a reference to a HarmonizationIndexer.
	// *
	// * @return HarmonizationIndexer
	// */
	// @Bean
	// public AsyncOntologyIndexer harmonizationIndexer()
	// {
	// return new AsyncOntologyIndexer(searchService, dataService, ontologyService());
	// }

	@Bean
	public UploadProgress uploadProgress()
	{
		return new UploadProgress();
	}

	public OntologyConfiguration()
	{
		System.setProperty("jdk.xml.entityExpansionLimit", "1280000");
	}
}
