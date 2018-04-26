package org.molgenis.integrationtest.config;

import org.molgenis.core.ui.menumanager.MenuManagerServiceImpl;
import org.molgenis.core.ui.settings.AppDbSettings;
import org.molgenis.settings.PropertyType;
import org.molgenis.settings.SettingsEntityType;
import org.molgenis.settings.SettingsPackage;
import org.molgenis.settings.SettingsPopulator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ AppDbSettings.class, SettingsPackage.class, SettingsPopulator.class, SettingsEntityType.class,
		PropertyType.class, MenuManagerServiceImpl.class })
public class SettingsTestConfig
{
}
