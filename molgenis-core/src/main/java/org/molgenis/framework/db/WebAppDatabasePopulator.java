package org.molgenis.framework.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

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