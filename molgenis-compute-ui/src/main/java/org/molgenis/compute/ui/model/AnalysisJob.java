package org.molgenis.compute.ui.model;

import java.util.Date;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.data.support.MapEntity;

public class AnalysisJob extends MapEntity
{
	private static final long serialVersionUID = -8201683162211699942L;

	public AnalysisJob()
	{
		super(AnalysisJobMetaData.IDENTIFIER);
		// TODO workaround for #1810 'EMX misses DefaultValue'
		setStatus(AnalysisJobMetaData.STATUS_DEFAULT);
	}

	public AnalysisJob(String identifier)
	{
		this();
		set(AnalysisJobMetaData.IDENTIFIER, identifier);
	}

	public String getIdentifier()
	{
		return getString(AnalysisJobMetaData.IDENTIFIER);
	}

	public String getSchedulerId()
	{
		return getString(AnalysisJobMetaData.SCHEDULER_ID);
	}

	public void setSchedulerId(String schedulerId)
	{
		set(AnalysisJobMetaData.SCHEDULER_ID, schedulerId);
	}

	public UIWorkflowNode getWorkflowNode()
	{
		return getEntity(AnalysisJobMetaData.WORKFLOW_NODE, UIWorkflowNode.class);
	}

	public void setWorkflowNode(UIWorkflowNode workflowNode)
	{
		set(AnalysisJobMetaData.WORKFLOW_NODE, workflowNode);
	}

	public String getGenerateScript()
	{
		return getString(AnalysisJobMetaData.GENERATED_SCRIPT);
	}

	public void setGeneratedScript(String generatedScript)
	{
		set(AnalysisJobMetaData.GENERATED_SCRIPT, generatedScript);
	}

	public JobStatus getStatus()
	{
		String status = getString(AnalysisJobMetaData.STATUS);
		if (status == null)
		{
			// TODO workaround for #1810 'EMX misses DefaultValue'
			return AnalysisJobMetaData.STATUS_DEFAULT;
		}

		return JobStatus.valueOf(status);
	}

	public void setStatus(JobStatus status)
	{
		set(AnalysisJobMetaData.STATUS, status.toString());
	}

	public Date getStartTime()
	{
		return getUtilDate(AnalysisJobMetaData.START_TIME);
	}

	public void setStartTime(Date startTime)
	{
		set(AnalysisJobMetaData.START_TIME, startTime);
	}

	public Date getEndTime()
	{
		return getUtilDate(AnalysisJobMetaData.END_TIME);
	}

	public void setEndTime(Date endTime)
	{
		set(AnalysisJobMetaData.END_TIME, endTime);
	}

	public String getErrorMessage()
	{
		return getString(AnalysisJobMetaData.ERROR_MESSAGE);
	}

	public void setErrorMessage(String errorMessage)
	{
		set(AnalysisJobMetaData.ERROR_MESSAGE, errorMessage);
	}

	public String getOutputMessage()
	{
		return getString(AnalysisJobMetaData.OUTPUT_MESSAGE);
	}

	public void setOutputMessage(String outputMessage)
	{
		set(AnalysisJobMetaData.OUTPUT_MESSAGE, outputMessage);
	}

	public String getName()
	{
		return getString(AnalysisJobMetaData.NAME);
	}

	public void setName(String name)
	{
		set(AnalysisJobMetaData.NAME, name);
	}

	public List<UIParameterValue> getParameters()
	{
		Iterable<UIParameterValue> params = getEntities(AnalysisJobMetaData.PARAMETER_VALUES, UIParameterValue.class);
		if (params == null) return Lists.newArrayList();
		return Lists.newArrayList(params);
	}

	public void setParameters(List<UIParameterValue> params)
	{
		set(AnalysisJobMetaData.PARAMETER_VALUES, params);
	}

	public Analysis getAnalysis()
	{
		return getEntity(AnalysisJobMetaData.ANALYSIS, Analysis.class);
	}

	public void setAnalysis(Analysis analysis)
	{
		set(AnalysisJobMetaData.ANALYSIS, analysis);
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

		return getIdentifier().equals(((AnalysisJob) obj).getIdentifier());
	}
}
