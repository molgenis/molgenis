package org.molgenis.omx.harmonization.config;

import org.molgenis.omx.harmonization.ontologyannotator.AsyncOntologyAnnotator;
import org.molgenis.omx.harmonization.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.harmonization.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.harmonization.ontologyindexer.OntologyIndexer;
import org.molgenis.omx.harmonization.ontologymatcher.AsyncOntologyMatcher;
import org.molgenis.omx.harmonization.ontologymatcher.OntologyMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class HarmonizationConfig
{
	/**
	 * Get a reference to a HarmonizationIndexer.
	 * 
	 * @return HarmonizationIndexer
	 */
	@Bean
	public OntologyIndexer harmonizationIndexer()
	{
		return new AsyncOntologyIndexer();
	}

	@Bean
	public OntologyAnnotator ontologyAnnotator()
	{
		return new AsyncOntologyAnnotator();
	}

	@Bean
	public OntologyMatcher ontologyMatcher()
	{
		return new AsyncOntologyMatcher();
	}
}
