package org.molgenis.metadata.manager.controller;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.Objects.requireNonNull;
import static org.molgenis.metadata.manager.controller.MetadataManagerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI + "/**")
public class MetadataManagerController extends MolgenisPluginController
{
	public static final String METADATA_MANAGER = "metadata-manager";
	public static final String URI = PLUGIN_URI_PREFIX + METADATA_MANAGER;

	private MenuReaderService menuReaderService;
	private LanguageService languageService;
	private AppSettings appSettings;

	public MetadataManagerController(MenuReaderService menuReaderService, LanguageService languageService, AppSettings appSettings)
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
		return "view-metadata-manager";
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(MetadataManagerController.METADATA_MANAGER);
	}
}
