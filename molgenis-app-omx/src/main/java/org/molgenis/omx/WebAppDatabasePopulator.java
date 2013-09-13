package org.molgenis.omx;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class WebAppDatabasePopulator implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger logger = Logger.getLogger(WebAppDatabasePopulator.class);

	private final WebAppDatabasePopulatorService webAppDatabasePopulatorService;

	@Autowired
	public WebAppDatabasePopulator(WebAppDatabasePopulatorService webAppDatabasePopulatorService)
	{
		if (webAppDatabasePopulatorService == null) throw new IllegalArgumentException(
				"Web app database populator service is null");
		this.webAppDatabasePopulatorService = webAppDatabasePopulatorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		try
		{
			if (!webAppDatabasePopulatorService.isDatabasePopulated())
			{
				logger.info("initializing application database");
				webAppDatabasePopulatorService.populateDatabase();
				logger.info("initialized application database");
			}
		}
		catch (DatabaseException e)
		{
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
}