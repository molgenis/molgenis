package org.molgenis.core.ui.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import java.io.File;
import java.nio.file.Files;
import org.mockito.Mock;
import org.molgenis.core.ui.cookiewall.CookieWallService;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.molgenis.web.exception.FallbackExceptionHandler;
import org.molgenis.web.exception.GlobalControllerExceptionHandler;
import org.molgenis.web.exception.SpringExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {UiContextControllerTest.Config.class, GsonConfig.class})
public class UiContextControllerTest extends AbstractMockitoTestNGSpringContextTests {

  private MockMvc mockMvc;
  private static Authentication AUTHENTICATION_PREVIOUS;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;
  @Autowired private FallbackExceptionHandler fallbackExceptionHandler;
  @Autowired private SpringExceptionHandler springExceptionHandler;
  @Autowired private GlobalControllerExceptionHandler globalControllerExceptionHandler;
  @Autowired private Gson gson;
  @Mock private LocaleResolver localeResolver;

  @Mock private AppSettings appSettings;
  @Mock private CookieWallService cookieWallService;

  @Configuration
  public static class Config {
    @Bean
    public GlobalControllerExceptionHandler globalControllerExceptionHandler() {
      return new GlobalControllerExceptionHandler();
    }

    @Bean
    public FallbackExceptionHandler fallbackExceptionHandler() {
      return new FallbackExceptionHandler();
    }

    @Bean
    public SpringExceptionHandler springExceptionHandler() {
      return new SpringExceptionHandler();
    }
  }

  @BeforeMethod
  public void beforeMethod() {
    UiContextController uiContextController =
        new UiContextController(appSettings, cookieWallService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(uiContextController)
            .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
            .setLocaleResolver(localeResolver)
            .setControllerAdvice(
                globalControllerExceptionHandler, fallbackExceptionHandler, springExceptionHandler)
            .build();
  }

  @BeforeClass
  public void setUpBeforeClass() {
    AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails =
        when(mock(UserDetails.class).getUsername()).thenReturn("username").getMock();
    Authentication authentication =
        when(mock(Authentication.class).getPrincipal()).thenReturn(userDetails).getMock();
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  public void testGetContext() throws Exception {

    File resource = new ClassPathResource("exampleMenu.json").getFile();
    String menu = new String(Files.readAllBytes(resource.toPath()));

    when(appSettings.getLogoNavBarHref()).thenReturn("http:://thisissomelogo/");
    when(appSettings.getLogoTopHref()).thenReturn("http:://thisisotherref/");
    when(appSettings.getLogoTopMaxHeight()).thenReturn(22);
    when(appSettings.getMenu()).thenReturn(menu);
    when(cookieWallService.showCookieWall()).thenReturn(false);

    mockMvc
        .perform(get("/api/context"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.logoNavBarHref", is(appSettings.getLogoNavBarHref())))
        .andExpect(jsonPath("$.logoTopHref", is(appSettings.getLogoTopHref())))
        .andExpect(jsonPath("$.logoTopMaxHeight", is(appSettings.getLogoTopMaxHeight())))
        .andExpect(jsonPath("$.loginHref", is(UiContextController.LOGIN_HREF)))
        .andExpect(jsonPath("$.helpLink", is(UiContextController.HELP_LINK_JSON)))
        .andExpect(jsonPath("$.authenticated", is(false)))
        .andExpect(jsonPath("$.showCookieWall", is(false)))
        .andExpect(jsonPath("$.menu.type", is("menu")));
  }
}
