package org.molgenis.ui.controller;

import org.molgenis.data.i18n.LanguageServiceImpl;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.web.PluginController;
import org.springframework.ui.Model;

import static java.util.Objects.requireNonNull;

public abstract class VuePluginController extends PluginController
{
	protected MenuReaderService menuReaderService;
	private LanguageServiceImpl languageService;
	private AppSettings appSettings;
	private UserAccountService userAccountService;

	public VuePluginController(String uri, MenuReaderService menuReaderService, LanguageServiceImpl languageService,
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
		model.addAttribute("isSuperUser", userAccountService.getCurrentUser().isSuperuser());
	}

	protected String getBaseUrl(final String pluginId)
	{
		return menuReaderService.getMenu().findMenuItemPath(pluginId);
	}
}
