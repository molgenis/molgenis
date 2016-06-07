package org.molgenis.data.meta.system;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.molgenis.data.SystemEntityFactory;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and registers {@link SystemEntityMetaData} beans with the {@link SystemEntityMetaDataRegistry}.
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
		Map<String, SystemEntityMetaData> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityMetaData.class);
		systemEntityMetaDataMap.values().forEach(this::register);

		Map<String, SystemEntityFactory> entityFactoryMap = ctx.getBeansOfType(SystemEntityFactory.class);
		entityFactoryMap.values().forEach(this::register);
	}

	private void register(SystemEntityMetaData systemEntityMetaData)
	{
		systemEntityMetaDataRegistry.addSystemEntityMetaData(systemEntityMetaData);
	}

	private void register(SystemEntityFactory systemEntityFactory)
	{
		systemEntityMetaDataRegistry.addSystemEntityFactory(systemEntityFactory);
	}
}
