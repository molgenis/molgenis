package org.molgenis.data.config;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityFactoryRegistry;
import org.molgenis.data.EntityReferenceCreator;
import org.molgenis.data.EntityReferenceCreatorImpl;
import org.molgenis.data.populate.AutoValuePopulator;
import org.molgenis.data.populate.DefaultValuePopulator;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static java.util.Objects.requireNonNull;

@Configuration
@Import({ RootSystemPackage.class, EntityPopulator.class, AutoValuePopulator.class, DefaultValuePopulator.class,
		IdGeneratorImpl.class, EntityFactoryRegistry.class })
public class EntityBaseTestConfig
{
	private final DataService dataService;
	private final EntityFactoryRegistry entityFactoryRegistry;

	public EntityBaseTestConfig(DataService dataService, EntityFactoryRegistry entityFactoryRegistry)
	{

		this.dataService = requireNonNull(dataService);
		this.entityFactoryRegistry = requireNonNull(entityFactoryRegistry);
	}

	@Bean
	public EntityReferenceCreator entityReferenceCreator()
	{
		return new EntityReferenceCreatorImpl(dataService, entityFactoryRegistry);
	}
}
