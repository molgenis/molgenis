package org.molgenis.settings.controller;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.settings.controller.SettingsController.URI;

@Controller
@RequestMapping(URI)
public class SettingsController extends VuePluginController

{
	public static final String ID = "settings";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public static final String VIEW_TEMPLATE = "view-settings";


	SettingsController(MenuReaderService menuReaderService, AppSettings appSettings,
			UserAccountService userAccountService)
	{
		super(URI, menuReaderService, appSettings, userAccountService);
	}

	@GetMapping("/**")
	public String init(Model model)
	{
		super.init(model, ID);
		return VIEW_TEMPLATE;
	}
}
