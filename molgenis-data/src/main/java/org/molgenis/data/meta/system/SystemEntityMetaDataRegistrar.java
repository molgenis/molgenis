package org.molgenis.data.meta.system;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Discovers and registers system entity meta data with the system entity meta data registry.
 *
 * @see SystemEntityType
 * @see SystemEntityMetaDataRegistry
 */
@Component
public class SystemEntityMetaDataRegistrar
{
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;

	@Autowired
	public SystemEntityMetaDataRegistrar(SystemEntityMetaDataRegistry systemEntityMetaDataRegistry)
	{
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, SystemEntityType> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityType.class);
		systemEntityMetaDataMap.values().forEach(this::register);
	}

	private void register(SystemEntityType systemEntityType)
	{
		systemEntityMetaDataRegistry.addSystemEntityMetaData(systemEntityType);
	}
}
