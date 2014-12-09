package org.molgenis.compute.ui.workflow;

import org.hibernate.validator.constraints.NotEmpty;

public class ImportWorkflowForm
{
	@NotEmpty
	private String path;

	@NotEmpty
	private String workflowFileName;

	@NotEmpty
	private String[] parametersFileName;

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getWorkflowFileName()
	{
		return workflowFileName;
	}

	public void setWorkflowFileName(String workflowFileName)
	{
		this.workflowFileName = workflowFileName;
	}

	public String[] getParametersFileName()
	{
		return parametersFileName;
	}

	public void setParametersFileName(String[] parametersFileName)
	{
		this.parametersFileName = parametersFileName;
	}

}
