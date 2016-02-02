package org.molgenis.ui.jobs;

import static org.molgenis.ui.jobs.JobsController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class JobsController extends MolgenisPluginController
{
	public static final String ID = "jobs";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	UserAccountService userAccountService;
	
	public JobsController()
	{
		super(URI);	
	}
	
	@RequestMapping(method = GET)
	public String init(Model model) {
		model.addAttribute("username", userAccountService.getCurrentUser().getUsername());
		return "view-jobs";
	}
}
