package org.molgenis.metadata.manager.controller;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.populate.Sequences;
import org.molgenis.data.security.auth.User;
import org.molgenis.metadata.manager.model.EditorAttribute;
import org.molgenis.metadata.manager.model.EditorAttributeIdentifier;
import org.molgenis.metadata.manager.model.EditorAttributeResponse;
import org.molgenis.metadata.manager.model.EditorEntityType;
import org.molgenis.metadata.manager.model.EditorEntityTypeResponse;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;
import org.molgenis.metadata.manager.service.MetadataManagerService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonWebConfig;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@WebAppConfiguration
@ContextConfiguration(classes = {MetadataManagerControllerTest.Config.class, GsonWebConfig.class})
class MetadataManagerControllerTest extends AbstractMockitoSpringContextTests {
  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Autowired private MenuReaderService menuReaderService;

  @Autowired private AppSettings appSettings;

  @Autowired private MetadataManagerService metadataManagerService;

  @Autowired private UserAccountService userAccountService;

  @Mock private LocaleResolver localeResolver;

  @Mock private Sequences sequences;

  private MockMvc mockMvc;

  @BeforeEach
  void beforeMethod() {
    MockitoAnnotations.initMocks(this);
    FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
    freeMarkerViewResolver.setSuffix(".ftl");

    when(menuReaderService.findMenuItemPath(MetadataManagerController.METADATA_MANAGER))
        .thenReturn("/test/path");

    when(appSettings.getLanguageCode()).thenReturn("nl");
    User user = mock(User.class);
    when(userAccountService.getCurrentUser()).thenReturn(user);

    MetadataManagerController metadataEditorController =
        new MetadataManagerController(menuReaderService, metadataManagerService, sequences);

    mockMvc =
        MockMvcBuilders.standaloneSetup(metadataEditorController)
            .setLocaleResolver(localeResolver)
            .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
            .build();
  }

  @Test
  void testInit() throws Exception {
    when(localeResolver.resolveLocale(any())).thenReturn(Locale.GERMAN);
    mockMvc
        .perform(get("/plugin/metadata-manager"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-metadata-manager"))
        .andExpect(model().attribute("baseUrl", "/test/path"));
  }

  @Test
  void testGetEditorPackages() throws Exception {
    when(metadataManagerService.getEditorPackages()).thenReturn(getEditorPackageResponse());
    mockMvc
        .perform(get("/plugin/metadata-manager/editorPackages"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
        .andExpect(content().string(getEditorPackageResponseJson()));
  }

  @Test
  void testGetEditorEntityType() throws Exception {
    when(metadataManagerService.getEditorEntityType("id_1"))
        .thenReturn(getEditorEntityTypeResponse());
    mockMvc
        .perform(get("/plugin/metadata-manager/entityType/{id}", "id_1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
        .andExpect(content().string(getEditorEntityTypeResponseJson()));
  }

  @Test
  void testCreateEditorEntityType() throws Exception {
    EditorEntityTypeResponse editorEntityTypeResponse = getEditorEntityTypeResponse();
    when(metadataManagerService.createEditorEntityType()).thenReturn(editorEntityTypeResponse);
    mockMvc
        .perform(get("/plugin/metadata-manager/create/entityType"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
        .andExpect(content().string(getEditorEntityTypeResponseJson()));
  }

  @Test
  void testUpsertEntityType() throws Exception {
    mockMvc
        .perform(
            post("/plugin/metadata-manager/entityType")
                .contentType(APPLICATION_JSON_UTF8)
                .content(getEditorEntityTypeJson()))
        .andExpect(status().isOk());
    verify(metadataManagerService, times(1)).upsertEntityType(getEditorEntityType());
  }

  @Test
  void testCreateEditorAttribute() throws Exception {
    when(metadataManagerService.createEditorAttribute()).thenReturn(getEditorAttributeResponse());
    mockMvc
        .perform(get("/plugin/metadata-manager/create/attribute"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
        .andExpect(content().string(getEditorAttributeResponseJson()));
  }

  private List<EditorPackageIdentifier> getEditorPackageResponse() {
    return newArrayList(EditorPackageIdentifier.create("test", "test"));
  }

  private String getEditorPackageResponseJson() {
    return "[{\"id\":\"test\",\"label\":\"test\"}]";
  }

  private EditorEntityTypeResponse getEditorEntityTypeResponse() {
    return EditorEntityTypeResponse.create(
        getEditorEntityType(), newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx"));
  }

  private String getEditorEntityTypeResponseJson() {
    return "{\"entityType\":"
        + getEditorEntityTypeJson()
        + ",\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
  }

  private EditorEntityType getEditorEntityType() {
    return EditorEntityType.create(
        "id_1",
        null,
        ImmutableMap.of(),
        null,
        ImmutableMap.of(),
        false,
        "backend",
        null,
        null,
        ImmutableList.of(),
        ImmutableList.of(),
        ImmutableList.of(),
        EditorAttributeIdentifier.create("id", "label"),
        EditorAttributeIdentifier.create("id", "label"),
        ImmutableList.of());
  }

  private String getEditorEntityTypeJson() {
    return "{\"id\":\"id_1\",\"labelI18n\":{},\"descriptionI18n\":{},\"abstract0\":false,\"backend\":\"backend\",\"attributes\":[],\"referringAttributes\":[],\"tags\":[],\"idAttribute\":{\"id\":\"id\",\"label\":\"label\"},\"labelAttribute\":{\"id\":\"id\",\"label\":\"label\"},\"lookupAttributes\":[]}";
  }

  private EditorAttributeResponse getEditorAttributeResponse() {
    EditorAttribute editorAttribute =
        EditorAttribute.create(
            "1",
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            false,
            false,
            false,
            null,
            ImmutableMap.of(),
            null,
            ImmutableMap.of(),
            false,
            ImmutableList.of(),
            null,
            null,
            false,
            false,
            ImmutableList.of(),
            null,
            null,
            null,
            null,
            1);

    return EditorAttributeResponse.create(
        editorAttribute, newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx"));
  }

  private String getEditorAttributeResponseJson() {
    return "{\"attribute\":{\"id\":\"1\",\"cascadeDelete\":false,\"nullable\":false,\"auto\":false,\"visible\":false,\"labelI18n\":{},\"descriptionI18n\":{},\"aggregatable\":false,\"enumOptions\":[],\"readonly\":false,\"unique\":false,\"tags\":[],\"sequenceNumber\":1},\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
  }

  @Configuration
  static class Config {
    @Bean
    MenuReaderService menuReaderService() {
      return mock(MenuReaderService.class);
    }

    @Bean
    AppSettings appSettings() {
      return mock(AppSettings.class);
    }

    @Bean
    MetadataManagerService metadataManagerService() {
      return mock(MetadataManagerService.class);
    }

    @Bean
    UserAccountService userAccountService() {
      return mock(UserAccountService.class);
    }
  }
}
