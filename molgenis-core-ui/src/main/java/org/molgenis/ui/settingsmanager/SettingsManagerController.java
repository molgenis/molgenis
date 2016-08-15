package org.molgenis.ui.settingsmanager;

import org.molgenis.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.ui.settingsmanager.SettingsManagerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class SettingsManagerController extends MolgenisPluginController
{
	public static final String ID = "settingsmanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public SettingsManagerController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-settingsmanager";
	}
}
