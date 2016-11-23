package org.molgenis.bootstrap;

import org.molgenis.data.annotation.web.bootstrap.AnnotatorBootstrapper;
import org.molgenis.data.elasticsearch.bootstrap.IndexBootstrapper;
import org.molgenis.data.jobs.JobBootstrapper;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistrar;
import org.molgenis.file.ingest.FileIngesterJobRegistrar;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

/**
 * Application bootstrapper
 */
@SuppressWarnings("unused")
@Component
class MolgenisBootstrapper implements ApplicationListener<ContextRefreshedEvent>, PriorityOrdered
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisBootstrapper.class);

	private final MolgenisUpgradeBootstrapper upgradeBootstrapper;
	private final TransactionExceptionTranslatorRegistrar transactionExceptionTranslatorRegistrar;
	private final RegistryBootstrapper registryBootstrapper;
	private final SystemEntityTypeBootstrapper systemEntityTypeBootstrapper;
	private final RepositoryPopulator repositoryPopulator;
	private final FileIngesterJobRegistrar fileIngesterJobRegistrar;
	private final JobBootstrapper jobBootstrapper;
	private final AnnotatorBootstrapper annotatorBootstrapper;
	private final IndexBootstrapper indexBootstrapper;

	@Autowired
	public MolgenisBootstrapper(MolgenisUpgradeBootstrapper upgradeBootstrapper,
			TransactionExceptionTranslatorRegistrar transactionExceptionTranslatorRegistrar,
			RegistryBootstrapper registryBootstrapper,
			SystemEntityTypeBootstrapper systemEntityTypeBootstrapper, RepositoryPopulator repositoryPopulator,
			FileIngesterJobRegistrar fileIngesterJobRegistrar, JobBootstrapper jobBootstrapper,
			AnnotatorBootstrapper annotatorBootstrapper,
			IndexBootstrapper indexBootstrapper)
	{
		this.upgradeBootstrapper = requireNonNull(upgradeBootstrapper);
		this.transactionExceptionTranslatorRegistrar = transactionExceptionTranslatorRegistrar;
		this.registryBootstrapper = requireNonNull(registryBootstrapper);
		this.systemEntityTypeBootstrapper = requireNonNull(systemEntityTypeBootstrapper);
		this.repositoryPopulator = requireNonNull(repositoryPopulator);
		this.fileIngesterJobRegistrar = requireNonNull(fileIngesterJobRegistrar);
		this.jobBootstrapper = requireNonNull(jobBootstrapper);
		this.annotatorBootstrapper = requireNonNull(annotatorBootstrapper);
		this.indexBootstrapper = requireNonNull(indexBootstrapper);
	}

	@Transactional
	@RunAsSystem
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{

		LOG.info("Bootstrapping application ...");

		LOG.trace("Updating MOLGENIS ...");
		upgradeBootstrapper.bootstrap();
		LOG.debug("Updated MOLGENIS");

		LOG.trace("Bootstrapping transaction exception translators ...");
		transactionExceptionTranslatorRegistrar.register(event.getApplicationContext());
		LOG.debug("Bootstrapped transaction exception translators");

		LOG.trace("Bootstrapping registries ...");
		registryBootstrapper.bootstrap(event);
		LOG.debug("Bootstrapped registries");

		LOG.trace("Bootstrapping system entity meta data ...");
		systemEntityTypeBootstrapper.bootstrap(event);
		LOG.debug("Bootstrapped system entity meta data");

		LOG.trace("Populating repositories ...");
		repositoryPopulator.populate(event);
		LOG.debug("Populated repositories");

		LOG.trace("Bootstrapping jobs ...");
		jobBootstrapper.bootstrap();
		LOG.debug("Bootstrapped jobs");

		LOG.trace("Scheduling file ingest jobs ...");
		fileIngesterJobRegistrar.scheduleJobs();
		LOG.debug("Scheduled file ingest jobs");

		LOG.trace("Bootstrapping annotators ...");
		annotatorBootstrapper.bootstrap(event);
		LOG.debug("Bootstrapped annotators");

		LOG.trace("Bootstrapping index ...");
		indexBootstrapper.bootstrap();
		LOG.debug("Bootstrapped index");

		LOG.info("Bootstrapping application completed");
	}

	@Override
	public int getOrder()
	{
		return PriorityOrdered.HIGHEST_PRECEDENCE; // bootstrap application before doing anything else
	}
}
