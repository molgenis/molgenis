package org.molgenis.bootstrap;

import org.molgenis.data.annotation.web.bootstrap.AnnotatorBootstrapper;
import org.molgenis.data.idcard.IdCardBootstrapper;
import org.molgenis.data.jobs.JobBootstrapper;
import org.molgenis.data.platform.bootstrap.SystemEntityMetaDataBootstrapper;
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
@Component
class MolgenisBootstrapper implements ApplicationListener<ContextRefreshedEvent>, PriorityOrdered
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisBootstrapper.class);

	private final MolgenisUpgradeBootstrapper upgradeBootstrapper;
	private final RegistryBootstrapper registryBootstrapper;
	private final SystemEntityMetaDataBootstrapper systemEntityMetaDataBootstrapper;
	private final RepositoryPopulator repositoryPopulator;
	private final FileIngesterJobRegistrar fileIngesterJobRegistrar;
	private final JobBootstrapper jobBootstrapper;
	private final IdCardBootstrapper idCardBootstrapper;
	private final AnnotatorBootstrapper annotatorBootstrapper;

	@Autowired
	public MolgenisBootstrapper(MolgenisUpgradeBootstrapper upgradeBootstrapper,
			RegistryBootstrapper registryBootstrapper,
			SystemEntityMetaDataBootstrapper systemEntityMetaDataBootstrapper, RepositoryPopulator repositoryPopulator,
			FileIngesterJobRegistrar fileIngesterJobRegistrar, JobBootstrapper jobBootstrapper,
			IdCardBootstrapper idCardBootstrapper, AnnotatorBootstrapper annotatorBootstrapper)
	{
		this.upgradeBootstrapper = requireNonNull(upgradeBootstrapper);
		this.registryBootstrapper = requireNonNull(registryBootstrapper);
		this.systemEntityMetaDataBootstrapper = requireNonNull(systemEntityMetaDataBootstrapper);
		this.repositoryPopulator = requireNonNull(repositoryPopulator);
		this.fileIngesterJobRegistrar = requireNonNull(fileIngesterJobRegistrar);
		this.jobBootstrapper = requireNonNull(jobBootstrapper);
		this.idCardBootstrapper = requireNonNull(idCardBootstrapper);
		this.annotatorBootstrapper = requireNonNull(annotatorBootstrapper);
	}

	@Transactional
	@RunAsSystem
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// TODO index rebuilding

		LOG.info("Bootstrapping application ...");

		LOG.trace("Updating MOLGENIS ...");
		upgradeBootstrapper.bootstrap();
		LOG.debug("Updated MOLGENIS");

		LOG.trace("Bootstrapping registries ...");
		registryBootstrapper.bootstrap(event);
		LOG.debug("Bootstrapped registries");

		LOG.trace("Bootstrapping system entity meta data ...");
		systemEntityMetaDataBootstrapper.bootstrap(event);
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

		LOG.trace("Bootstrapping ID Card scheduler ...");
		idCardBootstrapper.bootstrap();
		LOG.debug("Bootstrapped ID Card scheduler");

		LOG.trace("Bootstrapping annotators ...");
		annotatorBootstrapper.bootstrap(event);
		LOG.debug("Bootstrapped annotators");

		LOG.info("Bootstrapping application completed");
	}

	@Override
	public int getOrder()
	{
		return PriorityOrdered.HIGHEST_PRECEDENCE; // bootstrap application before doing anything else
	}
}
