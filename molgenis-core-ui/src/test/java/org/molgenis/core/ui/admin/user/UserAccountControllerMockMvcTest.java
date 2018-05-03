package org.molgenis.core.ui.admin.user;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.converter.GsonConfig;
import org.molgenis.web.exception.FallbackExceptionHandler;
import org.molgenis.web.exception.GlobalControllerExceptionHandler;
import org.molgenis.web.exception.SpringExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ContextConfiguration(classes = { UserAccountControllerTestConfig.class, GsonConfig.class })
public class UserAccountControllerMockMvcTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private UserAccountControllerTestConfig config;
	@Autowired
	private UserAccountService userAccountService;
	@Autowired
	private RecoveryService recoveryService;
	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;
	@Autowired
	private AuthenticationSettings authenticationSettings;
	@Autowired
	private UserAccountController userAccountController;
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;
	@Autowired
	private LocaleResolver localeResolver;
	@Mock
	private Model model;
	@Mock
	private User user;
	@Mock
	private Group allUsers;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp() throws Exception
	{
		config.resetMocks();
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(userAccountController)
								 .setLocaleResolver(localeResolver)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .setControllerAdvice(new GlobalControllerExceptionHandler(),
										 new FallbackExceptionHandler(), new SpringExceptionHandler())
								 .build();
	}

	@Test
	public void changeLanguageOk() throws Exception
	{
		mockMvc.perform(post("/plugin/useraccount/language/update").param("languageCode", "nl"))
			   .andExpect(status().isNoContent());
		verify(localeResolver).setLocale(any(), any(), eq(new Locale("nl")));
	}

	@Test
	public void changeLanguageForbidden() throws Exception
	{
		doThrow(new AccessDeniedException("Access denied.")).when(localeResolver)
															.setLocale(any(), any(), eq(new Locale("nl")));
		//FIXME: update expected status after specific exceptions are implemented
		mockMvc.perform(post("/plugin/useraccount/language/update").param("languageCode", "nl"))
			   .andExpect(status().isForbidden());
	}

	@Test
	public void changeLanguageUnknownLanguage() throws Exception
	{
		mockMvc.perform(post("/plugin/useraccount/language/update").param("languageCode", "swahili"))
			   .andExpect(status().isBadRequest());
	}

	@Test
	public void changeLanguageNPE() throws Exception
	{
		doThrow(new NullPointerException()).when(localeResolver).setLocale(any(), any(), any());
		mockMvc.perform(post("/plugin/useraccount/language/update").param("languageCode", "nl"))
			   .andExpect(status().isInternalServerError());
	}
}
