package org.molgenis.migrate.version.v1_10;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.system.core.RuntimeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@SuppressWarnings("deprecation")
@Component
public class Step17RuntimePropertiesToGafListSettings extends MolgenisUpgrade
		implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(Step17RuntimePropertiesToGafListSettings.class);
	private final DataService dataService;

	/**
	 * Whether or not this migrator is enabled
	 */
	private boolean enabled;

	@Autowired
	public Step17RuntimePropertiesToGafListSettings(DataService dataService)
	{
		super(16, 17);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating metadata from version 16 to 17");
		enabled = true;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (enabled)
		{
			runAsSystem(this::migrateGafListSettings);
		}
	}

	private void migrateGafListSettings()
	{
		List<RuntimeProperty> runtimeProperties = new ArrayList<>();
		dataService.findAll(RuntimeProperty.ENTITY_NAME, RuntimeProperty.class).forEach(prop -> {
			if (prop.getName().startsWith("gafList"))
			{
				runtimeProperties.add(prop);

				Entity entity = new DefaultEntity(dataService.getEntityMetaData("GafListSettings"), dataService);
				entity.set("Name", prop.getName());
				entity.set("Value", prop.getValue());

				dataService.add("GafListSettings", entity);
			}
		});

		dataService.delete(RuntimeProperty.ENTITY_NAME, runtimeProperties);
	}
}
