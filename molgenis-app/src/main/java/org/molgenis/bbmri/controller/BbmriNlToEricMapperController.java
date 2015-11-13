package org.molgenis.bbmri.controller;

import static java.util.Objects.requireNonNull;
import static org.molgenis.bbmri.controller.BbmriNlToEricMapperController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.molgenis.bbmri.settings.BbmriNlToEricMapperSettings;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class BbmriNlToEricMapperController extends MolgenisPluginController
		implements DisposableBean, ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(BbmriNlToEricMapperController.class);

	public static final String ID = "bbmrinltoericmapper";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DataService dataService;
	private final BbmriNlToEricMapperSettings bbmriNlToEricMapperSettings;
	private final Scheduler scheduler;

	@Autowired
	public BbmriNlToEricMapperController(DataService dataService, Scheduler scheduler,
			BbmriNlToEricMapperSettings bbmriNlToEricMapperSettings)
	{
		super(URI);
		this.dataService = dataService;
		this.scheduler = requireNonNull(scheduler);
		this.bbmriNlToEricMapperSettings = requireNonNull(bbmriNlToEricMapperSettings);
	}

	@RequestMapping(method = GET)
	public String init(Model model) throws Exception
	{
		return "view-bbmrinltoericmapper";
	}

	@RequestMapping(value = "/scheduleMappingJob", method = POST)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public void scheduleMappingJob() throws SchedulerException
	{
		doScheduleMappingJob(createCronTrigger());
	}

	@RequestMapping(value = "/scheduleMappingJobNow", method = POST)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public void scheduleMappingJobNow() throws SchedulerException
	{
		doScheduleMappingJob(null);
	}

	@Override
	public void destroy() throws Exception
	{
		LOG.debug("Stopping scheduler (waiting for jobs to complete) ...");
		boolean waitForJobsToComplete = true;
		scheduler.shutdown(waitForJobsToComplete);
		LOG.info("Scheduler stopped");
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}

	private void doScheduleMappingJob(Trigger trigger) throws SchedulerException
	{
		if (!dataService.hasRepository("bbmri_nl_sample_collections"))
		{
			throw new MolgenisDataException("Missing required source mapping entity [bbmri_nl_sample_collections]");
		}
		if (!dataService.hasRepository("eu_bbmri_eric_collections"))
		{
			throw new MolgenisDataException("Missing required source mapping entity [eu_bbmri_eric_collections]");
		}

		if (bbmriNlToEricMapperSettings.getScheduledMappingEnabled())
		{
			if (scheduler.getJobDetail(new JobKey("bbmrinltoericmapper")) != null)
			{
				throw new MolgenisDataException("Scheduling already enabled.");
			}

			if (trigger != null)
			{
				JobDetail jobDetail = JobBuilder.newJob(BbmriNlToEricMapperJob.class)
						.withIdentity("bbmrinltoericmapper").build();
				scheduler.scheduleJob(jobDetail, trigger);
			}
			else
			{
				scheduler.triggerJob(new JobKey("bbmrinltoericmapper-manual"));
			}
		}
		else
		{
			throw new MolgenisDataException("Scheduling is disabled, change settings to enable scheduling.");
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		try
		{
			doScheduleMappingJob(createCronTrigger());
		}
		catch (Throwable t)
		{
		}
	}

	private Trigger createCronTrigger()
	{
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("System")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 5 0 * * *")).build();
		return trigger;
	}
}
