package org.molgenis.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Populates the data service with default setting values for all {@link DefaultSettingsEntityType} beans.
 */
@Component
public class SettingsPopulator
{
	private final DataService dataService;

	public SettingsPopulator(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		ctx.getBeansOfType(DefaultSettingsEntityType.class).values().forEach(this::initialize);
	}

	private void initialize(DefaultSettingsEntityType defaultSettingsEntityType)
	{
		Entity settingsEntity = defaultSettingsEntityType.getSettings();
		if (settingsEntity == null)
		{
			Entity defaultSettingsEntity = defaultSettingsEntityType.getDefaultSettings();
			dataService.add(defaultSettingsEntityType.getId(), defaultSettingsEntity);
		}
	}
}
