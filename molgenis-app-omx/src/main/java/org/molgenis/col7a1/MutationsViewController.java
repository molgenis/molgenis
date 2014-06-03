package org.molgenis.col7a1;

import static org.molgenis.col7a1.MutationsViewController.URI;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(URI)
public class MutationsViewController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(MutationsViewController.class);
	public static final String ID = "col7a1_mutations";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public MutationsViewController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "view-col7a1-mutations";
	}
}
