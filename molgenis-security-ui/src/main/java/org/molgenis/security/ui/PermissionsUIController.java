package org.molgenis.security.ui;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.ui.PermissionsUIController.URI;

@Controller
@RequestMapping(URI + "/**")
public class PermissionsUIController extends PluginController
{
	public static final String PERMISSIONS = "permissions";
	public static final String URI = PLUGIN_URI_PREFIX + PERMISSIONS;

	private MenuReaderService menuReaderService;
	private LanguageService languageService;
	private AppSettings appSettings;

	public PermissionsUIController(MenuReaderService menuReaderService, LanguageService languageService,
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

		return "view-permissions";
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(PermissionsUIController.PERMISSIONS);
	}
}
