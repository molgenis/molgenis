package org.molgenis.framework.db;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

public class WebAppDatabasePopulator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger logger = Logger.getLogger(WebAppDatabasePopulator.class);

	private final WebAppDatabasePopulatorService webAppDatabasePopulatorService;

	public WebAppDatabasePopulator(WebAppDatabasePopulatorService webAppDatabasePopulatorService)
	{
		if (webAppDatabasePopulatorService == null) throw new IllegalArgumentException(
				"Web app database populator service is null");
		this.webAppDatabasePopulatorService = webAppDatabasePopulatorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (!webAppDatabasePopulatorService.isDatabasePopulated())
		{
			logger.info("initializing application database");
			webAppDatabasePopulatorService.populateDatabase();
			logger.info("initialized application database");
		}
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}
}