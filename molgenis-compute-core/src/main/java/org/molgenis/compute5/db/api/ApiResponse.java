package org.molgenis.compute5.db.api;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ApiResponse
{
	private String errorMessage;

	public ApiResponse()
	{
		super();
	}

	public ApiResponse(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}
