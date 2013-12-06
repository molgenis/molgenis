package org.molgenis.omx.biobankconnect;

import org.molgenis.omx.biobankconnect.ontologyannotator.AsyncOntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologyindexer.OntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologymatcher.AsyncOntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class BiobankConnectConfig
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

	@Bean
	public CurrentUserStatus currentUserStatus()
	{
		return new CurrentUserStatus();
	}
}
