package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.ontology.service.OntologyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class OntologyCoreConfiguration
{
	@Autowired
	private SearchService searchService;

	@Autowired
	private DataService dataService;

	@Bean
	public OntologyService ontologyService()
	{
		return new OntologyServiceImpl(searchService, dataService);
	}
}
