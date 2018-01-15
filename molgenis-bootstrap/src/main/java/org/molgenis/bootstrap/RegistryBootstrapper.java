package org.molgenis.bootstrap;

import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.SystemRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistrar;
import org.molgenis.jobs.JobFactoryRegistrar;
import org.molgenis.script.core.ScriptRunner;
import org.molgenis.script.core.ScriptRunnerRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;

/**
 * Bootstraps registries of {@link org.molgenis.data.RepositoryCollection repository collection},
 * {@link org.molgenis.data.importer.ImportService importers} and {@link ScriptRunner script runners}.
 */
@Component
public class RegistryBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger(RegistryBootstrapper.class);

	private final RepositoryCollectionBootstrapper repoCollectionBootstrapper;
	private final SystemEntityTypeRegistrar systemEntityTypeRegistrar;
	private final SystemPackageRegistrar systemPackageRegistrar;
	private final EntityFactoryRegistrar entityFactoryRegistrar;
	private final SystemRepositoryDecoratorFactoryRegistrar systemRepositoryDecoratorFactoryRegistrar;
	private final ImportServiceRegistrar importServiceRegistrar;
	private final ScriptRunnerRegistrar scriptRunnerRegistrar;
	private final JobFactoryRegistrar jobFactoryRegistrar;

	public RegistryBootstrapper(RepositoryCollectionBootstrapper repoCollectionBootstrapper,
			SystemEntityTypeRegistrar systemEntityTypeRegistrar, SystemPackageRegistrar systemPackageRegistrar,
			EntityFactoryRegistrar entityFactoryRegistrar,
			SystemRepositoryDecoratorFactoryRegistrar systemRepositoryDecoratorFactoryRegistrar,
			ImportServiceRegistrar importServiceRegistrar, ScriptRunnerRegistrar scriptRunnerRegistrar,
			JobFactoryRegistrar jobFactoryRegistrar)
	{
		this.repoCollectionBootstrapper = requireNonNull(repoCollectionBootstrapper);
		this.systemEntityTypeRegistrar = requireNonNull(systemEntityTypeRegistrar);
		this.systemPackageRegistrar = requireNonNull(systemPackageRegistrar);
		this.entityFactoryRegistrar = requireNonNull(entityFactoryRegistrar);
		this.systemRepositoryDecoratorFactoryRegistrar = requireNonNull(systemRepositoryDecoratorFactoryRegistrar);
		this.importServiceRegistrar = requireNonNull(importServiceRegistrar);
		this.scriptRunnerRegistrar = requireNonNull(scriptRunnerRegistrar);
		this.jobFactoryRegistrar = requireNonNull(jobFactoryRegistrar);
	}

	public void bootstrap(ContextRefreshedEvent event)
	{
		LOG.trace("Registering repository collections ...");
		repoCollectionBootstrapper.bootstrap(event, POSTGRESQL);
		LOG.trace("Registered repository collections");

		LOG.trace("Registering system entity types ...");
		systemEntityTypeRegistrar.register(event);
		LOG.trace("Registered system entity types");

		LOG.trace("Registering system packages ...");
		systemPackageRegistrar.register(event);
		LOG.trace("Registered system packages");

		LOG.trace("Registering entity factories ...");
		entityFactoryRegistrar.register(event);
		LOG.trace("Registered entity factories");

		LOG.trace("Registering repository decorator factories ...");
		systemRepositoryDecoratorFactoryRegistrar.register(event);
		LOG.trace("Registered entity factories");

		LOG.trace("Registering importers ...");
		importServiceRegistrar.register(event);
		LOG.trace("Registered importers");

		LOG.trace("Registering script runners ...");
		scriptRunnerRegistrar.register(event);
		LOG.trace("Registered script runners");

		LOG.trace("Registering job factories ...");
		jobFactoryRegistrar.register(event);
		LOG.trace("Registered job factories");
	}
}
