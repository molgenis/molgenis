package org.molgenis.app.controller;

import org.molgenis.core.ui.controller.AbstractStaticContentController;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.app.controller.ContactController.URI;

/**
 * Controller that handles contact page requests
 */
@Controller
@RequestMapping(URI)
public class ContactController extends AbstractStaticContentController
{
	public static final String ID = "contact";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public ContactController()
	{
		super(ID, URI);
	}
}
