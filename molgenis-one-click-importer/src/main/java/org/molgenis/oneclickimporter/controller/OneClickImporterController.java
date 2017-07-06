package org.molgenis.oneclickimporter.controller;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.Objects.requireNonNull;
import static org.molgenis.oneclickimporter.controller.OneClickImporterController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class OneClickImporterController extends MolgenisPluginController
{
	public static final String ONE_CLICK_IMPORTER = "one-click-importer";
	public static final String URI = PLUGIN_URI_PREFIX + ONE_CLICK_IMPORTER;

	private MenuReaderService menuReaderService;
	private LanguageService languageService;
	private AppSettings appSettings;

	public OneClickImporterController(MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings)
	{
		super(URI);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("baseUrl", getBaseUrl());
		return "view-one-click-importer";
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER);
	}
}
