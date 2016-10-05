package org.molgenis.data.meta.system;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityTypeMetadata;
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
 * Discovers and initializes {@link SystemEntityType} beans.
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

		EntityTypeMetadata entityTypeMetadata = ctx.getBean(EntityTypeMetadata.class);
		ctx.getBean(AttributeMetaDataMetaData.class).bootstrap(entityTypeMetadata);
		//TODO: doesn't this mean all attributes get added twice?

		Map<String, SystemEntityType> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityType.class);
		genericDependencyResolver.resolve(systemEntityMetaDataMap.values(), SystemEntityType::getDependencies)
				.stream().forEach(systemEntityMetaData -> initialize(systemEntityMetaData, entityTypeMetadata));
	}

	private void initialize(SystemEntityType systemEntityType, EntityTypeMetadata entityTypeMetadata)
	{
		systemEntityType.bootstrap(entityTypeMetadata);
		setDefaultBackend(systemEntityType);
		setPackage(systemEntityType);
		checkPackage(systemEntityType);
	}

	private void setDefaultBackend(SystemEntityType systemEntityType)
	{
		if (systemEntityType.getBackend() == null)
		{
			systemEntityType.setBackend(metaDataService.getDefaultBackend().getName());
		}
	}

	private void checkPackage(SystemEntityType systemEntityType)
	{
		if (!systemEntityType.getPackage().getRootPackage().getName().equals(rootSystemPackage.getName()))
		{
			throw new RuntimeException(
					format("System entity [%s] must be in package [%s]", systemEntityType.getName(),
							rootSystemPackage.getName()));
		}
	}

	private void setPackage(SystemEntityType systemEntityType)
	{
		if (systemEntityType.getPackage() == null)
		{
			if (metaDataService.isMetaEntityMetaData(systemEntityType))
			{
				systemEntityType.setPackage(metaPackage);
			}
			else
			{
				systemEntityType.setPackage(rootSystemPackage);
			}
		}
	}
}
