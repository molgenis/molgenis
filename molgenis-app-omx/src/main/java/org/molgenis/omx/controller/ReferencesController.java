package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.ReferencesController.URI;

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
public class ReferencesController extends MolgenisPluginController
{
	public static final String URI = "/plugin/references";

	private static final String DEFAULT_KEY_APP_CONTACT = "<p>Paste the code</p>";
	private static final String KEY_APP_CONTACT = "app.references";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public ReferencesController(MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		String homeHtml = molgenisSettings.getProperty(KEY_APP_CONTACT, DEFAULT_KEY_APP_CONTACT);
		model.addAttribute(KEY_APP_CONTACT.replace('.', '_'), homeHtml);
		return "view-references";
	}
}
