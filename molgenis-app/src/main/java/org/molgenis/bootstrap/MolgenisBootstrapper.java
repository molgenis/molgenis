package org.molgenis.bootstrap;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;

import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.idcard.IdCardBootstrapper;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.jobs.JobBootstrapper;
import org.molgenis.data.meta.system.SystemEntityMetaDataBootstrapper;
import org.molgenis.file.ingest.meta.FileIngesterJobRegistrar;
import org.molgenis.script.ScriptRunnerRegistrar;
import org.molgenis.security.core.runas.RunAsSystem;
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

	private final RepositoryCollectionBootstrapper repoCollectionBootstrapper;
	private final SystemEntityMetaDataBootstrapper systemEntityMetaDataBootstrapper;
	private final ImportServiceRegistrar importServiceRegistrar;
	private final ScriptRunnerRegistrar scriptRunnerRegistrar;
	private final RepositoryPopulator repositoryPopulator;
	private final FileIngesterJobRegistrar fileIngesterJobRegistrar;
	private final JobBootstrapper jobBootstrapper;
	private final IdCardBootstrapper idCardBootstrapper;

	@Autowired
	public MolgenisBootstrapper(RepositoryCollectionBootstrapper repoCollectionBootstrapper,
			SystemEntityMetaDataBootstrapper systemEntityMetaDataBootstrapper,
			ImportServiceRegistrar importServiceRegistrar, ScriptRunnerRegistrar scriptRunnerRegistrar,
			RepositoryPopulator repositoryPopulator, FileIngesterJobRegistrar fileIngesterJobRegistrar,
			JobBootstrapper jobBootstrapper, IdCardBootstrapper idCardBootstrapper)
	{
		this.repoCollectionBootstrapper = requireNonNull(repoCollectionBootstrapper);
		this.systemEntityMetaDataBootstrapper = requireNonNull(systemEntityMetaDataBootstrapper);
		this.importServiceRegistrar = requireNonNull(importServiceRegistrar);
		this.scriptRunnerRegistrar = requireNonNull(scriptRunnerRegistrar);
		this.repositoryPopulator = requireNonNull(repositoryPopulator);
		this.fileIngesterJobRegistrar = requireNonNull(fileIngesterJobRegistrar);
		this.jobBootstrapper = requireNonNull(jobBootstrapper);
		this.idCardBootstrapper = requireNonNull(idCardBootstrapper);
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
		repoCollectionBootstrapper.bootstrap(event, POSTGRESQL);
		LOG.debug("Registered repository collections");

		LOG.trace("Bootstrapping system entity meta data ...");
		systemEntityMetaDataBootstrapper.bootstrap(event);
		LOG.debug("Bootstrapped system entity meta data");

		LOG.trace("Registering importers ...");
		importServiceRegistrar.register(event);
		LOG.debug("Registered importers");

		LOG.trace("Registering script runners ...");
		scriptRunnerRegistrar.register(event);
		LOG.debug("Registered script runners");

		LOG.trace("Populating repositories ...");
		repositoryPopulator.populate(event);
		LOG.debug("Populated repositories");

		LOG.trace("Bootstrapping jobs ...");
		jobBootstrapper.bootstrap();
		LOG.debug("Bootstrapped jobs");

		LOG.trace("Scheduling file ingest jobs ...");
		fileIngesterJobRegistrar.scheduleJobs();
		LOG.debug("Scheduled file ingest jobs");

		LOG.trace("Bootstrapping ID Card scheduler ...");
		idCardBootstrapper.bootstrap();
		LOG.debug("Bootstrapped ID Card scheduler");

		LOG.info("Bootstrapping application completed");
	}

	@Override
	public int getOrder()
	{
		return PriorityOrdered.HIGHEST_PRECEDENCE; // bootstrap application before doing anything else
	}
}
