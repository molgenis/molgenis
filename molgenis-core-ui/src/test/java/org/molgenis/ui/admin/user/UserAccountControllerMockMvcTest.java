package org.molgenis.ui.admin.user;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
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
@ContextConfiguration(classes = { UserAccountControllerMockMvcTest.Config.class, GsonConfig.class })
public class UserAccountControllerMockMvcTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private Config config;
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
	private Group allUsers;

	private MockMvc mockMvc;
	private User user = User.builder().id("id").username("user").password("crypted").email("user@example.com").build();

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
		verify(userAccountService).updateCurrentUser(user.toBuilder().languageCode("nl").build());
	}

	@Test
	public void changeLanguageForbidden() throws Exception
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		doThrow(new AccessDeniedException("Access denied.")).when(userAccountService)
															.updateCurrentUser(any(User.class));

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

	@Configuration
	public static class Config
	{
		@Mock
		private UserAccountService userAccountService;
		@Mock
		private GroupService groupService;
		@Mock
		private RecoveryService recoveryService;
		@Mock
		private TwoFactorAuthenticationService twoFactorAuthenticationService;
		@Mock
		private AuthenticationSettings authenticationSettings;
		@Mock
		private DataService dataService;

		public Config()
		{
			MockitoAnnotations.initMocks(this);
		}

		public void resetMocks()
		{
			reset(userAccountService, groupService, recoveryService, twoFactorAuthenticationService,
					authenticationSettings, dataService);
		}

		@Bean
		public DataService dataService()
		{
			return dataService;
		}

		@Bean
		public UserAccountService userAccountService()
		{
			return userAccountService;
		}

		@Bean
		public GroupService groupService()
		{
			return groupService;
		}

		@Bean
		public RecoveryService recoveryService()
		{
			return recoveryService;
		}

		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return twoFactorAuthenticationService;
		}

		@Bean
		public AuthenticationSettings authenticationSettings()
		{
			return authenticationSettings;
		}

		@Bean
		public UserAccountController userAccountController()
		{
			return new UserAccountController(userAccountService(), recoveryService(), twoFactorAuthenticationService(),
					authenticationSettings(), groupService());
		}
	}
}
