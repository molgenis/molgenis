package org.molgenis.bootstrap;

import org.molgenis.bootstrap.i18n.I18nStringsPopulator;
import org.molgenis.data.settings.SettingsPopulator;
import org.molgenis.framework.db.WebAppDatabasePopulator;
import org.molgenis.script.ScriptTypePopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

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
	private final ScriptTypePopulator scriptTypePopulator;

	@Autowired
	public RepositoryPopulator(WebAppDatabasePopulator webAppDatabasePopulator, SettingsPopulator settingsPopulator,
			I18nStringsPopulator i18nStringsPopulator, ScriptTypePopulator scriptTypePopulator)
	{
		this.webAppDatabasePopulator = requireNonNull(webAppDatabasePopulator);
		this.settingsPopulator = requireNonNull(settingsPopulator);
		this.i18nStringsPopulator = requireNonNull(i18nStringsPopulator);
		this.scriptTypePopulator = requireNonNull(scriptTypePopulator);
	}

	public void populate(ContextRefreshedEvent event)
	{
		LOG.trace("Populating database ...");
		webAppDatabasePopulator.populateDatabase();
		LOG.trace("Populated database");

		LOG.trace("Populating settings entities ...");
		settingsPopulator.initialize(event);
		LOG.trace("Populated settings entities");

		LOG.trace("Populating database with I18N strings ...");
		i18nStringsPopulator.populate();
		LOG.trace("Populated database with I18N strings");

		LOG.trace("Populating script type entities ...");
		scriptTypePopulator.populate();
		LOG.trace("Populated script type entities");
	}
}
