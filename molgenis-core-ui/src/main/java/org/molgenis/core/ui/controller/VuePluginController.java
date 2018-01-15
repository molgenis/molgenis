package org.molgenis.core.ui.controller;

import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;

import static java.util.Objects.requireNonNull;

public abstract class VuePluginController extends PluginController
{
	protected MenuReaderService menuReaderService;
	private AppSettings appSettings;
	private UserAccountService userAccountService;

	public VuePluginController(String uri, MenuReaderService menuReaderService, AppSettings appSettings,
			UserAccountService userAccountService)
	{
		super(uri);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.appSettings = requireNonNull(appSettings);
		this.userAccountService = requireNonNull(userAccountService);
	}

	protected void init(Model model, final String pluginId)
	{
		model.addAttribute("baseUrl", getBaseUrl(pluginId));
		model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("isSuperUser", userAccountService.getCurrentUser().isSuperuser());
	}

	protected String getBaseUrl(final String pluginId)
	{
		return menuReaderService.getMenu().findMenuItemPath(pluginId);
	}
}
