package org.molgenis.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.app.controller.ContentControllersTest.Config;
import org.molgenis.core.ui.controller.StaticContentService;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileStore;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebAppConfiguration
@ContextConfiguration(classes = {Config.class, GsonConfig.class})
class ContentControllersTest extends AbstractMockitoSpringContextTests {

  @Autowired
  private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Autowired
  private HomeController homeController;

  @Autowired
  private NewsController newsController;

  @Autowired
  private BackgroundController backgroundController;

  @Autowired
  private ContactController contactController;

  @Autowired
  private ReferencesController referencesController;

  @Autowired
  private StaticContentService staticContentService;

  private MockMvc mockMvcHome;
  private MockMvc mockMvcNews;
  private MockMvc mockMvcContact;
  private MockMvc mockMvcReferences;
  private MockMvc mockMvcBackground;

  @BeforeEach
  void beforeMethod() {
    mockMvcHome =
        MockMvcBuilders.standaloneSetup(homeController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();

    mockMvcNews =
        MockMvcBuilders.standaloneSetup(newsController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();

    mockMvcContact =
        MockMvcBuilders.standaloneSetup(contactController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();

    mockMvcBackground =
        MockMvcBuilders.standaloneSetup(backgroundController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();

    mockMvcReferences =
        MockMvcBuilders.standaloneSetup(referencesController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();
  }

  @Test
  void initHome() throws Exception {
    this.initMethodTest(mockMvcHome, HomeController.URI);
  }

  @Test
  void initNews() throws Exception {
    this.initMethodTest(mockMvcNews, NewsController.URI);
  }

  @Test
  void initBackground() throws Exception {
    this.initMethodTest(mockMvcBackground, BackgroundController.URI);
  }

  @Test
  void initContact() throws Exception {
    this.initMethodTest(mockMvcContact, ContactController.URI);
  }

  @Test
  void initReferences() throws Exception {
    this.initMethodTest(mockMvcReferences, ReferencesController.URI);
  }

  @Test
  void initEditGetHome() throws Exception {
    this.initEditGetMethodTest(mockMvcHome, HomeController.URI);
  }

  @Test
  void initEditGetNews() throws Exception {
    this.initEditGetMethodTest(mockMvcNews, NewsController.URI);
  }

  @Test
  void initEditGetBackground() throws Exception {
    this.initEditGetMethodTest(
        mockMvcBackground, BackgroundController.URI);
  }

  @Test
  void initEditGetContact() throws Exception {
    this.initEditGetMethodTest(mockMvcContact, ContactController.URI);
  }

  @Test
  void initEditGetReferences() throws Exception {
    this.initEditGetMethodTest(
        mockMvcReferences, ReferencesController.URI);
  }

  private void initMethodTest(MockMvc mockMvc, String uri)
      throws Exception {
    when(this.staticContentService.getContent(any(String.class))).thenReturn("staticcontent");
    mockMvc
        .perform(MockMvcRequestBuilders.get(uri))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(view().name("view-staticcontent"))
        .andExpect(model().attributeExists("content"))
        .andExpect(model().attributeExists("isCurrentUserCanEdit"));
  }

  private void initEditGetMethodTest(MockMvc mockMvc, String uri)
      throws Exception {
    when(this.staticContentService.getContent(any(String.class))).thenReturn("staticcontent");
    when(this.staticContentService.isCurrentUserCanEdit("staticcontent")).thenReturn(true);

    mockMvc
        .perform(MockMvcRequestBuilders.get(uri + "/edit"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(view().name("view-staticcontent-edit"))
        .andExpect(model().attributeExists("content"));
  }

  @Test
  void initNotExistingURI() throws Exception {
    mockMvcHome
        .perform(MockMvcRequestBuilders.get(HomeController.URI + "/NotExistingURI"))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Configuration
  static class Config {

    @Bean
    HomeController homeController() {
      return new HomeController();
    }

    @Bean
    NewsController newsController() {
      return new NewsController();
    }

    @Bean
    BackgroundController backgroundController() {
      return new BackgroundController();
    }

    @Bean
    ContactController ContactController() {
      return new ContactController();
    }

    @Bean
    ReferencesController referencesController() {
      return new ReferencesController();
    }

    @Bean
    StaticContentService staticContentService() {
      return mock(StaticContentService.class);
    }

    @Bean
    FileStore fileStore() {
      return mock(FileStore.class);
    }

    @Bean
    DataService dataService() {
      return mock(DataService.class);
    }
  }
}
