package org.molgenis.compute.ui.job;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.Part;

import org.molgenis.compute.ui.model.JobStatus;
import org.molgenis.security.runas.SystemSecurityToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/job")
public class JobStatusController
{
	private static Logger logger = LoggerFactory.getLogger(JobStatusController.class);

	private final BlockingQueue<JobStatusUpdate> queue = new LinkedBlockingQueue<>();
	private final QueueHandler queueHandler = new QueueHandler();
	private final JobService jobService;

	@Autowired
	public JobStatusController(JobService jobService)
	{
		this.jobService = jobService;
		new Thread(queueHandler).start();
	}

	@ResponseBody
	@RequestMapping(value = "/{jobid}/status", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public void updateJobStatus(@PathVariable("jobid") String jobId, @RequestParam("status") JobStatus status,
			@RequestParam(value = "outLogFile", required = false) Part outLogFile,
			@RequestParam(value = "errLogFile", required = false) Part errLogFile)
	{
		JobStatusUpdate statusUpdate = new JobStatusUpdate(jobId, status);

		logger.info("Job [" + jobId + "] reported status [" + status + "]");

		if (outLogFile != null)
		{
			try
			{
				statusUpdate.setOutputMessage(FileCopyUtils.copyToString(new InputStreamReader(outLogFile
						.getInputStream())));
			}
			catch (IOException e)
			{
				logger.error("Exception reading outLogFile", e);
			}
		}

		if (outLogFile != null)
		{
			try
			{
				statusUpdate.setErrorMessage(FileCopyUtils.copyToString(new InputStreamReader(errLogFile
						.getInputStream())));
			}
			catch (IOException e)
			{
				logger.error("Exception reading errLogFile", e);
			}
		}


		try
		{
			queue.put(statusUpdate);
		}
		catch (Exception e)
		{
			logger.error("Error putting status update for job '" + jobId + "' in queue", e);
		}
	}

	private class QueueHandler implements Runnable
	{
		@Override
		public void run()
		{

				while (true)
				{
					JobStatusUpdate statusUpdate = null;
					try
					{
						statusUpdate = queue.take();
						jobService.updateJobStatus(statusUpdate);
					} catch (Throwable t)
					{
						logger.error("Error updating jobStatus [" + statusUpdate + "]", t);
					}
				}


		}

	}
}
