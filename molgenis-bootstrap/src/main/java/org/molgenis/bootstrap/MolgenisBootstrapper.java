package org.molgenis.bootstrap;

import org.molgenis.bootstrap.populate.PermissionPopulator;
import org.molgenis.bootstrap.populate.RepositoryPopulator;
import org.molgenis.core.ui.style.BootstrapThemePopulator;
import org.molgenis.data.annotation.web.bootstrap.AnnotatorBootstrapper;
import org.molgenis.data.event.BootstrappingEventPublisher;
import org.molgenis.data.index.bootstrap.IndexBootstrapper;
import org.molgenis.data.migrate.bootstrap.MolgenisUpgradeBootstrapper;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistryPopulator;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistrar;
import org.molgenis.jobs.JobBootstrapper;
import org.molgenis.security.acl.DataSourceAclTablesPopulator;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

/**
 * Application bootstrapper
 *
 * TODO code duplication with org.molgenis.integrationtest.platform.PlatformBootstrapper
 */
@SuppressWarnings("unused")
@Component
class MolgenisBootstrapper implements ApplicationListener<ContextRefreshedEvent>, PriorityOrdered
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisBootstrapper.class);

	private final MolgenisUpgradeBootstrapper upgradeBootstrapper;
	private final DataSourceAclTablesPopulator dataSourceAclTablesPopulator;
	private final TransactionExceptionTranslatorRegistrar transactionExceptionTranslatorRegistrar;
	private final RegistryBootstrapper registryBootstrapper;
	private final SystemEntityTypeBootstrapper systemEntityTypeBootstrapper;
	private final RepositoryPopulator repositoryPopulator;
	private final PermissionPopulator systemPermissionPopulator;
	private final JobBootstrapper jobBootstrapper;
	private final AnnotatorBootstrapper annotatorBootstrapper;
	private final IndexBootstrapper indexBootstrapper;
	private final EntityTypeRegistryPopulator entityTypeRegistryPopulator;
	private final BootstrapThemePopulator bootstrapThemePopulator;
	private final BootstrappingEventPublisher bootstrappingEventPublisher;

	public MolgenisBootstrapper(MolgenisUpgradeBootstrapper upgradeBootstrapper,
			DataSourceAclTablesPopulator dataSourceAclTablesPopulator,
			TransactionExceptionTranslatorRegistrar transactionExceptionTranslatorRegistrar,
			RegistryBootstrapper registryBootstrapper, SystemEntityTypeBootstrapper systemEntityTypeBootstrapper,
			RepositoryPopulator repositoryPopulator, PermissionPopulator systemPermissionPopulator,
			JobBootstrapper jobBootstrapper, AnnotatorBootstrapper annotatorBootstrapper,
			IndexBootstrapper indexBootstrapper, EntityTypeRegistryPopulator entityTypeRegistryPopulator,
			BootstrapThemePopulator bootstrapThemePopulator,BootstrappingEventPublisher bootstrappingEventPublisher)
	{
		this.upgradeBootstrapper = requireNonNull(upgradeBootstrapper);
		this.dataSourceAclTablesPopulator = requireNonNull(dataSourceAclTablesPopulator);
		this.transactionExceptionTranslatorRegistrar = transactionExceptionTranslatorRegistrar;
		this.registryBootstrapper = requireNonNull(registryBootstrapper);
		this.systemEntityTypeBootstrapper = requireNonNull(systemEntityTypeBootstrapper);
		this.repositoryPopulator = requireNonNull(repositoryPopulator);
		this.systemPermissionPopulator = requireNonNull(systemPermissionPopulator);
		this.jobBootstrapper = requireNonNull(jobBootstrapper);
		this.annotatorBootstrapper = requireNonNull(annotatorBootstrapper);
		this.indexBootstrapper = requireNonNull(indexBootstrapper);
		this.entityTypeRegistryPopulator = requireNonNull(entityTypeRegistryPopulator);
		this.bootstrapThemePopulator = requireNonNull(bootstrapThemePopulator);
		this.bootstrappingEventPublisher = requireNonNull(bootstrappingEventPublisher);
	}

	@Transactional
	@RunAsSystem
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		LOG.info("Bootstrapping application ...");
		bootstrappingEventPublisher.publishBootstrappingStartedEvent();

		LOG.trace("Updating MOLGENIS ...");
		upgradeBootstrapper.bootstrap();
		LOG.debug("Updated MOLGENIS");

		LOG.trace("Populating data source with ACL tables ...");
		dataSourceAclTablesPopulator.populate();
		LOG.debug("Populated data source with ACL tables");

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
		boolean wasDatabasePopulated = repositoryPopulator.populate(event);
		LOG.debug("Populated repositories");

		if (!wasDatabasePopulated)
		{
			LOG.trace("Populating permissions ...");
			systemPermissionPopulator.populate(event.getApplicationContext());
			LOG.debug("Populated permissions");
		}

		LOG.trace("Bootstrapping jobs ...");
		jobBootstrapper.bootstrap();
		LOG.debug("Bootstrapped jobs");

		LOG.trace("Bootstrapping annotators ...");
		annotatorBootstrapper.bootstrap(event);
		LOG.debug("Bootstrapped annotators");

		LOG.trace("Bootstrapping index ...");
		indexBootstrapper.bootstrap();
		LOG.debug("Bootstrapped index");

		LOG.trace("Populating entity type registry ...");
		entityTypeRegistryPopulator.populate();
		LOG.debug("Populated entity type registry");

		LOG.trace("Populating bootstrap themes ...");
		bootstrapThemePopulator.populate();
		LOG.debug("Populated bootstrap themes");

		bootstrappingEventPublisher.publishBootstrappingFinishedEvent();
		LOG.info("Bootstrapping application completed");
	}

	@Override
	public int getOrder()
	{
		return PriorityOrdered.HIGHEST_PRECEDENCE; // bootstrap application before doing anything else
	}
}
