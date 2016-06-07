package org.molgenis.bootstrap;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;

import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistrar;
import org.molgenis.script.ScriptRunnerRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Bootstraps registries of {@link org.molgenis.data.RepositoryCollection repository collection},
 * {@link org.molgenis.data.importer.ImportService importers} and {@link org.molgenis.script.ScriptRunner script runners}.
 */
@Component
public class RegistryBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger(RegistryBootstrapper.class);

	private final RepositoryCollectionBootstrapper repoCollectionBootstrapper;
	private final SystemEntityMetaDataRegistrar systemEntityMetaRegistrar;
	private final ImportServiceRegistrar importServiceRegistrar;
	private final ScriptRunnerRegistrar scriptRunnerRegistrar;

	@Autowired
	public RegistryBootstrapper(RepositoryCollectionBootstrapper repoCollectionBootstrapper,
			SystemEntityMetaDataRegistrar systemEntityMetaRegistrar, ImportServiceRegistrar importServiceRegistrar,
			ScriptRunnerRegistrar scriptRunnerRegistrar)
	{
		this.repoCollectionBootstrapper = requireNonNull(repoCollectionBootstrapper);
		this.systemEntityMetaRegistrar = systemEntityMetaRegistrar;
		this.importServiceRegistrar = requireNonNull(importServiceRegistrar);
		this.scriptRunnerRegistrar = requireNonNull(scriptRunnerRegistrar);
	}

	public void bootstrap(ContextRefreshedEvent event)
	{
		LOG.trace("Registering repository collections ...");
		repoCollectionBootstrapper.bootstrap(event, POSTGRESQL);
		LOG.trace("Registered repository collections");

		LOG.trace("Registering system entity meta data ...");
		systemEntityMetaRegistrar.register(event);
		LOG.trace("Registered system entity meta data");

		LOG.trace("Registering importers ...");
		importServiceRegistrar.register(event);
		LOG.trace("Registered importers");

		LOG.trace("Registering script runners ...");
		scriptRunnerRegistrar.register(event);
		LOG.trace("Registered script runners");
	}
}
