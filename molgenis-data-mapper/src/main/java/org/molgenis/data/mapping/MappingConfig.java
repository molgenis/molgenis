package org.molgenis.data.mapping;

import java.util.UUID;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.meta.AttributeMappingMetaData;
import org.molgenis.data.meta.EntityMappingMetaData;
import org.molgenis.data.meta.MappingProjectMetaData;
import org.molgenis.data.meta.MappingTargetMetaData;
import org.molgenis.data.repository.AttributeMappingRepository;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.repository.MappingProjectRepository;
import org.molgenis.data.repository.MappingTargetRepository;
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
	ManageableCrudRepositoryCollection repoCollection;

	@Autowired
	DataService dataService;

	@Autowired
	MolgenisUserService userService;

	private AttributeMappingRepository attributeMappingRepository;

	private EntityMappingRepository entityMappingRepository;

	private MappingProjectRepository mappingProjectRepository;

	private MappingTargetRepository mappingTargetRepository;

	@Bean
	public IdGenerator idGenerator()
	{
		return new IdGenerator() // TODO something more efficient..
		{
			@Override
			public UUID generateId()
			{
				return UUID.randomUUID();
			}
		};
	}

	@Bean
	public MappingService mappingService()
	{
		// create in order of dependency!
		attributeMappingRepository = new AttributeMappingRepositoryImpl(attributeMappingCrudRepository());
		entityMappingRepository = new EntityMappingRepositoryImpl(entityMappingCrudRepository(),
				attributeMappingRepository);
		mappingTargetRepository = new MappingTargetRepositoryImpl(mappingTargetCrudRepository(),
				entityMappingRepository);
		mappingProjectRepository = new MappingProjectRepositoryImpl(mappingProjectCrudRepository(),
				mappingTargetRepository);
		return new MappingServiceImpl(attributeMappingRepository, entityMappingRepository, mappingProjectRepository);
	}

	private CrudRepository attributeMappingCrudRepository()
	{
		if (!dataService.hasRepository(AttributeMappingMetaData.ENTITY_NAME))
		{
			repoCollection.add(AttributeMappingRepositoryImpl.META_DATA);
		}
		return dataService.getCrudRepository(AttributeMappingMetaData.ENTITY_NAME);

	}

	private CrudRepository entityMappingCrudRepository()
	{
		if (!dataService.hasRepository(EntityMappingMetaData.ENTITY_NAME))
		{
			repoCollection.add(EntityMappingRepositoryImpl.META_DATA);
		}
		return dataService.getCrudRepository(EntityMappingMetaData.ENTITY_NAME);
	}

	private CrudRepository mappingTargetCrudRepository()
	{
		if (!dataService.hasRepository(MappingTargetMetaData.ENTITY_NAME))
		{
			repoCollection.add(MappingTargetRepositoryImpl.META_DATA);
		}
		return dataService.getCrudRepository(MappingTargetMetaData.ENTITY_NAME);
	}

	private CrudRepository mappingProjectCrudRepository()
	{
		if (!dataService.hasRepository(MappingProjectMetaData.ENTITY_NAME))
		{
			repoCollection.add(MappingProjectRepositoryImpl.META_DATA);
		}
		return dataService.getCrudRepository(MappingProjectMetaData.ENTITY_NAME);
	}

}
