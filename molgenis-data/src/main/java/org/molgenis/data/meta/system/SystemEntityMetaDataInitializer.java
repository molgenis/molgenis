package org.molgenis.data.meta.system;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and initializes {@link SystemEntityMetaData} beans.
 */
@Component
public class SystemEntityMetaDataInitializer
{
	private final MetaDataService metaDataService;
	private final DefaultPackage defaultPackage;

	@Autowired
	public SystemEntityMetaDataInitializer(MetaDataService metaDataService, DefaultPackage defaultPackage)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.defaultPackage = requireNonNull(defaultPackage);
	}

	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, SystemEntityMetaData> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityMetaData.class);
		systemEntityMetaDataMap.values().forEach(this::initialize);
	}

	private void initialize(SystemEntityMetaData systemEntityMetaData)
	{
		systemEntityMetaData.init();
		systemEntityMetaData.setBackend(metaDataService.getDefaultBackend().getName());
		systemEntityMetaData.setPackage(defaultPackage);
	}
}
