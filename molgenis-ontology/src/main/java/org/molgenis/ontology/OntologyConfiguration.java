package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.ontology.index.AsyncOntologyIndexer;
import org.molgenis.ontology.service.OntologyServiceImpl;
import org.molgenis.ontology.service.OntologyServiceSessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class OntologyConfiguration
{
	@Autowired
	private SearchService searchService;

	@Autowired
	private WritableMetaDataService writableMetaDataService;

	@Autowired
	private DataService dataService;

	@Bean
	public OntologyService ontologyService()
	{
		return new OntologyServiceImpl(searchService, dataService);
	}

	/**
	 * Get a reference to a HarmonizationIndexer.
	 * 
	 * @return HarmonizationIndexer
	 */
	@Bean
	public AsyncOntologyIndexer harmonizationIndexer()
	{
		return new AsyncOntologyIndexer(searchService, dataService, ontologyService());
	}

	@Bean
	public OntologyServiceSessionData ontologyServiceSessionData()
	{
		return new OntologyServiceSessionData();
	}
}
