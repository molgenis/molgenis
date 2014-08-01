package org.molgenis.omx.controller;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles feedback page requests.
 */
@Controller
@RequestMapping(FeedbackController.URI)
public class FeedbackController extends MolgenisPluginController
{
	public static final String ID = "feedback";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	
	public FeedbackController()
	{
		super(URI);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String init(final Model model)
	{
		model.addAttribute("adminEmail", "molgenis@molgenis.org");
		return "view-feedback";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String submitFeedback(final Model model)
	{
		model.addAttribute("submitted", Boolean.TRUE);
		return "view-feedback";
	}
}
