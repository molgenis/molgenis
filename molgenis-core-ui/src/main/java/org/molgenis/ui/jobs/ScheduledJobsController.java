package org.molgenis.ui.jobs;

import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(ScheduledJobsController.URI)
public class ScheduledJobsController extends MolgenisPluginController
{
	public static final String ID = "scheduledjobs";
	public static final String URI = PLUGIN_URI_PREFIX + ID;

	@Autowired
	public ScheduledJobsController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-scheduled-jobs";
	}
}
