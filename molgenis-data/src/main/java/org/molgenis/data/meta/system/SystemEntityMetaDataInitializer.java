package org.molgenis.data.meta.system;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.meta.model.MetaPackage;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

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
		//FIXME: can we do this cleaner than with the hardcoded "Owned". Problem: even the "isAbstract" nullpoiters at this point.
		systemEntityMetaDataMap.values().stream()
				.filter(systemEntityMetaData -> systemEntityMetaData.getSimpleName().equals("Owned"))
				.forEach(systemEntityMetaData -> initialize(systemEntityMetaData, entityMetaDataMetaData));
		systemEntityMetaDataMap.values().stream()
				.filter(systemEntityMetaData -> !systemEntityMetaData.getSimpleName().equals("Owned"))
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
