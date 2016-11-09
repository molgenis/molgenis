package org.molgenis.data.mapper.config;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.algorithmgenerator.service.impl.AlgorithmGeneratorServiceImpl;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.mapper.meta.MappingProjectMetaData;
import org.molgenis.data.mapper.repository.impl.AttributeMappingRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.MappingProjectRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.MappingTargetRepositoryImpl;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.mapper.service.impl.*;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.ontology.core.config.OntologyConfig;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OntologyConfig.class)
public class MappingConfig
{
	@Autowired
	DataService dataService;

	@Autowired
	UserService userService;

	@Autowired
	OntologyTagService ontologyTagService;

	@Autowired
	SemanticSearchService semanticSearchService;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	IdGenerator idGenerator;

	@Autowired
	PermissionSystemService permissionSystemService;

	@Autowired
	OntologyTermRepository ontologyTermRepository;

	@Autowired
	AttributeMappingMetaData attributeMappingMetaData;

	@Autowired
	AttributeFactory attrMetaFactory;

	@Autowired
	MappingProjectMetaData mappingProjectMeta;

	@Autowired
	EntityManager entityManager;

	@Autowired
	JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

	@Bean
	public MappingService mappingService()
	{
		return new MappingServiceImpl(dataService, algorithmServiceImpl(), idGenerator, mappingProjectRepository(),
				permissionSystemService, attrMetaFactory);
	}

	@Bean
	public AlgorithmGeneratorService algorithmGeneratorService()
	{
		return new AlgorithmGeneratorServiceImpl(dataService, unitResolver(), algorithmTemplateServiceImpl());
	}

	@Bean
	public AlgorithmService algorithmServiceImpl()
	{
		return new AlgorithmServiceImpl(ontologyTagService, semanticSearchService, algorithmGeneratorService(),
				entityManager, jsMagmaScriptEvaluator);
	}

	@Bean
	public AlgorithmTemplateService algorithmTemplateServiceImpl()
	{
		return new AlgorithmTemplateServiceImpl(dataService);
	}

	@Bean
	public MappingProjectRepositoryImpl mappingProjectRepository()
	{
		return new MappingProjectRepositoryImpl(dataService, mappingTargetRepository(), userService,
				idGenerator, mappingProjectMeta);
	}

	@Bean
	public MappingTargetRepositoryImpl mappingTargetRepository()
	{
		return new MappingTargetRepositoryImpl(entityMappingRepository());
	}

	@Bean
	public EntityMappingRepositoryImpl entityMappingRepository()
	{
		return new EntityMappingRepositoryImpl(attributeMappingRepository());
	}

	@Bean
	public AttributeMappingRepositoryImpl attributeMappingRepository()
	{
		return new AttributeMappingRepositoryImpl(dataService, attributeMappingMetaData);
	}

	@Bean
	public UnitResolver unitResolver()
	{
		return new UnitResolverImpl(ontologyService);
	}
}
