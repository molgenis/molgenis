package org.molgenis.data.jpa;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class JpaEntitySourceRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;

	@Autowired
	public JpaEntitySourceRegistrator(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public int getOrder()
	{
		return HIGHEST_PRECEDENCE + 1;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		dataService.registerEntitySource("jpa://");
	}
}
