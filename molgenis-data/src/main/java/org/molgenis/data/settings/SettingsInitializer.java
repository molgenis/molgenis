package org.molgenis.data.settings;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.settings.DefaultSettingsEntityMetaData.ATTR_ID;

import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Initializes all {@link DefaultSettingsEntityMetaData} beans.
 */
@Component
public class SettingsInitializer
{
	private final DataService dataService;

	@Autowired
	public SettingsInitializer(DataService dataService) {
		this.dataService = requireNonNull(dataService);
	}

	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, DefaultSettingsEntityMetaData> defaultSettingsEntityMetaDataMap = ctx
				.getBeansOfType(DefaultSettingsEntityMetaData.class);
		defaultSettingsEntityMetaDataMap.values().forEach(this::initialize);

	}

	private void initialize(DefaultSettingsEntityMetaData defaultSettingsEntityMetaData)
	{
		Entity settingsEntity = defaultSettingsEntityMetaData.getSettings();
		if (settingsEntity == null)
		{
			Entity defaultSettingsEntity = defaultSettingsEntityMetaData.getDefaultSettings();
			defaultSettingsEntity.set(ATTR_ID, defaultSettingsEntityMetaData.getSimpleName());
			dataService.add(defaultSettingsEntityMetaData.getName(), defaultSettingsEntity);
		}
	}
}
