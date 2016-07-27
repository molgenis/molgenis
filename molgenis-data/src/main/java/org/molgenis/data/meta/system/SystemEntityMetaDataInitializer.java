package org.molgenis.data.meta.system;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.meta.model.MetaPackage;
import org.molgenis.data.system.model.RootSystemPackage;
import org.molgenis.util.GenericDependencyResolver;
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
	private final GenericDependencyResolver genericDependencyResolver;

	@Autowired
	public SystemEntityMetaDataInitializer(MetaDataService metaDataService, RootSystemPackage rootSystemPackage,
			MetaPackage metaPackage, GenericDependencyResolver genericDependencyResolver)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
		this.metaPackage = requireNonNull(metaPackage);
		this.genericDependencyResolver = genericDependencyResolver;
	}

	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();

		ctx.getBeansOfType(SystemPackage.class).values().forEach(SystemPackage::bootstrap);

		EntityMetaDataMetaData entityMetaDataMetaData = ctx.getBean(EntityMetaDataMetaData.class);
		ctx.getBean(AttributeMetaDataMetaData.class).bootstrap(entityMetaDataMetaData);
		//TODO: doesn't this mean all attributes get added twice?

		Map<String, SystemEntityMetaData> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityMetaData.class);
		genericDependencyResolver.resolve(systemEntityMetaDataMap.values(), SystemEntityMetaData::getDependencies)
				.stream().forEach(systemEntityMetaData -> initialize(systemEntityMetaData, entityMetaDataMetaData));
	}

	private void initialize(SystemEntityMetaData systemEntityMetaData, EntityMetaDataMetaData entityMetaDataMetaData)
	{
		systemEntityMetaData.bootstrap(entityMetaDataMetaData);
		setDefaultBackend(systemEntityMetaData);
		setPackage(systemEntityMetaData);
		checkPackage(systemEntityMetaData);
	}

	private void setDefaultBackend(SystemEntityMetaData systemEntityMetaData)
	{
		if (systemEntityMetaData.getBackend() == null)
		{
			systemEntityMetaData.setBackend(metaDataService.getDefaultBackend().getName());
		}
	}

	private void checkPackage(SystemEntityMetaData systemEntityMetaData)
	{
		if (!systemEntityMetaData.getPackage().getRootPackage().getName().equals(rootSystemPackage.getName()))
		{
			throw new RuntimeException(
					format("System entity [%s] must be in package [%s]", systemEntityMetaData.getName(),
							rootSystemPackage.getName()));
		}
	}

	private void setPackage(SystemEntityMetaData systemEntityMetaData)
	{
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
	}
}
