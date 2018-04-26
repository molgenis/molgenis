package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.ontology.core.ic.OntologyTermFrequencyServiceImpl;
import org.molgenis.ontology.core.ic.TermFrequencyService;
import org.molgenis.ontology.core.meta.OntologyTermSynonymFactory;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SortaConfig
{
	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyTermHitMetaData ontologyTermHitMetaData;

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
		return new SortaServiceImpl(dataService, informationContentService(), ontologyTermHitMetaData,
				ontologyTermSynonymFactory);
	}

	@Bean
	public InformationContentService informationContentService()
	{
		return new InformationContentService(dataService);
	}

	public SortaConfig()
	{
		System.setProperty("jdk.xml.entityExpansionLimit", "1280000");
	}
}
