package org.molgenis.data.mapping;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
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
	WritableMetaDataService writableMetaDataService;

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
		CrudRepository result = dataService.getCrudRepository(AttributeMappingMetaData.ENTITY_NAME);
		if (result == null)
		{
			writableMetaDataService.addEntityMetaData(AttributeMappingRepositoryImpl.META_DATA);
			result = dataService.getCrudRepository(AttributeMappingMetaData.ENTITY_NAME);
		}
		return result;
	}

	private CrudRepository entityMappingCrudRepository()
	{
		CrudRepository result = dataService.getCrudRepository(EntityMappingMetaData.ENTITY_NAME);
		if (result == null)
		{
			writableMetaDataService.addEntityMetaData(EntityMappingRepositoryImpl.META_DATA);
			result = dataService.getCrudRepository(EntityMappingMetaData.ENTITY_NAME);
		}
		return result;
	}

	private CrudRepository mappingProjectCrudRepository()
	{
		CrudRepository result = dataService.getCrudRepository(MappingProjectMetaData.ENTITY_NAME);
		if (result == null)
		{
			writableMetaDataService.addEntityMetaData(MappingProjectRepositoryImpl.META_DATA);
			result = dataService.getCrudRepository(MappingProjectMetaData.ENTITY_NAME);
		}
		return result;
	}

}
