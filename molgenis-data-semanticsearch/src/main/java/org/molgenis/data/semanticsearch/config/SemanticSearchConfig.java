package org.molgenis.data.semanticsearch.config;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.meta.model.TagMetaData;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.explain.service.ExplainMappingService;
import org.molgenis.data.semanticsearch.explain.service.impl.ExplainMappingServiceImpl;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.service.*;
import org.molgenis.data.semanticsearch.service.impl.*;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticSearchConfig
{
	@Autowired
	DataService dataService;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	IdGenerator idGenerator;

	@Autowired
	TermFrequencyService termFrequencyService;

	@Autowired
	TagMetaData tagMetaData;

	@Autowired
	TagFactory tagFactory;

	@Bean
	public OntologyTagService ontologyTagService()
	{
		return new OntologyTagServiceImpl(dataService, ontologyService, tagRepository(), idGenerator, tagMetaData);
	}

	@Bean
	public SemanticSearchService semanticSearchService()
	{
		return new SemanticSearchServiceImpl(dataService, ontologyService, tagGroupGenerator(), queryExpansionService(),
				explainMappingService());
	}

	@Bean
	public TagService<LabeledResource, LabeledResource> tagService()
	{
		return new UntypedTagService(dataService, tagRepository());
	}

	@Bean
	public QueryExpansionService queryExpansionService()
	{
		return new QueryExpansionServiceImpl(ontologyService, termFrequencyService);
	}

	@Bean
	public TagGroupGenerator tagGroupGenerator()
	{
		return new TagGroupGeneratorImpl(ontologyService);
	}

	@Bean
	public ExplainMappingService explainMappingService()
	{
		return new ExplainMappingServiceImpl(ontologyService, tagGroupGenerator());
	}

	@Bean
	TagRepository tagRepository()
	{
		return new TagRepository(dataService, idGenerator, tagFactory);
	}
}
