package org.molgenis.securityui.controller;

import org.mockito.Mock;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Configuration
@EnableWebMvc
public class SecurityUiControllerTest
{
	private MockMvc mockMvc;

	@Mock
	private MenuReaderService menuReaderService;

	@Mock
	private AppSettings appSettings;

	@Mock
	private UserAccountService userAccountService;

	@BeforeMethod
	public void before()
	{
		initMocks(this);

		Menu menu = mock(Menu.class);
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(appSettings.getLanguageCode()).thenReturn("en");
		User user = mock(User.class);
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(user.isSuperuser()).thenReturn(false);

		SecurityUiController securityUiController = new SecurityUiController(menuReaderService, appSettings,
				userAccountService);
		mockMvc = standaloneSetup(securityUiController).build();
	}

	@Test
	public void testInit() throws Exception
	{
		mockMvc.perform(get(SecurityUiController.URI))
			   .andExpect(status().isOk())
			   .andExpect(view().name(SecurityUiController.VIEW_TEMPLATE))
			   .andExpect(model().attribute("baseUrl", "/plugin/security-ui"))
			   .andExpect(model().attribute("lng", "en"))
			   .andExpect(model().attribute("fallbackLng", "en"));
	}

}
