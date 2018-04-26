package org.molgenis.data.platform.config;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityFactoryRegistry;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.platform.decorators.SystemRepositoryDecoratorRegistryImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

@Configuration
@Import({ DataServiceImpl.class, MetaDataServiceImpl.class, EntityManagerImpl.class,
		SystemRepositoryDecoratorRegistryImpl.class, EntityFactoryRegistry.class, EntityListenersService.class })
public class PlatformConfig
{
	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private DataService dataService;

	@PostConstruct
	public void init()
	{
		dataService.setMetaDataService(metaDataService);
	}
}
