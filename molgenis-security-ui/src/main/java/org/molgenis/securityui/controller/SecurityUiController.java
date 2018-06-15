package org.molgenis.securityui.controller;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.securityui.controller.SecurityUiController.URI;

@Controller
@RequestMapping(URI)
public class SecurityUiController extends VuePluginController
{
	public static final String ID = "security-ui";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public static final String VIEW_TEMPLATE = "view-security-ui";

	private AppSettings appSettings;

	SecurityUiController(MenuReaderService menuReaderService, AppSettings appSettings,
			UserAccountService userAccountService)
	{
		super(URI, menuReaderService, appSettings, userAccountService);
		this.appSettings = appSettings;
	}

	@GetMapping("/**")
	public String init(Model model)
	{
		super.init(model, ID);
		return VIEW_TEMPLATE;
	}
}
