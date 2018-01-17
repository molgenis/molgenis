package org.molgenis.core.ui.jobs;

import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ScheduledJobsController.URI)
public class ScheduledJobsController extends PluginController
{
	public static final String ID = "scheduledjobs";
	public static final String URI = PLUGIN_URI_PREFIX + ID;

	public ScheduledJobsController()
	{
		super(URI);
	}

	@GetMapping
	public String init()
	{
		return "view-scheduled-jobs";
	}
}
