package org.molgenis.searchall.controller;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.web.PluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.Objects.requireNonNull;
import static org.molgenis.searchall.controller.SearchAllPluginController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class SearchAllPluginController extends PluginController
{
	public static final String SEARCHALL = "searchAll";
	public static final String URI = PLUGIN_URI_PREFIX + SEARCHALL;

	private final LanguageService languageService;
	private final AppSettings appSettings;
	private final MenuReaderService menuReaderService;

	public static final String NAVIGATOR = "navigator";
	public static final String DATAEXPLORER = "dataexplorer";

	@Autowired
	public SearchAllPluginController(LanguageService languageService, AppSettings appSettings,
			MenuReaderService menuReaderService)
	{
		super(URI);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
		this.menuReaderService = requireNonNull(menuReaderService);
	}

	@RequestMapping(value = "/**", method = GET)
	public String init(Model model)
	{
		model.addAttribute("baseUrl", menuReaderService.getMenu().findMenuItemPath(SEARCHALL));
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("navigatorBaseUrl", menuReaderService.getMenu().findMenuItemPath(NAVIGATOR));
		model.addAttribute("dataExplorerBaseUrl", menuReaderService.getMenu().findMenuItemPath(DATAEXPLORER));

		return "view-search-all";
	}

}
