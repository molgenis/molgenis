package org.molgenis.navigator;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.Objects.requireNonNull;
import static org.molgenis.navigator.NavigatorController.URI;

@Controller
@RequestMapping(URI)
public class NavigatorController extends PluginController
{
	public static final String NAVIGATOR = "navigator";
	public static final String URI = PLUGIN_URI_PREFIX + NAVIGATOR;

	private MenuReaderService menuReaderService;
	private LanguageService languageService;
	private AppSettings appSettings;
	private UserAccountService userAccountService;

	public NavigatorController(MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings, UserAccountService userAccountService)
	{
		super(URI);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
		this.userAccountService = userAccountService;
	}

	@GetMapping("/**")
	public String init(Model model)
	{
		model.addAttribute("baseUrl", getBaseUrl());
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("isSuperUser", userAccountService.getCurrentUser().isSuperuser());
		return "view-navigator";
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(NavigatorController.NAVIGATOR);
	}
}
