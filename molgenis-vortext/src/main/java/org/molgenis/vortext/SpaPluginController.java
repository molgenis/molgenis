package org.molgenis.vortext;

import java.util.UUID;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(SpaPluginController.URI)
public class SpaPluginController extends MolgenisPluginController
{
	public static final String ID = "textmining";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String CSRF_TOKEN_MODEL_ATTRIBUTE = "csrf_token";

	public SpaPluginController()
	{
		super(URI);
	}

	@RequestMapping
	public String view(Model model)
	{
		model.addAttribute(CSRF_TOKEN_MODEL_ATTRIBUTE, UUID.randomUUID().toString());
		return "view-spa";
	}
}
