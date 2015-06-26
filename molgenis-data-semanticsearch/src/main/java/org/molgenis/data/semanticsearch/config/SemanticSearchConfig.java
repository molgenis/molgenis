package org.molgenis.data.semanticsearch.config;

import org.molgenis.data.DataService;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainServiceImpl;
import org.molgenis.data.semanticsearch.explain.service.ExplainServiceHelper;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.semanticsearch.service.impl.OntologyTagServiceImpl;
import org.molgenis.data.semanticsearch.service.impl.SemanticSearchServiceHelper;
import org.molgenis.data.semanticsearch.service.impl.SemanticSearchServiceImpl;
import org.molgenis.data.semanticsearch.service.impl.UntypedTagService;
import org.molgenis.ontology.core.service.OntologyService;
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
	SemanticSearchServiceHelper semanticSearchServiceHelper;

	@Autowired
	EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@Bean
	public OntologyTagService ontologyTagService()
	{
		return new OntologyTagServiceImpl(dataService, ontologyService, tagRepository(), idGenerator);
	}

	@Bean
	public SemanticSearchService semanticSearchService()
	{
		return new SemanticSearchServiceImpl();
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
		Repository repo = dataService.getRepository(TagMetaData.ENTITY_NAME);
		return new TagRepository(repo, idGenerator);
	}

	@Bean
	ElasticSearchExplainService elasticSearchExplainService()
	{
		return new ElasticSearchExplainServiceImpl(embeddedElasticSearchServiceFactory.getClient(),
				explainServiceHelper());
	}
}
