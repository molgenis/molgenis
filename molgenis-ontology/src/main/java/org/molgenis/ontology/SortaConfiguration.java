package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.ontology.ic.OntologyTermFrequencyServiceImpl;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.roc.MatchQualityRocService;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SortaConfiguration
{
	@Autowired
	private DataService dataService;

	@Bean
	public TermFrequencyService termFrequencyService()
	{
		return new OntologyTermFrequencyServiceImpl(dataService);
	}

	@Bean
	public SortaService sortaService()
	{
		return new SortaServiceImpl(dataService, informationContentService());
	}

	@Bean
	public MatchQualityRocService matchQualityRocService()
	{
		return new MatchQualityRocService(dataService, sortaService());
	}

	@Bean
	public InformationContentService informationContentService()
	{
		return new InformationContentService(dataService);
	}

	public SortaConfiguration()
	{
		System.setProperty("jdk.xml.entityExpansionLimit", "1280000");
	}
}
