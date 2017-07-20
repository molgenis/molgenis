package org.molgenis.navigator;

import org.mockito.Mock;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
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
	private LanguageService languageService;

	@Mock
	private AppSettings appSettings;

	@BeforeMethod
	public void before()
	{
		initMocks(this);

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(NavigatorController.NAVIGATOR)).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(languageService.getCurrentUserLanguageCode()).thenReturn("AABBCC");
		when(appSettings.getLanguageCode()).thenReturn("DDEEFF");

		NavigatorController navigatorController = new NavigatorController(menuReaderService, languageService,
				appSettings);
		mockMvc = MockMvcBuilders.standaloneSetup(navigatorController).build();
	}

	/**
	 * Test that a get call to the plugin returns the correct view
	 */
	@Test
	public void testInit() throws Exception
	{
		mockMvc.perform(get(NavigatorController.URI)).andExpect(status().isOk())
				.andExpect(view().name("view-navigator")).andExpect(model().attribute("baseUrl", "/test/path")).
					   andExpect(model().attribute("lng", "AABBCC"))
			   .
					   andExpect(model().attribute("fallbackLng", "DDEEFF"));
	}

}