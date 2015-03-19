package org.molgenis.data.semantic;

import org.molgenis.data.DataService;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.repository.OntologyRepository;
import org.molgenis.ontology.repository.OntologyTermRepository;
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

	@Bean
	public OntologyTagService ontologyTagService()
	{
		return new OntologyTagService(dataService, ontologyService, tagRepository);
	}
}
