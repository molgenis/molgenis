package org.molgenis.data.mapping;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.meta.AttributeMappingMetaData;
import org.molgenis.data.meta.EntityMappingMetaData;
import org.molgenis.data.meta.MappingProjectMetaData;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.repository.AttributeMappingRepository;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.repository.MappingProjectRepository;
import org.molgenis.data.repository.impl.AttributeMappingRepositoryImpl;
import org.molgenis.data.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.data.repository.impl.MappingProjectRepositoryImpl;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingConfig
{
	@Autowired
	ManageableCrudRepositoryCollection repoCollection;

	@Autowired
	DataService dataService;

	@Autowired
	MolgenisUserService userService;

	@Bean
	public MappingService mappingService()
	{
		// create in order of dependency!
		AttributeMappingRepository attributeMappingRepository = attributeMappingRepository();
		EntityMappingRepository entityMappingRepository = entityMappingRepository();
		MappingProjectRepository mappingProjectRepository = mappingProjectRepository();
		return new MappingServiceImpl(attributeMappingRepository, entityMappingRepository, mappingProjectRepository);
	}

	private AttributeMappingRepository attributeMappingRepository()
	{
		return new AttributeMappingRepositoryImpl(attributeMappingCrudRepository());
	}

	private EntityMappingRepository entityMappingRepository()
	{
		return new EntityMappingRepositoryImpl(entityMappingCrudRepository());
	}

	private MappingProjectRepository mappingProjectRepository()
	{
		return new MappingProjectRepositoryImpl(mappingProjectCrudRepository(), userService);
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

	private CrudRepository mappingProjectCrudRepository()
	{
		if (!dataService.hasRepository(MappingProjectMetaData.ENTITY_NAME))
		{
			repoCollection.add(MappingProjectRepositoryImpl.META_DATA);
		}
		return dataService.getCrudRepository(MappingProjectMetaData.ENTITY_NAME);
	}

}
