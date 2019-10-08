package org.molgenis.core.ui.controller;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.core.ui.cookiewall.CookieWallService;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
  private SecurityContext previousContext;

  @Configuration
  static class Config {}

  @BeforeEach
  void beforeMethod() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    Authentication authentication = mock(AnonymousAuthenticationToken.class);
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);

    UiContextController uiContextController =
        new UiContextController(appSettings, cookieWallService, "mock-version", "mock date-time");
    mockMvc =
        MockMvcBuilders.standaloneSetup(uiContextController)
            .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
            .setLocaleResolver(localeResolver)
            .build();
  }

  @AfterEach
  void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void testGetContext() throws Exception {

    File resource = new ClassPathResource("exampleMenu.json").getFile();
    String menu = new String(Files.readAllBytes(resource.toPath()));

    when(appSettings.getLogoNavBarHref()).thenReturn("http:://thisissomelogo/");
    when(appSettings.getLogoTopHref()).thenReturn("http:://thisisotherref/");
    when(appSettings.getLogoTopMaxHeight()).thenReturn(22);
    when(appSettings.getMenu()).thenReturn(menu);
    when(appSettings.getFooter()).thenReturn("<a class=\"foo\">message</a>");
    when(appSettings.getCssHref()).thenReturn("cssHref");
    when(cookieWallService.showCookieWall()).thenReturn(false);

    mockMvc
        .perform(get("/app-ui-context"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.navBarLogo", is(appSettings.getLogoNavBarHref())))
        .andExpect(jsonPath("$.logoTop", is(appSettings.getLogoTopHref())))
        .andExpect(jsonPath("$.logoTopMaxHeight", is(appSettings.getLogoTopMaxHeight())))
        .andExpect(jsonPath("$.loginHref", is(UiContextController.LOGIN_HREF)))
        .andExpect(jsonPath("$.helpLink", is(UiContextController.HELP_LINK_JSON)))
        .andExpect(jsonPath("$.authenticated", is(false)))
        .andExpect(jsonPath("$.showCookieWall", is(false)))
        .andExpect(jsonPath("$.menu.type", is("menu")))
        .andExpect(jsonPath("$.additionalMessage", is("<a class=\"foo\">message</a>")))
        .andExpect(jsonPath("$.version", is("mock-version")))
        .andExpect(jsonPath("$.buildDate", is("mock date-time")))
        .andExpect(jsonPath("$.cssHref", is("cssHref")));
  }
}
