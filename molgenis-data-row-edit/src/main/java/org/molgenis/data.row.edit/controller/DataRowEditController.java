package org.molgenis.data.row.edit.controller;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.data.row.edit.controller.DataRowEditController.URI;

@Controller
@RequestMapping(URI)
public class DataRowEditController extends VuePluginController

{
	public static final String ID = "data-row-edit";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public static final String VIEW_TEMPLATE = "view-data-row-edit";


	DataRowEditController(MenuReaderService menuReaderService, AppSettings appSettings,
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
