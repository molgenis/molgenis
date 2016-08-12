package org.molgenis.app.controller;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.controller.AbstractStaticContentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.app.controller.ReferencesController.URI;

/**
 * Controller that handles references page requests
 */
@Controller
@RequestMapping(URI)
public class ReferencesController extends AbstractStaticContentController
{
	public static final String ID = "references";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public ReferencesController()

	{
		super(ID, URI);
	}
}
