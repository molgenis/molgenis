package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.StaticContentController.URI;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles static content pages requests
 * 
 * RuntimeProperty_[KeyApp] is the way an identifier is made
 */
@Controller
@RequestMapping(URI)
public class StaticContentController extends MolgenisPluginController
{
	/**
	 * TODO before ending this implementation
	 * 
	 * REMOVE: BackgroundController.java; NewsController.java; HomeController.java; ContactController.java; MORE???
	 */

	public static final String URI = "/plugin/staticcontent";

	private static final String DEFAULT_CONTENT = "<p>Place here some content!</p>";

	private static enum KeyApp
	{
		NEWS("app.news"),
		HOME("app.home.html"),
		BACKGROUND("app.background"),
		CONTACT("app.contact"),
		REFERENCES("app.references");

		private String keyApp;

		private KeyApp(String keyApp)
		{
			if (keyApp == null || keyApp.isEmpty())
			{
				throw new IllegalArgumentException("keyApp kan niet null zijn");
			}
			this.keyApp = keyApp;
		}

		public String getKeyApp()
		{
			return this.keyApp;
		}
	}

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public StaticContentController(MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (molgenisSettings == null)
		{
			throw new IllegalArgumentException("molgenisSettings is null");
		}
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(value = "/{uniqueReference}", method = RequestMethod.GET)
	public String init(@PathVariable
	String uniqueReference, Model model)
	{
		// TODO 
		// check permission !!!@££$%^&^**
		KeyApp keyApp = KeyApp.valueOf(uniqueReference.toUpperCase());
		String content = molgenisSettings.getProperty(keyApp.getKeyApp(), DEFAULT_CONTENT);
		model.addAttribute("content", content);
		return "view-staticcontent";
	}
}
