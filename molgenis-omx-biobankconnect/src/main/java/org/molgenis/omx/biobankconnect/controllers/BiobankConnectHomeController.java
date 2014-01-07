package org.molgenis.omx.biobankconnect.controllers;

import static org.molgenis.omx.biobankconnect.controllers.BiobankConnectHomeController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.wizard.BiobankConnectController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(URI)
public class BiobankConnectHomeController extends MolgenisPluginController
{
	public static final String URI = BiobankConnectController.URI + "/home";

	public BiobankConnectHomeController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		return "BiobankConnectHome";
	}
}
