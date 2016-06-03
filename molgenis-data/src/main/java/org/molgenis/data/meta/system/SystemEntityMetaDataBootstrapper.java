package org.molgenis.data.meta.system;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SystemEntityMetaDataBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger(SystemEntityMetaDataBootstrapper.class);

	private final SystemEntityMetaDataInitializer systemEntityMetaDataInitializer;
	private final SystemEntityMetaDataPersister systemEntityMetaDataPersister;
	private final SystemEntityMetaDataRegistrar systemEntityMetaDataRegistrar;

	@Autowired
	SystemEntityMetaDataBootstrapper(SystemEntityMetaDataInitializer systemEntityMetaDataInitializer,
			SystemEntityMetaDataPersister systemEntityMetaDataPersister,
			SystemEntityMetaDataRegistrar systemEntityMetaDataRegistrar)
	{

		this.systemEntityMetaDataInitializer = requireNonNull(systemEntityMetaDataInitializer);
		this.systemEntityMetaDataPersister = requireNonNull(systemEntityMetaDataPersister);
		this.systemEntityMetaDataRegistrar = requireNonNull(systemEntityMetaDataRegistrar);
	}

	public void bootstrap(ContextRefreshedEvent event)
	{
		LOG.trace("Initializing system entity meta data ...");
		systemEntityMetaDataInitializer.initialize(event);
		LOG.trace("Initialized system entity meta data");

		LOG.trace("Registering system entity meta data ...");
		systemEntityMetaDataRegistrar.register(event);
		LOG.trace("Registered system entity meta data");

		LOG.trace("Persisting system entity meta data ...");
		systemEntityMetaDataPersister.persist(event);
		LOG.trace("Persisted system entity meta data");
	}
}
