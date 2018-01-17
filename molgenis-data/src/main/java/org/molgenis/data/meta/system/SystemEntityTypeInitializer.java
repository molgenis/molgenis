package org.molgenis.data.meta.system;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.MetaPackage;
import org.molgenis.data.system.model.RootSystemPackage;
import org.molgenis.data.util.GenericDependencyResolver;
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
public class SystemEntityTypeInitializer
{
	private final MetaDataService metaDataService;
	private final RootSystemPackage rootSystemPackage;
	private final MetaPackage metaPackage;
	private final GenericDependencyResolver genericDependencyResolver;

	public SystemEntityTypeInitializer(MetaDataService metaDataService, RootSystemPackage rootSystemPackage,
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

		EntityTypeMetadata entityTypeMetaData = ctx.getBean(EntityTypeMetadata.class);
		ctx.getBean(AttributeMetadata.class).bootstrap(entityTypeMetaData);
		//TODO: doesn't this mean all attributes get added twice?

		Map<String, SystemEntityType> systemEntityTypeMap = ctx.getBeansOfType(SystemEntityType.class);
		genericDependencyResolver.resolve(systemEntityTypeMap.values(), SystemEntityType::getDependencies)
								 .forEach(systemEntityType -> initialize(systemEntityType, entityTypeMetaData));
	}

	private void initialize(SystemEntityType systemEntityType, EntityTypeMetadata entityTypeMetaData)
	{
		systemEntityType.bootstrap(entityTypeMetaData);
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
		if (!systemEntityType.getPackage().getRootPackage().getId().equals(rootSystemPackage.getId()))
		{
			throw new RuntimeException(format("System entity [%s] must be in package [%s]", systemEntityType.getId(),
					rootSystemPackage.getId()));
		}
	}

	private void setPackage(SystemEntityType systemEntityType)
	{
		if (systemEntityType.getPackage() == null)
		{
			if (MetaDataService.isMetaEntityType(systemEntityType))
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
