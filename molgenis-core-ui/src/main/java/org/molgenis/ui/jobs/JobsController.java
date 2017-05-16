package org.molgenis.ui.jobs;

import org.molgenis.auth.User;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.jobs.schedule.JobScheduler;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.SUBMISSION_DATE;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.USER;
import static org.molgenis.data.support.Href.concatEntityHref;
import static org.molgenis.ui.jobs.JobsController.URI;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class JobsController extends MolgenisPluginController
{
	public static final String ID = "jobs";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final int MAX_JOBS_TO_RETURN = 20;

	private final UserAccountService userAccountService;
	private final DataService dataService;
	private final JobExecutionMetaData jobMetaDataMetaData;
	private final JobScheduler jobScheduler;
	private final MenuReaderService menuReaderService;

	@Autowired
	public JobsController(UserAccountService userAccountService, DataService dataService,
			JobExecutionMetaData jobMetaDataMetaData, JobScheduler jobScheduler, MenuReaderService menuReaderService)
	{
		super(URI);
		this.userAccountService = requireNonNull(userAccountService);
		this.dataService = requireNonNull(dataService);
		this.jobMetaDataMetaData = requireNonNull(jobMetaDataMetaData);
		this.jobScheduler = requireNonNull(jobScheduler);
		this.menuReaderService = requireNonNull(menuReaderService);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("username", userAccountService.getCurrentUser().getUsername());
		return "view-jobs";
	}

	@RequestMapping(method = GET, value = "/viewJob")
	public String viewJob(Model model, @RequestParam(name = "jobHref") String jobHref,
			@RequestParam(name = "refreshTimeoutMillis", defaultValue = "10000") Integer refreshTimeoutMillis)
	{
		model.addAttribute("jobHref", jobHref);
		model.addAttribute("refreshTimeoutMillis", refreshTimeoutMillis);
		return "view-job";
	}

	@RequestMapping(method = GET, value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Entity> findLastJobs()
	{
		final List<Entity> jobs = new ArrayList<>();

		Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
		User currentUser = userAccountService.getCurrentUser();

		dataService.getMeta().getEntityTypes()
				.filter(e -> e.getExtends() != null && e.getExtends().getId().equals(jobMetaDataMetaData.getId()))
				.forEach(e ->
				{
					Query<Entity> q = dataService.query(e.getId()).ge(JobExecutionMetaData.SUBMISSION_DATE, weekAgo);
					if (!currentUser.isSuperuser())
					{
						q.and().eq(USER, currentUser.getUsername());
					}
					dataService.findAll(e.getId(), q).forEach(jobs::add);
				});

		Collections.sort(jobs,
				(job1, job2) -> job2.getInstant(SUBMISSION_DATE).compareTo(job1.getInstant(SUBMISSION_DATE)));
		if (jobs.size() > MAX_JOBS_TO_RETURN)
		{
			return jobs.subList(0, MAX_JOBS_TO_RETURN);
		}

		return jobs;
	}

	@RequestMapping(value = "/run/{scheduledJobId}", method = POST)
	@ResponseStatus(NO_CONTENT)
	public void runNow(@PathVariable("scheduledJobId") String scheduledJobId)
	{
		jobScheduler.runNow(scheduledJobId);
	}

	public String createJobExecutionHref(JobExecution jobExecution, int refreshTimeoutMillis) throws IOException
	{
		String jobHref = concatEntityHref("/api/v2", jobExecution.getEntityType().getId(), jobExecution.getIdValue());
		String jobControllerURL = menuReaderService.getMenu().findMenuItemPath(ID);
		return format("{0}/viewJob/?jobHref={1}&refreshTimeoutMillis={2}", jobControllerURL, jobHref,
				refreshTimeoutMillis);
	}
}
