package org.molgenis.compute.ui.analysis;

import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.compute.ui.model.JobStatus;

public class AnalysisJobCount
{
	private final Iterable<AnalysisJob> analysisJobs;

	public AnalysisJobCount(Iterable<AnalysisJob> analysisJobs)
	{
		this.analysisJobs = analysisJobs;
	}

	/**
	 * Get the nr of jobs generated for a WorkflowNode
	 *
	 * @param nodeId
	 * @return
	 */
	public int getTotalJobCount(String nodeId)
	{
		int count = 0;
		for (AnalysisJob job : analysisJobs)
		{
			if ((job.getWorkflowNode() != null) && job.getWorkflowNode().getIdentifier().equals(nodeId))
			{
				count++;
			}
		}

		return count;
	}

	public int getScheduledJobCount(String nodeId)
	{
		return getJobCount(nodeId, JobStatus.SCHEDULED);
	}

	public int getRunningJobCount(String nodeId)
	{
		return getJobCount(nodeId, JobStatus.RUNNING);
	}

	public int getCompletedJobCount(String nodeId)
	{
		return getJobCount(nodeId, JobStatus.COMPLETED);
	}

	public int getFailedJobCount(String nodeId)
	{
		return getJobCount(nodeId, JobStatus.FAILED);
	}

	private int getJobCount(String nodeId, JobStatus status)
	{
		int count = 0;
		for (AnalysisJob job : analysisJobs)
		{
			if ((job.getWorkflowNode() != null) && (job.getStatus() == status)
					&& job.getWorkflowNode().getIdentifier().equals(nodeId))
			{
				count++;
			}
		}

		return count;
	}
}