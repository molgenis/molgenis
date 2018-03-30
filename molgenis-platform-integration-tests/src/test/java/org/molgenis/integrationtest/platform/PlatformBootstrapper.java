package org.molgenis.integrationtest.platform;

import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.SystemRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.decorator.meta.DynamicDecoratorPopulator;
import org.molgenis.data.event.BootstrappingEventPublisher;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistrar;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistryPopulator;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.jobs.JobFactoryRegistrar;
import org.molgenis.security.acl.DataSourceAclTablesPopulator;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;

/**
 * TODO code duplication with org.molgenis.bootstrap.MolgenisBootstrapper
 */
@Component
public class PlatformBootstrapper
{
	private final static Logger LOG = LoggerFactory.getLogger(PlatformITConfig.class);

	private final DataSourceAclTablesPopulator dataSourceAclTablesPopulator;
	private final RepositoryCollectionBootstrapper repoCollectionBootstrapper;
	private final SystemEntityTypeRegistrar systemEntityTypeRegistrar;
	private final SystemPackageRegistrar systemPackageRegistrar;
	private final EntityFactoryRegistrar entityFactoryRegistrar;
	private final SystemEntityTypeBootstrapper systemEntityTypeBootstrapper;
	private final SystemRepositoryDecoratorFactoryRegistrar systemRepositoryDecoratorFactoryRegistrar;
	private final JobFactoryRegistrar jobFactoryRegistrar;
	private final DynamicRepositoryDecoratorFactoryRegistrar dynamicRepositoryDecoratorFactoryRegistrar;
	private final BootstrappingEventPublisher bootstrappingEventPublisher;
	private final TransactionManager transactionManager;

	PlatformBootstrapper(DataSourceAclTablesPopulator dataSourceAclTablesPopulator,
			RepositoryCollectionBootstrapper repoCollectionBootstrapper,
			SystemEntityTypeRegistrar systemEntityTypeRegistrar, SystemPackageRegistrar systemPackageRegistrar,
			EntityFactoryRegistrar entityFactoryRegistrar, SystemEntityTypeBootstrapper systemEntityTypeBootstrapper,
			SystemRepositoryDecoratorFactoryRegistrar systemRepositoryDecoratorFactoryRegistrar,
			JobFactoryRegistrar jobFactoryRegistrar,
			DynamicRepositoryDecoratorFactoryRegistrar dynamicRepositoryDecoratorFactoryRegistrar,
			BootstrappingEventPublisher bootstrappingEventPublisher, TransactionManager transactionManager)
	{
		this.dataSourceAclTablesPopulator = dataSourceAclTablesPopulator;
		this.repoCollectionBootstrapper = repoCollectionBootstrapper;
		this.systemEntityTypeRegistrar = systemEntityTypeRegistrar;
		this.systemPackageRegistrar = systemPackageRegistrar;
		this.entityFactoryRegistrar = entityFactoryRegistrar;
		this.systemEntityTypeBootstrapper = systemEntityTypeBootstrapper;
		this.systemRepositoryDecoratorFactoryRegistrar = systemRepositoryDecoratorFactoryRegistrar;
		this.jobFactoryRegistrar = jobFactoryRegistrar;
		this.dynamicRepositoryDecoratorFactoryRegistrar = dynamicRepositoryDecoratorFactoryRegistrar;
		this.bootstrappingEventPublisher = bootstrappingEventPublisher;
		this.transactionManager = transactionManager;
	}

	public void bootstrap(ContextRefreshedEvent event)
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate();
		transactionTemplate.setTransactionManager(transactionManager);
		transactionTemplate.execute((action) ->
		{
			try
			{
				RunAsSystemAspect.runAsSystem(() ->
				{
					LOG.info("Bootstrapping registries ...");
					bootstrappingEventPublisher.publishBootstrappingStartedEvent();

					LOG.trace("Populating data source with ACL tables ...");
					dataSourceAclTablesPopulator.populate();
					LOG.debug("Populated data source with ACL tables");

					LOG.trace("Registering repository collections ...");
					repoCollectionBootstrapper.bootstrap(event, POSTGRESQL);
					LOG.trace("Registered repository collections");

					LOG.trace("Registering system entity meta data ...");
					systemEntityTypeRegistrar.register(event);
					LOG.trace("Registered system entity meta data");

					LOG.trace("Registering system packages ...");
					systemPackageRegistrar.register(event);
					LOG.trace("Registered system packages");

					LOG.trace("Registering entity factories ...");
					entityFactoryRegistrar.register(event);
					LOG.trace("Registered entity factories");

					LOG.trace("Registering entity factories ...");
					systemRepositoryDecoratorFactoryRegistrar.register(event);
					LOG.trace("Registered entity factories");
					LOG.debug("Bootstrapped registries");

					LOG.trace("Registering dynamic decorator factories ...");
					dynamicRepositoryDecoratorFactoryRegistrar.register(event.getApplicationContext());
					LOG.trace("Registered dynamic repository decorator factories");

					LOG.trace("Bootstrapping system entity types ...");
					systemEntityTypeBootstrapper.bootstrap(event);
					LOG.debug("Bootstrapped system entity types");

					LOG.trace("Registering job factories ...");
					jobFactoryRegistrar.register(event);
					LOG.trace("Registered job factories");

					event.getApplicationContext().getBean(EntityTypeRegistryPopulator.class).populate();
					event.getApplicationContext().getBean(DynamicDecoratorPopulator.class).populate();

					bootstrappingEventPublisher.publishBootstrappingFinishedEvent();
				});
			}
			catch (Exception unexpected)
			{
				LOG.error("Error bootstrapping tests!", unexpected);
				throw new RuntimeException(unexpected);
			}
			return (Void) null;
		});
	}
}
