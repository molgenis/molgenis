package org.molgenis.omx.harmonization.config;

import org.molgenis.omx.harmonization.utils.AsyncOntologyIndexer;
import org.molgenis.omx.harmonization.utils.OntologyIndexer;
import org.molgenis.omx.ontologyAnnotator.plugin.AsyncOntologyAnnotator;
import org.molgenis.omx.ontologyAnnotator.plugin.OntologyAnnotator;
import org.molgenis.omx.ontologyMatcher.lucene.AsyncOntologyMatcher;
import org.molgenis.omx.ontologyMatcher.lucene.OntologyMatcher;
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
	public OntologyMatcher luceneMatcher()
	{
		return new AsyncOntologyMatcher();
	}
}
