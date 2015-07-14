package org.molgenis.data.mapper.config;

import org.molgenis.data.DataService;
import org.molgenis.data.mapper.repository.impl.AttributeMappingRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.MappingProjectRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.MappingTargetRepositoryImpl;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.mapper.service.impl.AlgorithmServiceImpl;
import org.molgenis.data.mapper.service.impl.MappingServiceImpl;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingConfig
{
	@Autowired
	DataService dataService;

	@Autowired
	MolgenisUserService userService;

	@Autowired
	OntologyTagService ontologyTagService;

	@Autowired
	SemanticSearchService semanticSearchService;

	@Bean
	public MappingService mappingService()
	{
		return new MappingServiceImpl();
	}

	@Bean
	public AlgorithmService algorithmServiceImpl()
	{
		return new AlgorithmServiceImpl(dataService, ontologyTagService, semanticSearchService);
	}

	@Bean
	public MappingProjectRepositoryImpl mappingProjectRepository()
	{
		return new MappingProjectRepositoryImpl(dataService, mappingTargetRepository());
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
		return new AttributeMappingRepositoryImpl(dataService);
	}

}
