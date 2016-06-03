package org.molgenis.bootstrap;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.settings.SettingsPopulator;
import org.molgenis.framework.db.WebAppDatabasePopulator;
import org.molgenis.ui.I18nStringsPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Populates {@link org.molgenis.data.Repository repositories} with data during bootstrapping.
 */
@Component
public class RepositoryPopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryPopulator.class);

	private final WebAppDatabasePopulator webAppDatabasePopulator;
	private final SettingsPopulator settingsPopulator;
	private final I18nStringsPopulator i18nStringsPopulator;

	@Autowired
	public RepositoryPopulator(WebAppDatabasePopulator webAppDatabasePopulator, SettingsPopulator settingsPopulator,
			I18nStringsPopulator i18nStringsPopulator)
	{
		this.webAppDatabasePopulator = requireNonNull(webAppDatabasePopulator);
		this.settingsPopulator = requireNonNull(settingsPopulator);
		this.i18nStringsPopulator = requireNonNull(i18nStringsPopulator);
	}

	public void populate(ContextRefreshedEvent event)
	{
		LOG.trace("Populating database ...");
		webAppDatabasePopulator.populateDatabase();
		LOG.trace("Populated database");

		LOG.trace("Initializing settings entities ...");
		settingsPopulator.initialize(event);
		LOG.trace("Initialized settings entities");

		LOG.trace("Populating database with I18N strings ...");
		i18nStringsPopulator.populate();
		LOG.trace("Populated database with I18N strings");
	}
}
