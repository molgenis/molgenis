package org.molgenis.compute.ui.job;

import java.util.Date;

import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.data.DataService;
import org.molgenis.security.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobServiceImpl implements JobService
{
	private static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

	private final DataService dataService;

	@Autowired
	public JobServiceImpl(DataService dataService)
	{
		this.dataService = dataService;
	}

	@RunAsSystem
	@Override
	@Transactional
	public void updateJobStatus(JobStatusUpdate statusUpdate)
	{
		AnalysisJob job = dataService.findOne(AnalysisJobMetaData.INSTANCE.getName(), statusUpdate.getJobId(),
				AnalysisJob.class);
		if (job == null)
		{
			logger.info("Unknow jobid '" + statusUpdate.getJobId() + "'");
			return;
		}

		job.setStatus(statusUpdate.getStatus());
		switch (job.getStatus())
		{
			case RUNNING:
				job.setStartTime(new Date());
				break;
			case COMPLETED:
			case FAILED:
				job.setOutputMessage(tail50(statusUpdate.getOutputMessage()));
				job.setErrorMessage(tail50(statusUpdate.getErrorMessage()));
				job.setEndTime(new Date());
				break;
			case CREATED:
				break;
			default:
				break;
		}

		dataService.update(AnalysisJobMetaData.INSTANCE.getName(), job);
		logger.info("Job[" + job.getIdentifier() + "] with status [" + job.getStatus() + "] updated in DB" );
	}

	private String tail50(String string)
	{
		String result = "";
		StringBuilder builder = new StringBuilder();
		String lines [] = string.split("\\n");
		if(lines.length > 50)
		{
			for (int i = lines.length - 50; i < lines.length; i++)
				builder.append(lines[i]).append("\n");
			result = builder.toString();
		}
		else
			result = string;
		return result;
	}

}
