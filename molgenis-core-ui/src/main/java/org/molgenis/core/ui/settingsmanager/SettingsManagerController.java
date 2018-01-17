package org.molgenis.core.ui.settingsmanager;

import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.core.ui.settingsmanager.SettingsManagerController.URI;

@Controller
@RequestMapping(URI)
public class SettingsManagerController extends PluginController
{
	public static final String ID = "settingsmanager";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public SettingsManagerController()
	{
		super(URI);
	}

	@GetMapping
	public String init()
	{
		return "view-settingsmanager";
	}
}
