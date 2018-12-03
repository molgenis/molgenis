package org.molgenis.semanticmapper.config;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.ontology.core.config.OntologyConfig;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticmapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.semanticmapper.algorithmgenerator.service.impl.AlgorithmGeneratorServiceImpl;
import org.molgenis.semanticmapper.meta.AttributeMappingMetaData;
import org.molgenis.semanticmapper.meta.MappingProjectMetaData;
import org.molgenis.semanticmapper.repository.impl.AttributeMappingRepositoryImpl;
import org.molgenis.semanticmapper.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.semanticmapper.repository.impl.MappingProjectRepositoryImpl;
import org.molgenis.semanticmapper.repository.impl.MappingTargetRepositoryImpl;
import org.molgenis.semanticmapper.service.AlgorithmService;
import org.molgenis.semanticmapper.service.MappingService;
import org.molgenis.semanticmapper.service.UnitResolver;
import org.molgenis.semanticmapper.service.impl.AlgorithmServiceImpl;
import org.molgenis.semanticmapper.service.impl.AlgorithmTemplateService;
import org.molgenis.semanticmapper.service.impl.AlgorithmTemplateServiceImpl;
import org.molgenis.semanticmapper.service.impl.MappingServiceImpl;
import org.molgenis.semanticmapper.service.impl.UnitResolverImpl;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OntologyConfig.class)
public class MappingConfig {
  private final DataService dataService;
  private final SemanticSearchService semanticSearchService;
  private final OntologyService ontologyService;
  private final IdGenerator idGenerator;
  private final PermissionSystemService permissionSystemService;
  private final AttributeMappingMetaData attributeMappingMetaData;
  private final AttributeFactory attrMetaFactory;
  private final MappingProjectMetaData mappingProjectMeta;
  private final EntityManager entityManager;
  private final JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

  public MappingConfig(
      PermissionSystemService permissionSystemService,
      DataService dataService,
      SemanticSearchService semanticSearchService,
      OntologyService ontologyService,
      IdGenerator idGenerator,
      AttributeMappingMetaData attributeMappingMetaData,
      AttributeFactory attrMetaFactory,
      MappingProjectMetaData mappingProjectMeta,
      EntityManager entityManager,
      JsMagmaScriptEvaluator jsMagmaScriptEvaluator) {
    this.permissionSystemService = requireNonNull(permissionSystemService);
    this.dataService = requireNonNull(dataService);
    this.semanticSearchService = requireNonNull(semanticSearchService);
    this.ontologyService = requireNonNull(ontologyService);
    this.idGenerator = requireNonNull(idGenerator);
    this.attributeMappingMetaData = requireNonNull(attributeMappingMetaData);
    this.attrMetaFactory = requireNonNull(attrMetaFactory);
    this.mappingProjectMeta = requireNonNull(mappingProjectMeta);
    this.entityManager = requireNonNull(entityManager);
    this.jsMagmaScriptEvaluator = requireNonNull(jsMagmaScriptEvaluator);
  }

  @Bean
  public MappingService mappingService() {
    return new MappingServiceImpl(
        dataService,
        algorithmServiceImpl(),
        mappingProjectRepository(),
        permissionSystemService,
        attrMetaFactory,
        entityManager);
  }

  @Bean
  public AlgorithmGeneratorService algorithmGeneratorService() {
    return new AlgorithmGeneratorServiceImpl(
        dataService, unitResolver(), algorithmTemplateServiceImpl());
  }

  @Bean
  public AlgorithmService algorithmServiceImpl() {
    return new AlgorithmServiceImpl(
        semanticSearchService, algorithmGeneratorService(), entityManager, jsMagmaScriptEvaluator);
  }

  @Bean
  public AlgorithmTemplateService algorithmTemplateServiceImpl() {
    return new AlgorithmTemplateServiceImpl(dataService);
  }

  @Bean
  public MappingProjectRepositoryImpl mappingProjectRepository() {
    return new MappingProjectRepositoryImpl(
        dataService, mappingTargetRepository(), idGenerator, mappingProjectMeta);
  }

  @Bean
  public MappingTargetRepositoryImpl mappingTargetRepository() {
    return new MappingTargetRepositoryImpl(entityMappingRepository());
  }

  @Bean
  public EntityMappingRepositoryImpl entityMappingRepository() {
    return new EntityMappingRepositoryImpl(attributeMappingRepository());
  }

  @Bean
  public AttributeMappingRepositoryImpl attributeMappingRepository() {
    return new AttributeMappingRepositoryImpl(dataService, attributeMappingMetaData);
  }

  @Bean
  public UnitResolver unitResolver() {
    return new UnitResolverImpl(ontologyService);
  }
}
