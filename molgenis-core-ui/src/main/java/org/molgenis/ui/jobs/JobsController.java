package org.molgenis.ui.jobs;

import org.molgenis.auth.User;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.SUBMISSION_DATE;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.USER;
import static org.molgenis.ui.jobs.JobsController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class JobsController extends MolgenisPluginController
{
	public static final String ID = "jobs";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static int MAX_JOBS_TO_RETURN = 20;

	private UserAccountService userAccountService;
	private DataService dataService;
	private JobExecutionMetaData jobMetaDataMetaData;

	@Autowired
	public JobsController(UserAccountService userAccountService, DataService dataService,
			JobExecutionMetaData jobMetaDataMetaData)
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
	public List<Entity> findLastJobs()
	{
		final List<Entity> jobs = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		Date weekAgo = cal.getTime();
		User currentUser = userAccountService.getCurrentUser();

		dataService.getMeta().getEntityTypes()
				.filter(e -> e.getExtends() != null && e.getExtends().getFullyQualifiedName().equals(jobMetaDataMetaData.getFullyQualifiedName()))
				.forEach(e ->
				{
					Query<Entity> q = dataService.query(e.getFullyQualifiedName()).ge(JobExecutionMetaData.SUBMISSION_DATE, weekAgo);
					if (!currentUser.isSuperuser())
					{
						q.and().eq(USER, currentUser.getUsername());
					}
					dataService.findAll(e.getFullyQualifiedName(), q).forEach(jobs::add);
				});

		Collections.sort(jobs,
				(job1, job2) -> job2.getUtilDate(SUBMISSION_DATE).compareTo(job1.getUtilDate(SUBMISSION_DATE)));
		if (jobs.size() > MAX_JOBS_TO_RETURN)
		{
			return jobs.subList(0, MAX_JOBS_TO_RETURN);
		}

		return jobs;
	}
}
