package org.molgenis.compute.ui.model;

import java.util.Date;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.data.support.MapEntity;

public class Analysis extends MapEntity
{
	private static final long serialVersionUID = 406768955023996782L;

	public Analysis()
	{
		super(AnalysisMetaData.IDENTIFIER);
	}

	public Analysis(String identifier, String name)
	{
		this();
		set(AnalysisMetaData.IDENTIFIER, identifier);
		setName(name);
	}

	public String getIdentifier()
	{
		return getString(AnalysisMetaData.IDENTIFIER);
	}

	public String getName()
	{
		return getString(AnalysisMetaData.NAME);
	}

	public void setName(String name)
	{
		set(AnalysisMetaData.NAME, name);
	}

	public String getDescription()
	{
		return getString(AnalysisMetaData.DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(AnalysisMetaData.DESCRIPTION, description);
	}

	public Date getCreationDate()
	{
		return getUtilDate(AnalysisMetaData.CREATION_DATE);
	}

	public void setCreationDate(Date creationDate)
	{
		set(AnalysisMetaData.CREATION_DATE, creationDate);
	}

	public UIWorkflow getWorkflow()
	{
		return getEntity(AnalysisMetaData.WORKFLOW, UIWorkflow.class);
	}

	public void setWorkflow(UIWorkflow workflow)
	{
		set(AnalysisMetaData.WORKFLOW, workflow);
	}

	public List<AnalysisJob> getJobs()
	{
		Iterable<AnalysisJob> jobs = getEntities(AnalysisMetaData.JOBS, AnalysisJob.class);
		if (jobs == null) return Lists.newArrayList();
		return Lists.newArrayList(jobs);
	}

	public void setJobs(List<AnalysisJob> jobs)
	{
		set(AnalysisMetaData.JOBS, jobs);
	}

	public UIBackend getBackend()
	{
		return getEntity(AnalysisMetaData.BACKEND, UIBackend.class);
	}

	public void setBackend(UIBackend backend)
	{
		set(AnalysisMetaData.BACKEND, backend);
	}

	public String getSubmitScript()
	{
		return getString(AnalysisMetaData.SUBMIT_SCRIPT);
	}

	public void setSubmitScript(String submitScript)
	{
		set(AnalysisMetaData.SUBMIT_SCRIPT, submitScript);
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
		for (AnalysisJob job : getJobs())
		{
			if ((job.getWorkflowNode() != null) && job.getWorkflowNode().getIdentifier().equals(nodeId))
			{
				count++;
			}
		}

		return count;
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
		for (AnalysisJob job : getJobs())
		{
			if ((job.getWorkflowNode() != null) && (job.getStatus() == status)
					&& job.getWorkflowNode().getIdentifier().equals(nodeId))
			{
				count++;
			}
		}

		return count;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdentifier().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		return getIdentifier().equals(((Analysis) obj).getIdentifier());
	}
}
