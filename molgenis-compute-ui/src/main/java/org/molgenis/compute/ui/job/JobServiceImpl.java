package org.molgenis.compute.ui.job;

import java.util.Date;

import org.apache.log4j.Logger;
import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.data.DataService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService
{
	private static Logger logger = Logger.getLogger(JobServiceImpl.class);
	private final DataService dataService;

	@Autowired
	public JobServiceImpl(DataService dataService)
	{
		this.dataService = dataService;
	}

	@RunAsSystem
	@Override
	public void updateJobStatus(JobStatusUpdate statusUpdate)
	{
		AnalysisJob job = dataService.findOne(AnalysisJobMetaData.INSTANCE.getName(), statusUpdate.getJobId(),
				AnalysisJob.class);
		if (job == null)
		{
			logger.info("Unknow jobid '" + statusUpdate.getJobId() + "'");
			return;
		}

		logger.info("Job[" + job.getIdentifier() + "] with status [" + job.getStatus() +
				"] got new status [" + statusUpdate.getStatus() + "]");
		job.setStatus(statusUpdate.getStatus());
		job.setOutputMessage(statusUpdate.getOutputMessage());

		switch (job.getStatus())
		{
			case RUNNING:
				job.setStartTime(new Date());
				break;
			case COMPLETED:
			case FAILED:
				job.setEndTime(new Date());
				break;
			case CREATED:
				break;
			default:
				break;
		}
		logger.info("Job[" + job.getIdentifier() + "] with status [" + job.getStatus() + "] updated in DB" );
		dataService.update(AnalysisJobMetaData.INSTANCE.getName(), job);
	}

}
