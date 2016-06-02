package org.molgenis.data.meta.system;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaPackage;
import org.molgenis.data.meta.RootSystemPackage;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.SystemPackage;
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
	private final RootSystemPackage rootSystemPackage;
	private final MetaPackage metaPackage;

	@Autowired
	public SystemEntityMetaDataInitializer(MetaDataService metaDataService, RootSystemPackage rootSystemPackage,
			MetaPackage metaPackage)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
		this.metaPackage = requireNonNull(metaPackage);
	}

	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();

		ctx.getBeansOfType(SystemPackage.class).values().forEach(SystemPackage::bootstrap);

		EntityMetaDataMetaData entityMetaDataMetaData = ctx.getBean(EntityMetaDataMetaData.class);
		ctx.getBean(AttributeMetaDataMetaData.class).bootstrap(entityMetaDataMetaData);
		Map<String, SystemEntityMetaData> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityMetaData.class);
		systemEntityMetaDataMap.values()
				.forEach(systemEntityMetaData -> initialize(systemEntityMetaData, entityMetaDataMetaData));
	}

	private void initialize(SystemEntityMetaData systemEntityMetaData, EntityMetaDataMetaData entityMetaDataMetaData)
	{
		systemEntityMetaData.bootstrap(entityMetaDataMetaData);
		if (systemEntityMetaData.getBackend() == null)
		{
			systemEntityMetaData.setBackend(metaDataService.getDefaultBackend().getName());
		}
		if (systemEntityMetaData.getPackage() == null)
		{
			if (metaDataService.isMetaEntityMetaData(systemEntityMetaData))
			{
				systemEntityMetaData.setPackage(metaPackage);
			}
			else
			{
				systemEntityMetaData.setPackage(rootSystemPackage);
			}
		}
		else if (!systemEntityMetaData.getPackage().getRootPackage().getName().equals(rootSystemPackage.getName()))
		{
			throw new RuntimeException(
					format("System entity [%s] must be in package [%s]", systemEntityMetaData.getName(),
							rootSystemPackage.getName()));
		}
	}
}
