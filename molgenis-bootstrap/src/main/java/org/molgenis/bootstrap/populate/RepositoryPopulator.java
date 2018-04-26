package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.data.decorator.meta.DynamicDecoratorPopulator;
import org.molgenis.script.core.ScriptTypePopulator;
import org.molgenis.settings.SettingsPopulator;
import org.molgenis.web.bootstrap.PluginPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.UserMetaData.USER;

/**
 * Populates {@link org.molgenis.data.Repository repositories} with data during bootstrapping.
 */
@Component
public class RepositoryPopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryPopulator.class);

	private final DataService dataService;
	private final UsersGroupsPopulator usersGroupsAuthoritiesPopulator;
	private final SystemEntityPopulator systemEntityPopulator;
	private final PluginPopulator pluginPopulator;
	private final SettingsPopulator settingsPopulator;
	private final I18nPopulator i18nPopulator;
	private final ScriptTypePopulator scriptTypePopulator;
	private final GenomeBrowserAttributesPopulator genomeBrowserAttributesPopulator;
	private final DynamicDecoratorPopulator dynamicDecoratorPopulator;

	public RepositoryPopulator(DataService dataService, UsersGroupsPopulator usersGroupsAuthoritiesPopulator,
			SystemEntityPopulator systemEntityPopulator, PluginPopulator pluginPopulator,
			SettingsPopulator settingsPopulator, I18nPopulator i18nPopulator, ScriptTypePopulator scriptTypePopulator,
			GenomeBrowserAttributesPopulator genomeBrowserAttributesPopulator,
			DynamicDecoratorPopulator dynamicDecoratorPopulator)
	{
		this.dataService = requireNonNull(dataService);
		this.usersGroupsAuthoritiesPopulator = requireNonNull(usersGroupsAuthoritiesPopulator);
		this.systemEntityPopulator = requireNonNull(systemEntityPopulator);
		this.pluginPopulator = requireNonNull(pluginPopulator);
		this.settingsPopulator = requireNonNull(settingsPopulator);
		this.i18nPopulator = requireNonNull(i18nPopulator);
		this.scriptTypePopulator = requireNonNull(scriptTypePopulator);
		this.genomeBrowserAttributesPopulator = requireNonNull(genomeBrowserAttributesPopulator);
		this.dynamicDecoratorPopulator = requireNonNull(dynamicDecoratorPopulator);
	}

	/**
	 * Returns whether the database was populated previously.
	 */
	public boolean populate(ContextRefreshedEvent event)
	{
		boolean databasePopulated = isDatabasePopulated();
		if (!databasePopulated)
		{
			LOG.trace("Populating database with I18N strings ...");
			i18nPopulator.populateLanguages();
			LOG.trace("Populated database with I18N strings");
		}

		LOG.trace("Populating plugin entities ...");
		pluginPopulator.populate(event.getApplicationContext());
		LOG.trace("Populated plugin entities");

		LOG.trace("Populating settings entities ...");
		settingsPopulator.initialize(event);
		LOG.trace("Populated settings entities");

		LOG.trace("Populating default genome browser attributes ...");
		genomeBrowserAttributesPopulator.populate();
		LOG.trace("Populated default genome browser attributes");

		LOG.trace("Populating database with I18N strings ...");
		i18nPopulator.populateL10nStrings();
		LOG.trace("Populated database with I18N strings");

		LOG.trace("Populating script type entities ...");
		scriptTypePopulator.populate();
		LOG.trace("Populated script type entities");

		if (!databasePopulated)
		{
			LOG.trace("Populating database with users, groups and authorities ...");
			usersGroupsAuthoritiesPopulator.populate();
			LOG.trace("Populated database with users, groups and authorities");

			// populate repositories with application-specific entities
			LOG.trace("Populating database with application entities ...");
			systemEntityPopulator.populate(event);
			LOG.trace("Populated database with application entities");
		}

		LOG.trace("Populating dynamic decorator entities ...");
		dynamicDecoratorPopulator.populate();
		LOG.trace("Populated dynamic decorator entities");

		return databasePopulated;
	}

	private boolean isDatabasePopulated()
	{
		return dataService.count(USER) > 0;
	}
}
