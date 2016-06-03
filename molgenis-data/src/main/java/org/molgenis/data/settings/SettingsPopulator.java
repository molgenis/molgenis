package org.molgenis.data.settings;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.settings.DefaultSettingsEntityMetaData.ATTR_ID;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Populates the data service with default setting values for all {@link DefaultSettingsEntityMetaData} beans.
 */
@Component
public class SettingsPopulator
{
	private final DataService dataService;

	@Autowired
	public SettingsPopulator(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		ctx.getBeansOfType(DefaultSettingsEntityMetaData.class).values().forEach(this::initialize);
	}

	private void initialize(DefaultSettingsEntityMetaData defaultSettingsEntityMeta)
	{
		Entity settingsEntity = defaultSettingsEntityMeta.getSettings();
		if (settingsEntity == null)
		{
			Entity defaultSettingsEntity = defaultSettingsEntityMeta.getDefaultSettings();
			defaultSettingsEntity.set(ATTR_ID, defaultSettingsEntityMeta.getSimpleName());
			dataService.add(defaultSettingsEntityMeta.getName(), defaultSettingsEntity);
		}
	}
}
