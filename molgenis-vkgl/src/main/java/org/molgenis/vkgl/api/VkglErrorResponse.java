package org.molgenis.vkgl.api;

import org.molgenis.util.ErrorMessageResponse.ErrorMessage;

public class VkglErrorResponse
{
	private String message;

	public VkglErrorResponse(ErrorMessage errorMessage)
	{
		message =  errorMessage.getMessage();
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
