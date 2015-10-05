package org.molgenis.app.promise;

public class MappingReport
{
	public enum Status
	{
		UNKNOWN, SUCCESS, ERROR
	}

	private String projectName;
	private Status status;
	private String message;

	public MappingReport()
	{
		this.status = Status.UNKNOWN;
	}

	public String getProjectName()
	{
		return projectName;
	}

	public void setProjectName(String projectName)
	{
		this.projectName = projectName;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
