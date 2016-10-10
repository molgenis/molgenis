package org.molgenis.bootstrap;

import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.script.ScriptRunnerRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;

/**
 * Bootstraps registries of {@link org.molgenis.data.RepositoryCollection repository collection},
 * {@link org.molgenis.data.importer.ImportService importers} and {@link org.molgenis.script.ScriptRunner script runners}.
 */
@Component
public class RegistryBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger(RegistryBootstrapper.class);

	private final RepositoryCollectionBootstrapper repoCollectionBootstrapper;
	private final SystemEntityTypeRegistrar systemEntityTypeRegistrar;
	private final EntityFactoryRegistrar entityFactoryRegistrar;
	private final ImportServiceRegistrar importServiceRegistrar;
	private final ScriptRunnerRegistrar scriptRunnerRegistrar;

	@Autowired
	public RegistryBootstrapper(RepositoryCollectionBootstrapper repoCollectionBootstrapper,
			SystemEntityTypeRegistrar systemEntityTypeRegistrar, EntityFactoryRegistrar entityFactoryRegistrar,
			ImportServiceRegistrar importServiceRegistrar, ScriptRunnerRegistrar scriptRunnerRegistrar)
	{
		this.repoCollectionBootstrapper = requireNonNull(repoCollectionBootstrapper);
		this.systemEntityTypeRegistrar = requireNonNull(systemEntityTypeRegistrar);
		this.entityFactoryRegistrar = requireNonNull(entityFactoryRegistrar);
		this.importServiceRegistrar = requireNonNull(importServiceRegistrar);
		this.scriptRunnerRegistrar = requireNonNull(scriptRunnerRegistrar);
	}

	public void bootstrap(ContextRefreshedEvent event)
	{
		LOG.trace("Registering repository collections ...");
		repoCollectionBootstrapper.bootstrap(event, POSTGRESQL);
		LOG.trace("Registered repository collections");

		LOG.trace("Registering system entity meta data ...");
		systemEntityTypeRegistrar.register(event);
		LOG.trace("Registered system entity meta data");

		LOG.trace("Registering entity factories ...");
		entityFactoryRegistrar.register(event);
		LOG.trace("Registered entity factories");

		LOG.trace("Registering importers ...");
		importServiceRegistrar.register(event);
		LOG.trace("Registered importers");

		LOG.trace("Registering script runners ...");
		scriptRunnerRegistrar.register(event);
		LOG.trace("Registered script runners");
	}
}
