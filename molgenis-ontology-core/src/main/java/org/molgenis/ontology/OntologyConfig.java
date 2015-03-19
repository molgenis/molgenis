package org.molgenis.ontology;

import org.molgenis.ontology.repository.OntologyRepository;
import org.molgenis.ontology.repository.OntologyTermRepository;
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
