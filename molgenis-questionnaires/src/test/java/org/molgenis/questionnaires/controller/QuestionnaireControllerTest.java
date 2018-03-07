package org.molgenis.questionnaires.controller;

import org.mockito.Mock;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.core.ui.util.GsonConfig;
import org.molgenis.data.security.auth.User;
import org.molgenis.questionnaires.response.QuestionnaireResponse;
import org.molgenis.questionnaires.service.QuestionnaireService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Locale.ENGLISH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.questionnaires.meta.QuestionnaireStatus.NOT_STARTED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class QuestionnaireControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Mock
	private QuestionnaireService questionnaireService;

	@Mock
	private MenuReaderService menuReaderService;

	@Mock
	private AppSettings appSettings;

	@Mock
	private UserAccountService userAccountService;

	@Mock
	private LocaleResolver localeResolver;

	private static final String QUESTIONNAIRE_ID = "test_quest";

	private MockMvc mockMvc;

	@BeforeMethod
	private void beforeMethod()
	{
		initMocks(this);

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(QuestionnaireController.ID)).thenReturn("/test/path");

		User user = mock(User.class);
		when(user.isSuperuser()).thenReturn(false);

		when(menuReaderService.getMenu()).thenReturn(menu);
		when(appSettings.getLanguageCode()).thenReturn("en");
		when(userAccountService.getCurrentUser()).thenReturn(user);

		QuestionnaireController questionnaireController = new QuestionnaireController(questionnaireService,
				menuReaderService, appSettings, userAccountService);

		mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController)
								 .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
								 .build();
	}

	@Test
	public void testInit() throws Exception
	{
		when(localeResolver.resolveLocale(any())).thenReturn(ENGLISH);
		mockMvc.perform(get(QuestionnaireController.URI))
			   .andExpect(status().isOk())
			   .andExpect(view().name("view-questionnaire"))
			   .andExpect(model().attribute("baseUrl", "/test/path"))
			   .andExpect(model().attribute("lng", "en"))
			   .andExpect(model().attribute("fallbackLng", "en"))
			   .andExpect(model().attribute("isSuperUser", false));
	}

	@Test
	public void testGetQuestionnaireList() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = QuestionnaireResponse.create(QUESTIONNAIRE_ID, "label", "description", NOT_STARTED);
		List<QuestionnaireResponse> questionnaires = newArrayList(questionnaireResponse);
		when(questionnaireService.getQuestionnaires()).thenReturn(questionnaires);

		MvcResult result = mockMvc.perform(get(QuestionnaireController.URI + "/list"))
								  .andExpect(status().isOk())
								  .andReturn();

		String actual = result.getResponse().getContentAsString();
		String expected = "[{\"id\":\"test_quest\",\"label\":\"label\",\"description\":\"description\",\"status\":\"NOT_STARTED\"}]";

		assertEquals(actual, expected);
	}

	@Test
	public void testStartQuestionnaire() throws Exception
	{
		mockMvc.perform(get(QuestionnaireController.URI + "/start/1")).andExpect(status().isOk());
		verify(questionnaireService).startQuestionnaire("1");
	}

	@Test
	public void testGetQuestionnaireSubmissionText() throws Exception
	{
		when(questionnaireService.getQuestionnaireSubmissionText("1")).thenReturn("thanks!");
		MvcResult result = mockMvc.perform(get(QuestionnaireController.URI + "/submission-text/1"))
								  .andExpect(status().isOk())
								  .andReturn();

		String actual = result.getResponse().getContentAsString();
		String expected = "\"thanks!\"";

		assertEquals(actual, expected);
	}
}
