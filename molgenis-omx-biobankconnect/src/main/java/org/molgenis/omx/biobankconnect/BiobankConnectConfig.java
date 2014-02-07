package org.molgenis.omx.biobankconnect;

import org.molgenis.omx.biobankconnect.algorithm.AlgorithmGenerator;
import org.molgenis.omx.biobankconnect.algorithm.AlgorithmScriptLibrary;
import org.molgenis.omx.biobankconnect.algorithm.AlgorithmUnitConverter;
import org.molgenis.omx.biobankconnect.ontologyannotator.AsyncOntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologyindexer.OntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologymatcher.AsyncOntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
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
	@Scope("prototype")
	public OntologyMatcher ontologyMatcher()
	{
		return new AsyncOntologyMatcher();
	}

	@Bean
	public AlgorithmUnitConverter algorithmUnitConverter() throws IllegalArgumentException, IllegalAccessException
	{
		return new AlgorithmUnitConverter();
	}

	@Bean
	public AlgorithmScriptLibrary algorithmScriptLibrary()
	{
		return new AlgorithmScriptLibrary();
	}

	@Bean
	public AlgorithmGenerator algorithmGenerator()
	{
		return new AlgorithmGenerator();
	}

	@Bean
	public CurrentUserStatus currentUserStatus()
	{
		return new CurrentUserStatus();
	}
}
