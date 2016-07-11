package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.ontology.core.meta.OntologyTermSynonymFactory;
import org.molgenis.ontology.ic.OntologyTermFrequencyServiceImpl;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.sorta.meta.OntologyTermHitEntityMetaData;
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

	@Autowired
	private OntologyTermHitEntityMetaData ontologyTermHitEntityMetaData;

	@Autowired
	private OntologyTermSynonymFactory ontologyTermSynonymFactory;

	@Bean
	public TermFrequencyService termFrequencyService()
	{
		return new OntologyTermFrequencyServiceImpl(dataService);
	}

	@Bean
	public SortaService sortaService()
	{
		return new SortaServiceImpl(dataService, informationContentService(), ontologyTermHitEntityMetaData,
				ontologyTermSynonymFactory);
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
