package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.semantic.SemanticSearchService;
import org.molgenis.ontology.index.AsyncOntologyIndexer;
import org.molgenis.ontology.search.SemanticSearchServiceImpl;
import org.molgenis.ontology.service.OntologyService;
import org.molgenis.ontology.service.OntologyServiceSessionData;
import org.molgenis.ontology.tag.AsyncOntologyAnnotator;
import org.molgenis.ontology.tag.OntologyAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class OntologyConfig
{
	@Autowired
	private SearchService searchService;

	@Autowired
	private DataService dataService;

	/**
	 * Get a reference to a HarmonizationIndexer.
	 * 
	 * @return HarmonizationIndexer
	 */
	@Bean
	public AsyncOntologyIndexer harmonizationIndexer()
	{
		return new AsyncOntologyIndexer(searchService, dataService);
	}

	@Bean
	public OntologyAnnotator ontologyAnnotator()
	{
		return new AsyncOntologyAnnotator();
	}

	@Bean
	public SemanticSearchService semanticSearchService()
	{
		return new SemanticSearchServiceImpl(dataService, searchService);
	}

	@Bean
	public OntologyService ontologyService()
	{
		return new OntologyService(searchService);
	}

	@Bean
	public OntologyServiceSessionData ontologyServiceSessionData()
	{
		return new OntologyServiceSessionData();
	}
}
