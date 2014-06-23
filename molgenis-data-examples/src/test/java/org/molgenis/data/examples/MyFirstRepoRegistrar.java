package org.molgenis.data.examples;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * Automatically register MyFirstRepo on startup
 */
public class MyFirstRepoRegistrar implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private DataService dataService;

	@Autowired
	public MyFirstRepoRegistrar(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		dataService.addRepository(new MyFirstRepo());
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
