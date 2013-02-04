package org.molgenis.framework.server.services;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.services.SchedulingService;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

/**
 * A MolgenisService to clean the tmp dir every hour. Files older than 12 hours
 * are attempted to be deleted. The handleRequest of this service should never
 * be called, instead it is just initialized and uses the FrontController
 * scheduler to start the Quartz cleaning job. This job will be triggered every
 * hour.
 * 
 * Does not work.
 * 
 * Made using this example: http://www.mkyong.com/java/quartz-scheduler-example/
 * 
 * @author joerivandervelde
 * 
 */
public class MolgenisCleanTmpDirServiceQuartz implements MolgenisService
{
	Logger logger = Logger.getLogger(MolgenisCleanTmpDirServiceQuartz.class);

	public MolgenisCleanTmpDirServiceQuartz(MolgenisContext mc)
	{
		try
		{

			CleanTmpDirTask task = new CleanTmpDirTask();

			// specify your sceduler task details
			JobDetail job = new JobDetail();
			job.setName("cleanTmpDirJob");
			job.setJobClass(CleanTmpDirJob.class);

			@SuppressWarnings("unchecked")
			Map<String, CleanTmpDirTask> dataMap = job.getJobDataMap();
			dataMap.put("cleanTmpDirTask", task);

			// configure the scheduler time
			SimpleTrigger trigger = new SimpleTrigger();
			trigger.setName("runMeJobTesting");
			trigger.setStartTime(new Date(System.currentTimeMillis() + 1000));
			trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
			trigger.setRepeatInterval(30000);

			Trigger t = TriggerUtils.makeMinutelyTrigger();
			t.setStartTime(new Date());
			t.setName("MolgenisCleanTmpDirServiceTrigger");

			// schedule it
			SchedulingService schedulingService = mc.getSchedulingService();
			// Scheduler scheduler = mc.getScheduler();

			// if(!scheduler.isStarted())
			// {
			// throw new SchedulerException("Scheduler is not active");
			// }
			//
			// scheduler.start();
			// scheduler.scheduleJob(job, t);
			HashMap<Object, Object> dataMap2 = new HashMap<Object, Object>();
			// dataMap2.put("cleanTmpDirTask", task);
			schedulingService.scheduleOnce(dataMap2, CleanTmpDirJob.class);
			// schedulingService.shutdown();

			System.out.println("MolgenisCleanTmpDirService initialized.");

		}
		catch (SchedulerException e)
		{
			System.err.println("FATAL EXCEPTION: failure in starting MolgenisCleanTmpDirService.");
			e.printStackTrace();
			System.exit(0);
		}

	}

	@Override
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws ParseException,
			DatabaseException, IOException
	{
		throw new IOException("This service does not accept requests.");
	}
}

class CleanTmpDirJob implements Job
{
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		@SuppressWarnings("unchecked")
		Map<String, CleanTmpDirTask> dataMap = context.getJobDetail().getJobDataMap();
		CleanTmpDirTask task = dataMap.get("cleanTmpDirTask");
		try
		{
			task.cleanTmpDir();
		}
		catch (InterruptedException e)
		{
			throw new JobExecutionException(e);
		}
	}
}

class CleanTmpDirTask
{

	public void cleanTmpDir() throws InterruptedException
	{
		// delete all files in tmpdir older than 12 hours
		System.out.println("MolgenisCleanTmpDirService: executing cleaning job!");

		String tmpDirLoc = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tmpDirLoc);

		long curDate = new Date().getTime();
		long twelveHours = 1000 * 60 * 60 * 12;

		for (File f : tmpDir.listFiles())
		{
			// TODO: directory recursion..
			if (!f.isDirectory())
			{
				long lastMod = f.lastModified();
				long age = lastMod - curDate;

				if (age > twelveHours)
				{
					System.out.println(f.getAbsolutePath() + " is older than 12 hrs, deleting...");
					FileUtils.deleteQuietly(f);
				}
				else
				{
					System.out.println(f.getAbsolutePath() + " is younger than 12 hrs");
				}
			}
		}
	}
}
