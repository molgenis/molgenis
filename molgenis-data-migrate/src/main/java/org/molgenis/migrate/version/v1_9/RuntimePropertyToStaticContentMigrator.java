package org.molgenis.migrate.version.v1_9;

import static java.util.Objects.requireNonNull;
import static org.molgenis.system.core.RuntimeProperty.ENTITY_NAME;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.system.core.RuntimeProperty;
import org.molgenis.ui.settings.StaticContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@SuppressWarnings("deprecation")
@Component
public class RuntimePropertyToStaticContentMigrator implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(RuntimePropertyToStaticContentMigrator.class);

	private final DataService dataService;
	private final MolgenisPluginRegistry molgenisPluginRegistry;

	/**
	 * Whether or not this migrator is enabled
	 */
	private boolean enabled;

	@Autowired
	public RuntimePropertyToStaticContentMigrator(DataService dataService,
			MolgenisPluginRegistry molgenisPluginRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.molgenisPluginRegistry = requireNonNull(molgenisPluginRegistry);
	}

	private RuntimePropertyToStaticContentMigrator migrateSettings()
	{
		if (enabled)
		{
			LOG.info("Migrating RuntimeProperty instances to StaticContent instance ...");

			for (MolgenisPlugin molgenisPlugin : molgenisPluginRegistry)
			{
				String key = "app." + molgenisPlugin.getName();
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;

					StaticContent staticContent = new StaticContent(molgenisPlugin.getName(), dataService);
					staticContent.setContent(rtpValue);
					LOG.info("Creating StaticContent for RuntimeProperty [" + key + "]");
					dataService.add(StaticContent.ENTITY_NAME, staticContent);
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			LOG.info("Migrated RuntimeProperty instances to StaticContent instances");
		}
		return this;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		RunAsSystemProxy.runAsSystem(() -> migrateSettings());
	}

	private RuntimeProperty getProperty(String key)
	{
		return dataService.findOne(ENTITY_NAME, new QueryImpl().eq(RuntimeProperty.NAME, key), RuntimeProperty.class);
	}

	public void enableMigrator()
	{
		this.enabled = true;
	}
}
