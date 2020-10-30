package org.molgenis.integrationtest.config;

import com.google.gson.Gson;
import org.molgenis.core.ui.jobs.JobsController;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.MenuReaderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({JobsController.class})
public class ViewTestConfig {
  @Autowired private AppSettings appSettings;
  @Autowired private UserPermissionEvaluator userPermissionEvaluator;
  @Autowired private Gson gson;

  @Bean
  public MenuReaderService menuReaderService() {
    return new MenuReaderServiceImpl(appSettings, gson, userPermissionEvaluator);
  }
}
