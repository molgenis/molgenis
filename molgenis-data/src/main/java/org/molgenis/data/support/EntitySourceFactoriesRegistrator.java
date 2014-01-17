package org.molgenis.data.support;

import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.EntitySourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class EntitySourceFactoriesRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;

	@Autowired
	public EntitySourceFactoriesRegistrator(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public int getOrder()
	{
		return HIGHEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		Map<String, EntitySourceFactory> factories = event.getApplicationContext().getBeansOfType(
				EntitySourceFactory.class);

		for (EntitySourceFactory factory : factories.values())
		{
			dataService.registerFactory(factory);
		}
	}
}
