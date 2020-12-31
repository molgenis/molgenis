package org.molgenis.core.ui.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebAppConfiguration
@ContextConfiguration(classes = ThemeEndpointControllerTest.Config.class)
class ThemeEndpointControllerTest extends AbstractMockitoSpringContextTests {

  private MockMvc mockMvc;

  @Autowired private ThemeEndpointController themeEndpointController;

  @Autowired private AppSettings appSettings;

  @BeforeEach
  void beforeMethod() {
    when(appSettings.getThemeURL()).thenReturn("/foo/bar/blue-style.css");
    mockMvc = MockMvcBuilders.standaloneSetup(themeEndpointController).build();
  }

  @Test
  void testGetTheme() throws Exception {
    mockMvc
        .perform(get("/" + ThemeEndpointController.ID + "/style.css"))
        .andExpect(redirectedUrl("/foo/bar/blue-style.css"))
        .andExpect(status().isFound());
  }

  @Configuration
  @EnableWebMvc
  static class Config {
    @Bean
    AppSettings appSettings() {
      return mock(AppSettings.class);
    }

    @Bean
    ThemeEndpointController themeEndpointController() {
      return new ThemeEndpointController(appSettings());
    }
  }
}
