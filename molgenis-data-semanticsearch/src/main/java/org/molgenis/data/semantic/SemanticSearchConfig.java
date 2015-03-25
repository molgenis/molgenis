package org.molgenis.data.semantic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.molgenis.data.DataService;
import org.molgenis.data.IdGenerator;
import org.molgenis.ontology.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticSearchConfig
{
	@Autowired
	DataService dataService;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	TagRepository tagRepository;

	@Autowired
	IdGenerator idGenerator;

	@Bean
	ExecutorService executorService()
	{
		return Executors.newFixedThreadPool(1);
	}

	@Bean
	public OntologyTagService ontologyTagService()
	{
		return new OntologyTagService(dataService, ontologyService, tagRepository, idGenerator);
	}

	@Bean
	public SemanticSearchService semanticSearchService()
	{
		return new SemanticSearchServiceImpl();
	}
	
}
