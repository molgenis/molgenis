package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.NewsController.URI;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
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
@Deprecated
public class NewsController extends MolgenisPluginController
{
	public static final String URI = "/plugin/news";

	private static final String DEFAULT_KEY_APP_NEWS = "<p>Paste here some news !</p>";
	private static final String KEY_APP_NEWS = "app.news";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public NewsController(MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		String homeHtml = molgenisSettings.getProperty(KEY_APP_NEWS, DEFAULT_KEY_APP_NEWS);
		model.addAttribute(KEY_APP_NEWS.replace('.', '_'), homeHtml);
		return "view-news";
	}
}
