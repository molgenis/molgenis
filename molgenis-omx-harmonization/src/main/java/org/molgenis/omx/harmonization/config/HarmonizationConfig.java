package org.molgenis.omx.harmonization.config;

import org.molgenis.omx.harmonization.plugin.AsyncOntologyAnnotator;
import org.molgenis.omx.harmonization.plugin.OntologyAnnotator;
import org.molgenis.omx.ontologyIndexer.plugin.AsyncOntologyIndexer;
import org.molgenis.omx.ontologyIndexer.plugin.OntologyIndexer;
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
}
