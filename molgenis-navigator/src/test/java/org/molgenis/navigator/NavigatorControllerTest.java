package org.molgenis.navigator;

import org.mockito.Mock;
import org.molgenis.auth.User;
import org.molgenis.data.i18n.LanguageServiceImpl;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
	private LanguageServiceImpl languageService;

	@Mock
	private AppSettings appSettings;

	@Mock
	private UserAccountService userAccountService;

	@BeforeMethod
	public void before()
	{
		initMocks(this);

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(NavigatorController.ID)).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(languageService.getCurrentUserLanguageCode()).thenReturn("AABBCC");
		when(appSettings.getLanguageCode()).thenReturn("DDEEFF");
		User user = mock(User.class);
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(user.isSuperuser()).thenReturn(false);

		NavigatorController navigatorController = new NavigatorController(menuReaderService, languageService,
				appSettings, userAccountService);
		mockMvc = MockMvcBuilders.standaloneSetup(navigatorController).build();
	}

	/**
	 * Test that a get call to the plugin returns the correct view
	 */
	@Test
	public void testInit() throws Exception
	{
		mockMvc.perform(get(NavigatorController.URI)).andExpect(status().isOk())
				.andExpect(view().name("view-navigator")).
				andExpect(model().attribute("baseUrl", "/test/path")).
				andExpect(model().attribute("lng", "AABBCC")).
				andExpect(model().attribute("fallbackLng", "DDEEFF")).
				andExpect(model().attribute("isSuperUser", false));
	}

}