package org.molgenis.app.controller;

import static org.molgenis.app.controller.ContactController.URI;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.controller.AbstractStaticContentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles contact page requests
 */
@Controller
@RequestMapping(URI)
public class ContactController extends AbstractStaticContentController
{
	public static final String ID = "contact";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	
	public ContactController()
	{
		super(ID, URI);
	}
}
