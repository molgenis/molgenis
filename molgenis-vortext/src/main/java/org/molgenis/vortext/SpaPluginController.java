package org.molgenis.vortext;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(SpaPluginController.URI)
public class SpaPluginController extends MolgenisPluginController
{
	public static final String ID = "textmining";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public SpaPluginController()
	{
		super(URI);
	}

	@RequestMapping
	public String view()
	{
		return "view-spa";
	}
}
