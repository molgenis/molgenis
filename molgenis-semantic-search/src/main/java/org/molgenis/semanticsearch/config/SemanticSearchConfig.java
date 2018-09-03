package org.molgenis.semanticsearch.config;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchService;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticSearchConfig {
  private final DataService dataService;
  private final OntologyService ontologyService;
  private final IdGenerator idGenerator;
  private final TermFrequencyService termFrequencyService;
  private final TagMetadata tagMetadata;
  private final TagFactory tagFactory;
  private final ElasticsearchService elasticsearchService;

  public SemanticSearchConfig(
      DataService dataService,
      OntologyService ontologyService,
      IdGenerator idGenerator,
      TermFrequencyService termFrequencyService,
      TagMetadata tagMetadata,
      TagFactory tagFactory,
      ElasticsearchService elasticsearchService) {
    this.dataService = requireNonNull(dataService);
    this.ontologyService = requireNonNull(ontologyService);
    this.idGenerator = requireNonNull(idGenerator);
    this.termFrequencyService = requireNonNull(termFrequencyService);
    this.tagMetadata = requireNonNull(tagMetadata);
    this.tagFactory = requireNonNull(tagFactory);
    this.elasticsearchService = requireNonNull(elasticsearchService);
  }

  @Bean
  public SemanticSearchServiceHelper semanticSearchServiceHelper() {
    return new SemanticSearchServiceHelper(dataService, ontologyService, termFrequencyService);
  }

  @Bean
  public OntologyTagService ontologyTagService() {
    return new OntologyTagServiceImpl(
        dataService, ontologyService, tagRepository(), idGenerator, tagMetadata);
  }

  @Bean
  public SemanticSearchService semanticSearchService() {
    return new SemanticSearchServiceImpl(
        dataService,
        ontologyService,
        semanticSearchServiceHelper(),
        elasticSearchExplainService(),
        ontologyTagService());
  }

  @Bean
  public TagService<LabeledResource, LabeledResource> tagService() {
    return new UntypedTagService(dataService, tagRepository());
  }

  @Bean
  public ExplainServiceHelper explainServiceHelper() {
    return new ExplainServiceHelper();
  }

  @Bean
  TagRepository tagRepository() {
    return new TagRepository(dataService, idGenerator, tagFactory);
  }

  @Bean
  ElasticSearchExplainService elasticSearchExplainService() {
    return new ElasticSearchExplainServiceImpl(elasticsearchService, explainServiceHelper());
  }
}
