package org.molgenis.integrationtest.data.settings;

import org.molgenis.core.ui.menumanager.MenuManagerServiceImpl;
import org.molgenis.core.ui.settings.AppDbSettings;
import org.molgenis.data.settings.SettingsEntityType;
import org.molgenis.data.settings.SettingsPackage;
import org.molgenis.data.settings.SettingsPopulator;
import org.molgenis.settings.PropertyType;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ AppDbSettings.class, SettingsPackage.class, SettingsPopulator.class, SettingsEntityType.class,
		PropertyType.class, MenuManagerServiceImpl.class })
public class SettingsTestConfig
{
}
