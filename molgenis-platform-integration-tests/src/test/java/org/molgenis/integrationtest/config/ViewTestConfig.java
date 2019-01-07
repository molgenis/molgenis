package org.molgenis.integrationtest.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.io.IOException;
import org.molgenis.core.ui.jobs.JobsController;
import org.molgenis.core.ui.style.MolgenisStyleException;
import org.molgenis.core.ui.style.StyleService;
import org.molgenis.core.ui.style.StyleSheetFactory;
import org.molgenis.core.ui.style.StyleSheetMetadata;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.MenuReaderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({JobsController.class, StyleSheetFactory.class, StyleSheetMetadata.class})
public class ViewTestConfig {
  @Autowired private AppSettings appSettings;
  @Autowired private UserPermissionEvaluator userPermissionEvaluator;
  @Autowired private Gson gson;

  @Bean
  public MenuReaderService menuReaderService() {
    return new MenuReaderServiceImpl(appSettings, gson, userPermissionEvaluator);
  }

  @Bean
  public StyleService styleService() {
    return mock(StyleService.class);
  }

  @Bean
  public ThemeFingerprintRegistry themeFingerprintRegistry()
      throws IOException, MolgenisStyleException {
    ThemeFingerprintRegistry themeFingerprintRegistry = mock(ThemeFingerprintRegistry.class);
    when(themeFingerprintRegistry.getFingerprint(anyString())).thenReturn("");
    return themeFingerprintRegistry;
  }
}
