package org.molgenis.ui.jobs;

import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.ui.jobs.JobsController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.jobs.JobMetaData;
import org.molgenis.data.jobs.JobMetaDataMetaData;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class JobsController extends MolgenisPluginController
{
	public static final String ID = "jobs";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static int MAX_JOBS_TO_RETURN = 20;

	private UserAccountService userAccountService;
	private DataService dataService;
	private JobMetaDataMetaData jobMetaDataMetaData;

	@Autowired
	public JobsController(UserAccountService userAccountService, DataService dataService,
			JobMetaDataMetaData jobMetaDataMetaData)
	{
		super(URI);
		this.userAccountService = requireNonNull(userAccountService);
		this.dataService = requireNonNull(dataService);
		this.jobMetaDataMetaData = requireNonNull(jobMetaDataMetaData);
	}

	public JobsController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("username", userAccountService.getCurrentUser().getUsername());
		return "view-jobs";
	}

	@RequestMapping(method = GET, value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<JobMetaData> findLastJobs()
	{
		final List<JobMetaData> jobs = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		Date weekAgo = cal.getTime();
		MolgenisUser currentUser = userAccountService.getCurrentUser();

		stream(dataService.getMeta().getEntityMetaDatas().spliterator(), false)
				.filter(e -> e.getExtends() != null && e.getExtends().getName().equals(jobMetaDataMetaData.getName()))
				.forEach(e -> {
					Query q = dataService.query(e.getName()).ge(JobMetaData.SUBMISSION_DATE, weekAgo);
					if (!currentUser.isSuperuser())
					{
						q.and().eq(JobMetaData.USER, currentUser);
					}
					dataService.findAll(e.getName(), q, JobMetaData.class).forEach(jobs::add);
				});

		sort(jobs);
		if (jobs.size() > MAX_JOBS_TO_RETURN)
		{
			return jobs.subList(0, MAX_JOBS_TO_RETURN);
		}

		return jobs;
	}
}
