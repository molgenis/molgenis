package org.molgenis.semanticsearch.config;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.meta.model.TagMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.ontology.core.ic.TermFrequencyService;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.semanticsearch.explain.service.ElasticSearchExplainServiceImpl;
import org.molgenis.semanticsearch.explain.service.ExplainServiceHelper;
import org.molgenis.semanticsearch.repository.TagRepository;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.semanticsearch.service.TagService;
import org.molgenis.semanticsearch.service.impl.OntologyTagServiceImpl;
import org.molgenis.semanticsearch.service.impl.SemanticSearchServiceHelper;
import org.molgenis.semanticsearch.service.impl.SemanticSearchServiceImpl;
import org.molgenis.semanticsearch.service.impl.UntypedTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticSearchConfig
{
	@Autowired
	DataService dataService;

	@Autowired
	MetaDataService metaDataService;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	IdGenerator idGenerator;

	@Autowired
	TermFrequencyService termFrequencyService;

	@Autowired
	TagMetadata tagMetadata;

	@Autowired
	TagFactory tagFactory;

	@Autowired
	ElasticsearchService elasticsearchService;

	@Bean
	public SemanticSearchServiceHelper semanticSearchServiceHelper()
	{
		return new SemanticSearchServiceHelper(dataService, ontologyService, termFrequencyService);
	}

	@Bean
	public OntologyTagService ontologyTagService()
	{
		return new OntologyTagServiceImpl(dataService, ontologyService, tagRepository(), idGenerator, tagMetadata);
	}

	@Bean
	public SemanticSearchService semanticSearchService()
	{
		return new SemanticSearchServiceImpl(dataService, ontologyService, metaDataService,
				semanticSearchServiceHelper(), elasticSearchExplainService());
	}

	@Bean
	public TagService<LabeledResource, LabeledResource> tagService()
	{
		return new UntypedTagService(dataService, tagRepository());
	}

	@Bean
	public ExplainServiceHelper explainServiceHelper()
	{
		return new ExplainServiceHelper();
	}

	@Bean
	TagRepository tagRepository()
	{
		return new TagRepository(dataService, idGenerator, tagFactory);
	}

	@Bean
	ElasticSearchExplainService elasticSearchExplainService()
	{
		return new ElasticSearchExplainServiceImpl(elasticsearchService, explainServiceHelper());
	}
}
