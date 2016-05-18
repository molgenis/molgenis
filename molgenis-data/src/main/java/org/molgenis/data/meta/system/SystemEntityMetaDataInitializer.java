package org.molgenis.data.meta.system;

import java.util.Map;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and initializes {@link SystemEntityMetaData} beans.
 */
@Component
public class SystemEntityMetaDataInitializer
{
	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, SystemEntityMetaData> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityMetaData.class);
		systemEntityMetaDataMap.values().forEach(this::initialize);
	}

	private void initialize(SystemEntityMetaData systemEntityMetaData)
	{
		systemEntityMetaData.init();
	}
}
