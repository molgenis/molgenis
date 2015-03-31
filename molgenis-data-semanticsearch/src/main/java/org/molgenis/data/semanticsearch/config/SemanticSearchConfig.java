package org.molgenis.data.semanticsearch.config;

import org.molgenis.data.DataService;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.service.impl.OntologyTagService;
import org.molgenis.data.semanticsearch.service.impl.SemanticSearchServiceImpl;
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
