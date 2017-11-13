package org.molgenis.navigator;

import org.molgenis.data.i18n.LanguageServiceImpl;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.controller.VuePluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.navigator.NavigatorController.URI;

@Controller
@RequestMapping(URI)
public class NavigatorController extends VuePluginController
{
	public static final String ID = "navigator";
	public static final String URI = PLUGIN_URI_PREFIX + ID;

	public NavigatorController(MenuReaderService menuReaderService, LanguageServiceImpl languageService,
			AppSettings appSettings, UserAccountService userAccountService)
	{
		super(URI, menuReaderService, languageService, appSettings, userAccountService);
	}

	@GetMapping("/**")
	public String init(Model model)
	{
		super.init(model, ID);
		return "view-navigator";
	}

}