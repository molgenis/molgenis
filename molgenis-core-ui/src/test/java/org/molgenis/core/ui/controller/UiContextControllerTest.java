package org.molgenis.core.ui.controller;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.core.ui.cookiewall.CookieWallService;
import org.molgenis.core.ui.style.MolgenisStyleException;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;

@ContextConfiguration(classes = {UiContextControllerTest.Config.class, GsonConfig.class})
class UiContextControllerTest extends AbstractMockitoSpringContextTests {

  private MockMvc mockMvc;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;
  @Autowired private Gson gson;
  @Mock private LocaleResolver localeResolver;

  @Mock private AppSettings appSettings;
  @Mock private CookieWallService cookieWallService;
  @Mock private MenuReaderService menuReaderService;
  @Mock private UserAccountService userAccountService;
  @Mock private User user;
  @Mock private ThemeFingerprintRegistry themeFingerprintRegistry;
  private SecurityContext previousContext;

  @Configuration
  static class Config {}

  @BeforeEach
  void beforeMethod() throws IOException, MolgenisStyleException {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    Authentication authentication =
        new TestingAuthenticationToken("henkie", "password", "USER", "LIFELINES_SHOPPER");
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);

    UiContextController uiContextController =
        new UiContextController(
            appSettings,
            cookieWallService,
            menuReaderService,
            userAccountService,
            "mock-version",
            "mock date-time",
            themeFingerprintRegistry);
    mockMvc =
        MockMvcBuilders.standaloneSetup(uiContextController)
            .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
            .setLocaleResolver(localeResolver)
            .build();

    when(appSettings.getBootstrapTheme()).thenReturn("selected-theme.css");
    when(themeFingerprintRegistry.getFingerprint("/css/bootstrap-4/selected-theme.css"))
        .thenReturn("fingerprint");
  }

  @AfterEach
  void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void testGetContextEmptyMenu() throws Exception {
    when(menuReaderService.getMenu()).thenReturn(Optional.empty());
    when(appSettings.getLogoNavBarHref()).thenReturn("http:://thisissomelogo/");
    when(appSettings.getLogoTopHref()).thenReturn("http:://thisisotherref/");
    when(appSettings.getLogoTopMaxHeight()).thenReturn(22);
    when(appSettings.getFooter()).thenReturn("<a class=\"foo\">message</a>");
    when(appSettings.getCssHref()).thenReturn("cssHref");
    when(cookieWallService.showCookieWall()).thenReturn(false);
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.getEmail()).thenReturn("henkie@example.org");
    when(user.getUsername()).thenReturn("henkie");

    mockMvc
        .perform(get("/app-ui-context"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.menu").doesNotExist());
  }

  @Test
  void testGetContext() throws Exception {

    File resource = new ClassPathResource("exampleMenu.json").getFile();
    String json = new String(Files.readAllBytes(resource.toPath()));
    Menu menu = gson.fromJson(json, Menu.class);

    when(menuReaderService.getMenu()).thenReturn(Optional.of(menu));
    when(appSettings.getLogoNavBarHref()).thenReturn("http:://thisissomelogo/");
    when(appSettings.getLogoTopHref()).thenReturn("http:://thisisotherref/");
    when(appSettings.getLogoTopMaxHeight()).thenReturn(22);
    when(appSettings.getFooter()).thenReturn("<a class=\"foo\">message</a>");
    when(appSettings.getCssHref()).thenReturn("cssHref");
    when(cookieWallService.showCookieWall()).thenReturn(false);
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.getEmail()).thenReturn("henkie@example.org");
    when(user.getUsername()).thenReturn("henkie");

    mockMvc
        .perform(get("/app-ui-context"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.navBarLogo", is(appSettings.getLogoNavBarHref())))
        .andExpect(jsonPath("$.logoTop", is(appSettings.getLogoTopHref())))
        .andExpect(jsonPath("$.logoTopMaxHeight", is(appSettings.getLogoTopMaxHeight())))
        .andExpect(jsonPath("$.loginHref", is(UiContextController.LOGIN_HREF)))
        .andExpect(jsonPath("$.helpLink.label", is("Help")))
        .andExpect(jsonPath("$.helpLink.href", is(UiContextController.HELP_HREF)))
        .andExpect(jsonPath("$.authenticated", is(true)))
        .andExpect(jsonPath("$.username", is("henkie")))
        .andExpect(jsonPath("$.email", is("henkie@example.org")))
        .andExpect(jsonPath("$.roles.length()", is(2)))
        .andExpect(jsonPath("$.showCookieWall", is(false)))
        .andExpect(jsonPath("$.menu.id", is("main")))
        .andExpect(jsonPath("$.menu.items.length()", is(6)))
        .andExpect(jsonPath("$.additionalMessage", is("<a class=\"foo\">message</a>")))
        .andExpect(jsonPath("$.version", is("mock-version")))
        .andExpect(jsonPath("$.buildDate", is("mock date-time")))
        .andExpect(jsonPath("$.cssHref", is("cssHref")))
        .andExpect(
            jsonPath("$.selectedTheme", is("/css/bootstrap-4/selected-theme.css?fingerprint")));
  }
}
