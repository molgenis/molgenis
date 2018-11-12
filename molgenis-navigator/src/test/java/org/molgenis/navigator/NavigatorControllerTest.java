package org.molgenis.navigator;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = {GsonConfig.class})
public class NavigatorControllerTest extends AbstractMockitoTestNGSpringContextTests {

  private MockMvc mockMvc;

  @Mock private MenuReaderService menuReaderService;

  @Mock private AppSettings appSettings;

  @Mock private UserAccountService userAccountService;

  @Mock private LocaleResolver localeResolver;

  @Mock private NavigatorService navigatorService;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @BeforeMethod
  public void before() {
    NavigatorController navigatorController =
        new NavigatorController(
            menuReaderService, appSettings, userAccountService, navigatorService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(navigatorController)
            .setMessageConverters(gsonHttpMessageConverter)
            .setLocaleResolver(localeResolver)
            .build();
  }

  /** Test that a get call to the plugin returns the correct view */
  @Test
  public void testInit() throws Exception {
    Menu menu = mock(Menu.class);
    when(menu.findMenuItemPath(NavigatorController.ID)).thenReturn("/test/path");
    when(menuReaderService.getMenu()).thenReturn(menu);
    when(appSettings.getLanguageCode()).thenReturn("de");
    User user = mock(User.class);
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.isSuperuser()).thenReturn(false);

    when(localeResolver.resolveLocale(any())).thenReturn(Locale.FRENCH);
    mockMvc
        .perform(get(NavigatorController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name("view-navigator"))
        .andExpect(model().attribute("baseUrl", "/test/path"))
        .andExpect(model().attribute("lng", "fr"))
        .andExpect(model().attribute("fallbackLng", "de"))
        .andExpect(model().attribute("isSuperUser", false));
  }

  @Test
  public void testDelete() throws Exception {
    String json = "{\"entityTypeIds\":[\"e0\",\"e1\"], \"packageIds\":[\"p0\",\"p1\"]}";
    mockMvc
        .perform(
            delete(NavigatorController.URI + "/delete").content(json).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());
    verify(navigatorService).deleteItems(asList("p0", "p1"), asList("e0", "e1"));
  }
}
