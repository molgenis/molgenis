package org.molgenis.omx.biobankconnect;

import org.molgenis.omx.biobankconnect.algorithm.AlgorithmGenerator;
import org.molgenis.omx.biobankconnect.algorithm.AlgorithmScriptLibrary;
import org.molgenis.omx.biobankconnect.algorithm.AlgorithmUnitConverter;
import org.molgenis.omx.biobankconnect.algorithm.ApplyAlgorithms;
import org.molgenis.omx.biobankconnect.ontologyannotator.AsyncOntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologymatcher.AsyncOntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class BiobankConnectConfig
{
	@Autowired
	private SearchService searchService;

	/**
	 * Get a reference to a HarmonizationIndexer.
	 * 
	 * @return HarmonizationIndexer
	 */
	@Bean
	public AsyncOntologyIndexer harmonizationIndexer()
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
	public ApplyAlgorithms applyAlgorithms()
	{
		return new ApplyAlgorithms();
	}

	@Bean
	public AlgorithmGenerator algorithmGenerator()
	{
		return new AlgorithmGenerator();
	}

	@Bean
	public OntologyService ontologyService()
	{
		return new OntologyService(searchService);
	}

	@Bean
	public CurrentUserStatus currentUserStatus()
	{
		return new CurrentUserStatus();
	}
}
