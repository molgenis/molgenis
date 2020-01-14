package org.molgenis.settings.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class SettingsControllerTest {
  private MockMvc mockMvc;

  @Mock private MenuReaderService menuReaderService;

  @Mock private AppSettings appSettings;

  @Mock private UserAccountService userAccountService;

  @BeforeEach
  public void before() {
    initMocks(this);

    when(menuReaderService.findMenuItemPath(SettingsController.ID)).thenReturn("/test/path");
    when(appSettings.getLanguageCode()).thenReturn("DDEEFF");
    User user = mock(User.class);
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.isSuperuser()).thenReturn(false);

    SettingsController settingsController = new SettingsController(menuReaderService);
    mockMvc = standaloneSetup(settingsController).build();
  }

  @Test
  public void testInit() throws Exception {
    mockMvc
        .perform(get(SettingsController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name(SettingsController.VIEW_TEMPLATE))
        .andExpect(model().attribute("baseUrl", "/test/path"));
  }
}
