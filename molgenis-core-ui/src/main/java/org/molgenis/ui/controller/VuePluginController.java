package org.molgenis.ui.controller;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.web.PluginController;
import org.springframework.ui.Model;

import static java.util.Objects.requireNonNull;

public abstract class VuePluginController extends PluginController
{
	protected MenuReaderService menuReaderService;
	private LanguageService languageService;
	private AppSettings appSettings;
	protected UserAccountService userAccountService;

	public VuePluginController(String uri,MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings, UserAccountService userAccountService)
	{
		super(uri);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
		this.userAccountService = requireNonNull(userAccountService);
	}

	protected void init(Model model, final String pluginId)
	{
		model.addAttribute("baseUrl", getBaseUrl(pluginId));
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("isSuperUser",
				userAccountService.getCurrentUserIfPresent().map(User::isSuperuser).orElse(false));
	}

	protected String getBaseUrl(final String pluginId)
	{
		return menuReaderService.getMenu().findMenuItemPath(pluginId);
	}
}
