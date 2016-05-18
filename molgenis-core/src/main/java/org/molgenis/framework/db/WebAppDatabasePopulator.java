package org.molgenis.framework.db;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Populates database with initial data
 */
@Component
public class WebAppDatabasePopulator
{
	private final WebAppDatabasePopulatorService webAppDatabasePopulatorService;

	@Autowired
	public WebAppDatabasePopulator(WebAppDatabasePopulatorService webAppDatabasePopulatorService)
	{
		this.webAppDatabasePopulatorService = requireNonNull(webAppDatabasePopulatorService);
	}

	public void populateDatabase()
	{
		if (!webAppDatabasePopulatorService.isDatabasePopulated())
		{
			webAppDatabasePopulatorService.populateDatabase();
		}
	}
}