package org.molgenis.data.mapping;

import java.util.UUID;

import org.molgenis.data.DataService;
import org.molgenis.data.algorithm.AlgorithmService;
import org.molgenis.data.algorithm.AlgorithmServiceImpl;
import org.molgenis.data.repository.impl.AttributeMappingRepositoryImpl;
import org.molgenis.data.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.data.repository.impl.MappingProjectRepositoryImpl;
import org.molgenis.data.repository.impl.MappingTargetRepositoryImpl;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;

@Configuration
public class MappingConfig
{
	@Autowired
	DataService dataService;

	@Autowired
	MolgenisUserService userService;

	@Bean
	public IdGenerator idGenerator()
	{
		return new IdGenerator()
		{
			@Override
			public UUID generateId()
			{
				com.eaio.uuid.UUID uuid = new com.eaio.uuid.UUID();
				return UUID.fromString(uuid.toString());
			}
		};
	}

	@Bean
	public MappingService mappingService()
	{
		return new MappingServiceImpl();
	}

	@Bean
	public AlgorithmService algorithmServiceImpl()
	{
		return new AlgorithmServiceImpl(dataService);
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
