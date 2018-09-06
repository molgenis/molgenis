package org.molgenis.questionnaires.controller;

import static java.util.Locale.ENGLISH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.auth.User;
import org.molgenis.questionnaires.meta.QuestionnaireStatus;
import org.molgenis.questionnaires.response.QuestionnaireResponse;
import org.molgenis.questionnaires.service.QuestionnaireService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = {GsonConfig.class})
public class QuestionnaireControllerTest extends AbstractTestNGSpringContextTests {
  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Mock private QuestionnaireService questionnaireService;

  @Mock private MenuReaderService menuReaderService;

  @Mock private AppSettings appSettings;

  @Mock private UserAccountService userAccountService;

  @Mock private LocaleResolver localeResolver;

  private static final String QUESTIONNAIRE_ID = "test_quest";

  private MockMvc mockMvc;

  @BeforeMethod
  private void beforeMethod() {
    initMocks(this);

    Menu menu = mock(Menu.class);
    when(menu.findMenuItemPath(QuestionnaireController.ID)).thenReturn("/test/path");

    User user = mock(User.class);
    when(user.isSuperuser()).thenReturn(false);

    when(menuReaderService.getMenu()).thenReturn(menu);
    when(appSettings.getLanguageCode()).thenReturn("en");
    when(userAccountService.getCurrentUser()).thenReturn(user);

    QuestionnaireController questionnaireController =
        new QuestionnaireController(
            questionnaireService, menuReaderService, appSettings, userAccountService);
    Model model = mock(Model.class);
    questionnaireController.initView(model);

    mockMvc =
        MockMvcBuilders.standaloneSetup(questionnaireController)
            .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
            .build();
  }

  @Test
  public void testInit() throws Exception {
    when(localeResolver.resolveLocale(any())).thenReturn(ENGLISH);
    mockMvc
        .perform(get(QuestionnaireController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name("view-questionnaire"))
        .andExpect(model().attribute("baseUrl", "/test/path"))
        .andExpect(model().attribute("lng", "en"))
        .andExpect(model().attribute("fallbackLng", "en"))
        .andExpect(model().attribute("isSuperUser", false));
  }

  @Test
  public void testGetQuestionnaireList() throws Exception {
    EntityType questionnaire = mock(EntityType.class);
    when(questionnaire.getId()).thenReturn("test_quest");
    when(questionnaire.getLabel("en")).thenReturn("label");
    when(questionnaire.getDescription("en")).thenReturn("description");
    List<EntityType> questionnaires = Arrays.asList(questionnaire);
    when(questionnaireService.getQuestionnaires()).thenReturn(questionnaires.stream());

    MvcResult result =
        mockMvc
            .perform(get(QuestionnaireController.URI + "/list"))
            .andExpect(status().isOk())
            .andReturn();

    String actual = result.getResponse().getContentAsString();
    String expected =
        "[{\"id\":\"test_quest\",\"label\":\"label\",\"description\":\"description\",\"status\":\"NOT_STARTED\"}]";

    assertEquals(actual, expected);
  }

  @Test
  public void testStartQuestionnaire() throws Exception {
    String id = "1";
    QuestionnaireResponse questionnaireResponse =
        QuestionnaireResponse.create(id, "label", "desc", QuestionnaireStatus.NOT_STARTED);
    when(questionnaireService.startQuestionnaire(id)).thenReturn(questionnaireResponse);
    mockMvc
        .perform(get(QuestionnaireController.URI + "/start/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", Matchers.is(id)));
    verify(questionnaireService).startQuestionnaire(id);
  }

  @Test
  public void testGetQuestionnaireSubmissionText() throws Exception {
    when(questionnaireService.getQuestionnaireSubmissionText("1")).thenReturn("thanks!");
    MvcResult result =
        mockMvc
            .perform(get(QuestionnaireController.URI + "/submission-text/1"))
            .andExpect(status().isOk())
            .andReturn();

    String actual = result.getResponse().getContentAsString();
    String expected = "\"thanks!\"";

    assertEquals(actual, expected);
  }
}
