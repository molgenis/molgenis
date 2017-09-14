package org.molgenis.data.platform.bootstrap;

import org.molgenis.data.i18n.SystemEntityTypeI18nInitializer;
import org.molgenis.data.meta.system.SystemEntityTypeInitializer;
import org.molgenis.data.meta.system.SystemEntityTypePersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class SystemEntityTypeBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger(SystemEntityTypeBootstrapper.class);

	private final SystemEntityTypeInitializer systemEntityTypeInitializer;
	private final SystemEntityTypeI18nInitializer systemEntityTypeI18nInitializer;
	private final SystemEntityTypePersister systemEntityTypePersister;

	SystemEntityTypeBootstrapper(SystemEntityTypeInitializer systemEntityTypeInitializer,
			SystemEntityTypeI18nInitializer systemEntityTypeI18nInitializer,
			SystemEntityTypePersister systemEntityTypePersister)
	{

		this.systemEntityTypeInitializer = requireNonNull(systemEntityTypeInitializer);
		this.systemEntityTypeI18nInitializer = systemEntityTypeI18nInitializer;
		this.systemEntityTypePersister = requireNonNull(systemEntityTypePersister);
	}

	public void bootstrap(ContextRefreshedEvent event)
	{
		LOG.trace("Initializing system entity meta data ...");
		systemEntityTypeInitializer.initialize(event);
		LOG.trace("Initialized system entity meta data");

		LOG.trace("Internationalizing system entity meta data ...");
		systemEntityTypeI18nInitializer.initialize(event);
		LOG.trace("Internationalized system entity meta data");

		LOG.trace("Persisting system entity meta data ...");
		systemEntityTypePersister.persist();
		LOG.trace("Persisted system entity meta data");
	}
}
