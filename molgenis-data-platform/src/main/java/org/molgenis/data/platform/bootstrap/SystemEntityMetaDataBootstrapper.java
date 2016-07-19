package org.molgenis.data.platform.bootstrap;

import org.molgenis.data.i18n.SystemEntityMetaDataI18nInitializer;
import org.molgenis.data.meta.system.SystemEntityMetaDataInitializer;
import org.molgenis.data.meta.system.SystemEntityMetaDataPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class SystemEntityMetaDataBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger(SystemEntityMetaDataBootstrapper.class);

	private final SystemEntityMetaDataInitializer systemEntityMetaInitializer;
	private final SystemEntityMetaDataI18nInitializer systemEntityMetaI18nInitializer;
	private final SystemEntityMetaDataPersister systemEntityMetaDataPersister;

	@Autowired
	SystemEntityMetaDataBootstrapper(SystemEntityMetaDataInitializer systemEntityMetaInitializer,
			SystemEntityMetaDataI18nInitializer systemEntityMetaI18nInitializer,
			SystemEntityMetaDataPersister systemEntityMetaDataPersister)
	{

		this.systemEntityMetaInitializer = requireNonNull(systemEntityMetaInitializer);
		this.systemEntityMetaI18nInitializer = systemEntityMetaI18nInitializer;
		this.systemEntityMetaDataPersister = requireNonNull(systemEntityMetaDataPersister);
	}

	public void bootstrap(ContextRefreshedEvent event)
	{
		LOG.trace("Initializing system entity meta data ...");
		systemEntityMetaInitializer.initialize(event);
		LOG.trace("Initialized system entity meta data");

		LOG.trace("Internationalizing system entity meta data ...");
		systemEntityMetaI18nInitializer.initialize(event);
		LOG.trace("Internationalized system entity meta data");

		LOG.trace("Persisting system entity meta data ...");
		systemEntityMetaDataPersister.persist(event);
		LOG.trace("Persisted system entity meta data");
	}
}
