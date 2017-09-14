package org.molgenis.ui.admin.user;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.auth.Group;
import org.molgenis.auth.User;
import org.molgenis.data.DataService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
								 .setMessageConverters(gsonHttpMessageConverter)
								 .build();
	}

	@Test
	public void changeLanguageOk() throws Exception
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		mockMvc.perform(post("/plugin/useraccount/language/update").param("languageCode", "nl"))
			   .andExpect(status().isNoContent());
		verify(user).setLanguageCode("nl");
		verify(userAccountService).updateCurrentUser(user);
	}

	@Test
	public void changeLanguageForbidden() throws Exception
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		doThrow(new AccessDeniedException("Access denied.")).when(userAccountService).updateCurrentUser(user);

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
		mockMvc.perform(post("/plugin/useraccount/language/update").param("languageCode", "nl"))
			   .andExpect(status().isInternalServerError());
	}
}
