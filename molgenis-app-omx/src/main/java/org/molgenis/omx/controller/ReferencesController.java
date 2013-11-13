package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.ReferencesController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles references page requests
 */
@Controller
@RequestMapping(URI)
public class ReferencesController extends AbstractStaticContectController
{
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "references";
	public static final String UNIQUEREFERENCE = "references";
	
	public ReferencesController()
	{
		super(UNIQUEREFERENCE, URI);
	}
}