package org.molgenis.navigator;

import org.mockito.Mock;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Configuration
@EnableWebMvc
public class NavigatorControllerTest
{
	private MockMvc mockMvc;

	@Mock
	private MenuReaderService menuReaderService;

	@Mock
	private AppSettings appSettings;

	@Mock
	private UserAccountService userAccountService;

	@Mock
	private LocaleResolver localeResolver;

	@BeforeMethod
	public void before()
	{
		initMocks(this);

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(NavigatorController.ID)).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(appSettings.getLanguageCode()).thenReturn("de");
		User user = mock(User.class);
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(user.isSuperuser()).thenReturn(false);

		NavigatorController navigatorController = new NavigatorController(menuReaderService, appSettings,
				userAccountService);
		mockMvc = MockMvcBuilders.standaloneSetup(navigatorController).setLocaleResolver(localeResolver).build();
	}

	/**
	 * Test that a get call to the plugin returns the correct view
	 */
	@Test
	public void testInit() throws Exception
	{
		when(localeResolver.resolveLocale(any())).thenReturn(Locale.FRENCH);
		mockMvc.perform(get(NavigatorController.URI))
			   .andExpect(status().isOk())
			   .andExpect(view().name("view-navigator"))
			   .andExpect(model().attribute("baseUrl", "/test/path"))
			   .andExpect(model().attribute("lng", "fr"))
			   .andExpect(model().attribute("fallbackLng", "de"))
			   .andExpect(model().attribute("isSuperUser", false));
	}

}