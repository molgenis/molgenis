package org.molgenis.navigator;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static java.util.Objects.requireNonNull;
import static org.molgenis.navigator.NavigatorController.URI;

@Controller
@RequestMapping(URI + "/**")
public class NavigatorController extends MolgenisPluginController
{
	public static final String NAVIGATOR = "navigator";
	public static final String URI = PLUGIN_URI_PREFIX + NAVIGATOR;

	private MenuReaderService menuReaderService;
	private LanguageService languageService;
	private AppSettings appSettings;

	public NavigatorController(MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings)
	{
		super(URI);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute("baseUrl", getBaseUrl());
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());

		return "view-navigator";
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(NavigatorController.NAVIGATOR);
	}
}
