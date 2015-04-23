package org.molgenis.ontology.core.config;

import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.core.service.impl.OntologyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OntologyConfig
{
	@Bean
	public OntologyService ontologyService()
	{
		return new OntologyServiceImpl();
	}

	@Bean
	public OntologyRepository ontologyRepository()
	{
		return new OntologyRepository();
	}

	@Bean
	public OntologyTermRepository ontologyTermRepository()
	{
		return new OntologyTermRepository();
	}

}
