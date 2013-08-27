package org.molgenis.ngs.controller;

import static org.molgenis.ngs.controller.HomeController.URI;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends MolgenisPluginController
{
	public static final String URI = "/plugin/home";

	private static final String DEFAULT_KEY_APP_HOME_HTML = "<p>Welcome to Molgenis!</p>";
	private static final String KEY_APP_HOME_HTML = "app.home.html";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public HomeController(MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		String homeHtml = molgenisSettings.getProperty(KEY_APP_HOME_HTML, DEFAULT_KEY_APP_HOME_HTML);
		model.addAttribute(KEY_APP_HOME_HTML.replace('.', '_'), homeHtml);
		return "view-home";
	}
}
