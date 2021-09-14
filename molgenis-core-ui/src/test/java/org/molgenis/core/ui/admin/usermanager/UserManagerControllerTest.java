package org.molgenis.core.ui.admin.usermanager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.gson.Gson;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.core.ui.settings.FormSettings;
import org.molgenis.data.DataService;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@WebAppConfiguration
@ContextConfiguration(classes = {UserManagerControllerTest.Config.class})
class UserManagerControllerTest extends AbstractMockitoSpringContextTests {

  @Autowired private UserManagerController userManagerController;

  @Autowired private UserManagerService pluginUserManagerService;

  @Autowired private FormSettings formSettings;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
    freeMarkerViewResolver.setSuffix(".ftl");
    mockMvc =
        MockMvcBuilders.standaloneSetup(userManagerController)
            .setMessageConverters(
                new FormHttpMessageConverter(), new GsonHttpMessageConverter(new Gson()))
            .build();
    reset(pluginUserManagerService);
  }

  @Test
  void init() throws Exception {
    when(pluginUserManagerService.getActiveSessionsCount()).thenReturn(7L);
    List<UserViewData> userViewData = Collections.singletonList(mock(UserViewData.class));
    when(pluginUserManagerService.getAllUsers()).thenReturn(userViewData);
    when(pluginUserManagerService.getActiveSessionUserNames())
        .thenReturn(Collections.singletonList("Suzi"));
    this.mockMvc
        .perform(get("/plugin/usermanager"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("activeSessionCount", 7L))
        .andExpect(model().attribute("users", userViewData))
        .andExpect(model().attribute("activeUsers", Collections.singletonList("Suzi")))
        .andExpect(model().attribute("formSettings", formSettings))
        .andExpect(view().name("view-usermanager"));
  }

  @Configuration
  static class Config {

    @Bean
    FormSettings formSettings() {
      return mock(FormSettings.class);
    }

    @Bean
    UserManagerController userManagerController(FormSettings formSettings) {
      return new UserManagerController(userManagerService(), formSettings);
    }

    @Bean
    UserManagerService userManagerService() {
      return mock(UserManagerService.class);
    }

    @Bean
    DataService dataService() {
      return mock(DataService.class);
    }
  }
}
