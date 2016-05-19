package org.molgenis.bootstrap;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.RepositoryCollectionRegistrar;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.system.SystemEntityMetaDataInitializer;
import org.molgenis.data.meta.system.SystemEntityMetaDataPersister;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistrar;
import org.molgenis.data.postgresql.PostgreSqlRepositoryCollection;
import org.molgenis.data.settings.SettingsInitializer;
import org.molgenis.file.ingest.meta.FileIngesterJobRegistrar;
import org.molgenis.framework.db.WebAppDatabasePopulator;
import org.molgenis.script.ScriptRunnerRegistrar;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.I18nStringsPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application bootstrapper
 */
@Component
class MolgenisBootstrapper implements ApplicationListener<ContextRefreshedEvent>, PriorityOrdered
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisBootstrapper.class);

	private final SystemEntityMetaDataInitializer systemEntityMetaDataInitializer;
	private final SystemEntityMetaDataRegistrar systemEntityMetaDataRegistrar;
	private final SystemEntityMetaDataPersister systemEntityMetaDataPersister;
	private final SettingsInitializer settingsInitializer;
	private final I18nStringsPopulator i18nStringsPopulator;
	private final ScriptRunnerRegistrar scriptRunnerRegistrar;
	private final FileIngesterJobRegistrar fileIngesterJobRegistrar;
	private final ImportServiceRegistrar importServiceRegistrar;
	private final WebAppDatabasePopulator webAppDatabasePopulator;
	private final MetaDataService metaDataService;
	private final PostgreSqlRepositoryCollection postgreSqlRepositoryCollection;
	private final RepositoryCollectionRegistrar repositoryCollectionRegistrar;

	@Autowired
	public MolgenisBootstrapper(SystemEntityMetaDataInitializer systemEntityMetaDataInitializer,
			SystemEntityMetaDataRegistrar systemEntityMetaDataRegistrar,
			SystemEntityMetaDataPersister systemEntityMetaDataPersister, SettingsInitializer settingsInitializer,
			I18nStringsPopulator i18nStringsPopulator, ScriptRunnerRegistrar scriptRunnerRegistrar,
			FileIngesterJobRegistrar fileIngesterJobRegistrar, ImportServiceRegistrar importServiceRegistrar,
			WebAppDatabasePopulator webAppDatabasePopulator, MetaDataService metaDataService,
			PostgreSqlRepositoryCollection postgreSqlRepositoryCollection,
			RepositoryCollectionRegistrar repositoryCollectionRegistrar)
	{

		this.systemEntityMetaDataInitializer = requireNonNull(systemEntityMetaDataInitializer);
		this.systemEntityMetaDataRegistrar = requireNonNull(systemEntityMetaDataRegistrar);
		this.systemEntityMetaDataPersister = requireNonNull(systemEntityMetaDataPersister);
		this.settingsInitializer = requireNonNull(settingsInitializer);
		this.i18nStringsPopulator = requireNonNull(i18nStringsPopulator);
		this.scriptRunnerRegistrar = requireNonNull(scriptRunnerRegistrar);
		this.fileIngesterJobRegistrar = requireNonNull(fileIngesterJobRegistrar);
		this.importServiceRegistrar = requireNonNull(importServiceRegistrar);
		this.webAppDatabasePopulator = requireNonNull(webAppDatabasePopulator);
		this.metaDataService = metaDataService;
		this.postgreSqlRepositoryCollection = postgreSqlRepositoryCollection;
		this.repositoryCollectionRegistrar = repositoryCollectionRegistrar;
	}

	@Transactional
	@RunAsSystem
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// TODO migration
		// TODO index rebuilding

		LOG.info("Bootstrapping application ...");

		LOG.trace("Registering repository collections ...");
		repositoryCollectionRegistrar.register(event);
		LOG.debug("Registered repository collections");

		LOG.trace("Registering default repository collection ...");
		metaDataService.setDefaultBackend(postgreSqlRepositoryCollection);
		LOG.debug("Registered default repository collection");

		LOG.trace("Initializing system entity meta data ...");
		systemEntityMetaDataInitializer.initialize(event);
		LOG.debug("Initialized system entity meta data");

		LOG.trace("Registering system entity meta data ...");
		systemEntityMetaDataRegistrar.register(event);
		LOG.debug("Registered system entity meta data");

		LOG.trace("Persisting system entity meta data ...");
		systemEntityMetaDataPersister.persist();
		LOG.debug("Persisted system entity meta data");

		LOG.trace("Populating database ...");
		webAppDatabasePopulator.populateDatabase();
		LOG.debug("Populated database");

		//		LOG.trace("Initializing settings entities ...");
		//		settingsInitializer.initialize(event);
		//		LOG.debug("Initialized settings entities");

		LOG.trace("Scheduling importers ...");
		importServiceRegistrar.register(event);
		LOG.debug("Scheduled importers");

		//		LOG.trace("Populating database with I18N strings ...");
		//		i18nStringsPopulator.populate();
		//		LOG.debug("Populated database with I18N strings");

		//		LOG.trace("Registering script runners ...");
		//		scriptRunnerRegistrar.register(event);
		//		LOG.debug("Registered script runners");

		//		LOG.trace("Scheduling file ingest jobs ...");
		//		fileIngesterJobRegistrar.scheduleJobs();
		//		LOG.debug("Scheduled file ingest jobs");

		LOG.info("Bootstrapping application completed");
	}

	@Override
	public int getOrder()
	{
		return PriorityOrdered.HIGHEST_PRECEDENCE; // bootstrap application before doing anything else
	}
}
