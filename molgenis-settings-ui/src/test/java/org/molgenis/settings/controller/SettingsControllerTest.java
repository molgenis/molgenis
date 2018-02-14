package org.molgenis.settings.controller;

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
public class SettingsControllerTest
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
		when(menu.findMenuItemPath(SettingsController.ID)).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(appSettings.getLanguageCode()).thenReturn("DDEEFF");
		User user = mock(User.class);
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(user.isSuperuser()).thenReturn(false);

		SettingsController settingsController = new SettingsController(menuReaderService, appSettings,
				userAccountService);
		mockMvc = standaloneSetup(settingsController).build();
	}

	@Test
	public void testInit() throws Exception
	{
		mockMvc.perform(get(SettingsController.URI))
			   .andExpect(status().isOk())
			   .andExpect(view().name(SettingsController.VIEW_TEMPLATE))
			   .andExpect(model().attribute("baseUrl", "/test/path"))
			   .andExpect(model().attribute("lng", "en"))
			   .andExpect(model().attribute("fallbackLng", "DDEEFF"))
			   .andExpect(model().attribute("isSuperUser", false));
	}

}