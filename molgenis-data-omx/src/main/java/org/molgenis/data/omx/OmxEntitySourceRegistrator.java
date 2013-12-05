package org.molgenis.data.omx;

import org.molgenis.data.DataService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

public class OmxEntitySourceRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;

	public OmxEntitySourceRegistrator(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		dataService.registerEntitySource("omx://");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

}
