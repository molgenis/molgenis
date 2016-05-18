package org.molgenis.data.meta.system;

import java.util.Map;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/**
 * Discovers and registers {@link SystemEntityMetaData} beans with the {@link SystemEntityMetaDataRegistry}
 */
@Component
public class SystemEntityMetaDataRegistrar
{
	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, SystemEntityMetaData> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityMetaData.class);
		systemEntityMetaDataMap.values().forEach(this::register);
	}

	private void register(SystemEntityMetaData systemEntityMetaData)
	{
		SystemEntityMetaDataRegistry.INSTANCE.addSystemEntityMetaData(systemEntityMetaData);
	}
}
