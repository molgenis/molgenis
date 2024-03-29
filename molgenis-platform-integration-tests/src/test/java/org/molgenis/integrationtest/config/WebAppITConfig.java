package org.molgenis.integrationtest.config;

import org.molgenis.core.ui.MolgenisWebAppConfig;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.web.converter.GsonWebConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({GsonWebConfig.class, ViewTestConfig.class, SettingsTestConfig.class})
public class WebAppITConfig extends MolgenisWebAppConfig {
  @Bean
  public ApplicationContextProvider applicationContextProvider() {
    return new ApplicationContextProvider();
  }
}
