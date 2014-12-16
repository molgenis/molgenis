package org.molgenis.compute.ui.job;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/job")
public class JobStatusController
{
	private static Logger logger = Logger.getLogger(JobStatusController.class);
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
	@RequestMapping(value = "/{jobid}/status", method = RequestMethod.POST)
	public void updateJobStatus(@PathVariable("jobid") String jobId, @Valid JobStatusUpdate statusUpdate)
	{

		statusUpdate.setJobId(jobId);

		try
		{
			queue.put(statusUpdate);
		}
		catch (InterruptedException e)
		{
			logger.error("Error putting status update for job '" + jobId + "' in queue", e);
		}

	}

	private class QueueHandler implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					JobStatusUpdate statusUpdate = queue.take();
					jobService.updateJobStatus(statusUpdate);
				}

			}
			catch (InterruptedException e)
			{
				logger.error("Exception taking statusUpdate from queue", e);
			}

		}

	}
}
