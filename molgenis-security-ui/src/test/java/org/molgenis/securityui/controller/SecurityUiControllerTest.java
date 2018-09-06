package org.molgenis.securityui.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.mockito.Mock;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Configuration
@EnableWebMvc
public class SecurityUiControllerTest {
  private static final String MENU_PATH_SECURITY_UI = "/menu/path/security-ui";
  public static final String DEFAULT_LANG = "en";
  private MockMvc mockMvc;

  @Mock private MenuReaderService menuReaderService;

  @Mock private AppSettings appSettings;

  @Mock private UserAccountService userAccountService;

  @BeforeMethod
  public void before() {
    initMocks(this);

    Menu menu = mock(Menu.class);
    when(menuReaderService.getMenu()).thenReturn(menu);
    when(appSettings.getLanguageCode()).thenReturn(DEFAULT_LANG);
    User user = mock(User.class);
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.isSuperuser()).thenReturn(false);
    when(menu.findMenuItemPath(SecurityUiController.ID)).thenReturn(MENU_PATH_SECURITY_UI);
    when(menuReaderService.getMenu()).thenReturn(menu);

    SecurityUiController securityUiController =
        new SecurityUiController(menuReaderService, appSettings, userAccountService);
    mockMvc = standaloneSetup(securityUiController).build();
  }

  @Test
  public void testInit() throws Exception {
    mockMvc
        .perform(get(SecurityUiController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name(SecurityUiController.VIEW_TEMPLATE))
        .andExpect(model().attribute("baseUrl", MENU_PATH_SECURITY_UI))
        .andExpect(model().attribute("lng", DEFAULT_LANG))
        .andExpect(model().attribute("fallbackLng", DEFAULT_LANG));
  }
}
