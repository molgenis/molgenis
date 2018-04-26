package org.molgenis.ontology.core.config;

import org.molgenis.data.DataService;
import org.molgenis.ontology.core.ic.OntologyTermFrequencyServiceImpl;
import org.molgenis.ontology.core.ic.TermFrequencyService;
import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.core.service.impl.OntologyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OntologyConfig
{
	@Autowired
	DataService dataService;

	@Bean
	public OntologyService ontologyService()
	{
		return new OntologyServiceImpl(ontologyRepository(), ontologyTermRepository());
	}

	@Bean
	public OntologyRepository ontologyRepository()
	{
		return new OntologyRepository();
	}

	@Bean
	public OntologyTermRepository ontologyTermRepository()
	{
		return new OntologyTermRepository(dataService);
	}

	@Bean
	public TermFrequencyService termFrequencyService()
	{
		return new OntologyTermFrequencyServiceImpl(dataService);
	}
}
