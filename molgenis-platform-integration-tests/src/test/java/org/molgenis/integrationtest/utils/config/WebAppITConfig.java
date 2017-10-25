package org.molgenis.integrationtest.utils.config;

import org.molgenis.integrationtest.data.settings.SettingsTestConfig;
import org.molgenis.integrationtest.ui.UiTestConfig;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.molgenis.util.GsonConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ GsonConfig.class, UiTestConfig.class, SettingsTestConfig.class })
public class WebAppITConfig extends MolgenisWebAppConfig
{
}
