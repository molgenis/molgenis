package org.molgenis.navigator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.auth.User;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.Resource;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.molgenis.web.menu.MenuReaderService;
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
    when(menuReaderService.findMenuItemPath(NavigatorController.ID)).thenReturn("/test/path");
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
  public void testGetResources() throws Exception {
    String folderId = "myFolderId";
    Folder folder = Folder.create(folderId, "label", null);
    when(navigatorService.getFolder(folderId)).thenReturn(folder);
    when(navigatorService.getResources(folderId)).thenReturn(getMockResources());

    String expectedContent =
        "{\"folder\":{\"id\":\"myFolderId\",\"label\":\"label\"},\"resources\":[{\"type\":\"PACKAGE\",\"id\":\"myPackageId\",\"label\":\"label\",\"description\":\"description\",\"hidden\":false,\"readonly\":false}]}";
    mockMvc
        .perform(get(NavigatorController.URI + "/get?folderId=" + folderId))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedContent));
  }

  @Test
  public void testGetResourcesRootFolder() throws Exception {
    when(navigatorService.getFolder(null)).thenReturn(null);
    when(navigatorService.getResources(null)).thenReturn(getMockResources());

    String expectedContent =
        "{\"resources\":[{\"type\":\"PACKAGE\",\"id\":\"myPackageId\",\"label\":\"label\",\"description\":\"description\",\"hidden\":false,\"readonly\":false}]}";
    mockMvc
        .perform(get(NavigatorController.URI + "/get"))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedContent));
  }

  @Test
  public void testSearchResources() throws Exception {
    String query = "text";
    when(navigatorService.findResources(query)).thenReturn(getMockResources());

    String expectedContent =
        "{\"resources\":[{\"type\":\"PACKAGE\",\"id\":\"myPackageId\",\"label\":\"label\",\"description\":\"description\",\"hidden\":false,\"readonly\":false}]}";
    mockMvc
        .perform(get(NavigatorController.URI + "/search?query=" + query))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedContent));
  }

  @Test
  public void testCopyResources() throws Exception {
    String targetFolderId = "myFolderId";
    List<ResourceIdentifier> resources = getMockResourceIdentifiers();

    JobExecution jobExecution = mock(JobExecution.class);
    EntityType entityType = mock(EntityType.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);

    when(navigatorService.copyResources(resources, targetFolderId)).thenReturn(jobExecution);

    String json =
        "{\"resources\":[{\"type\":\"PACKAGE\",\"id\":\"myPackageId\",\"label\":\"label\",\"description\":\"description\"}],\"targetFolderId\":\"myFolderId\"}";
    mockMvc
        .perform(
            post(NavigatorController.URI + "/copy").content(json).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void testDownloadResources() throws Exception {
    List<ResourceIdentifier> resources = getMockResourceIdentifiers();

    JobExecution jobExecution = mock(JobExecution.class);
    EntityType entityType = mock(EntityType.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);

    when(navigatorService.downloadResources(resources)).thenReturn(jobExecution);

    String json =
        "{\"resources\":[{\"type\":\"PACKAGE\",\"id\":\"myPackageId\",\"label\":\"label\",\"description\":\"description\"}],\"targetFolderId\":\"myFolderId\"}";
    mockMvc
        .perform(
            post(NavigatorController.URI + "/download").content(json).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void testMoveResources() throws Exception {
    String targetFolderId = "myFolderId";
    List<ResourceIdentifier> resources = getMockResourceIdentifiers();

    String json =
        "{\"resources\":[{\"type\":\"PACKAGE\",\"id\":\"myPackageId\",\"label\":\"label\",\"description\":\"description\"}],\"targetFolderId\":\"myFolderId\"}";
    mockMvc
        .perform(
            post(NavigatorController.URI + "/move").content(json).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());
    verify(navigatorService).moveResources(resources, targetFolderId);
  }

  @Test
  public void testUpdateResource() throws Exception {
    String json =
        "{\"resource\":{\"id\":\"p0\",\"type\":\"PACKAGE\",\"label\":\"label\",\"description\":\"description\"}}";
    mockMvc
        .perform(
            put(NavigatorController.URI + "/update").content(json).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());
    verify(navigatorService)
        .updateResource(
            Resource.builder()
                .setType(ResourceType.PACKAGE)
                .setId("p0")
                .setLabel("label")
                .setDescription("description")
                .build());
  }

  @Test
  public void testDelete() throws Exception {
    String json =
        "{\"resources\": [{\"type\": \"PACKAGE\", \"id\": \"p0\"},{\"type\": \"PACKAGE\", \"id\": \"p1\"},{\"type\": \"ENTITY_TYPE\", \"id\": \"e0\"},{\"type\": \"ENTITY_TYPE\", \"id\": \"e1\"}]}";
    mockMvc
        .perform(
            delete(NavigatorController.URI + "/delete").content(json).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());
    verify(navigatorService)
        .deleteResources(
            asList(
                ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p0").build(),
                ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("p1").build(),
                ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build(),
                ResourceIdentifier.builder()
                    .setType(ResourceType.ENTITY_TYPE)
                    .setId("e1")
                    .build()));
  }

  private List<Resource> getMockResources() {
    return singletonList(
        Resource.builder()
            .setType(ResourceType.PACKAGE)
            .setId("myPackageId")
            .setLabel("label")
            .setDescription("description")
            .build());
  }

  private List<ResourceIdentifier> getMockResourceIdentifiers() {
    return singletonList(
        ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("myPackageId").build());
  }
}
