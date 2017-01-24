package org.molgenis.data.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.settings.DefaultSettingsEntityType.ATTR_ID;

/**
 * Populates the data service with default setting values for all {@link DefaultSettingsEntityType} beans.
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
		ctx.getBeansOfType(DefaultSettingsEntityType.class).values().forEach(this::initialize);
	}

	private void initialize(DefaultSettingsEntityType defaultSettingsEntityType)
	{
		Entity settingsEntity = defaultSettingsEntityType.getSettings();
		if (settingsEntity == null)
		{
			Entity defaultSettingsEntity = defaultSettingsEntityType.getDefaultSettings();
			defaultSettingsEntity.set(ATTR_ID, defaultSettingsEntityType.getName());
			dataService.add(defaultSettingsEntityType.getFullyQualifiedName(), defaultSettingsEntity);
		}
	}
}
