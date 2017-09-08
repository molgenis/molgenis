package org.molgenis.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorMessageResponse
{
	private List<ErrorMessage> errors;

	public ErrorMessageResponse()
	{
	}

	public ErrorMessageResponse(ErrorMessage errorMessage)
	{
		if (errorMessage == null) throw new IllegalArgumentException("error message is null");
		addErrorMessage(errorMessage);
	}

	public ErrorMessageResponse(List<ErrorMessage> errorMessages)
	{
		if (errorMessages == null) throw new IllegalArgumentException("error messages is null");
		addErrorMessages(errorMessages);
	}

	public List<ErrorMessage> getErrors()
	{
		return errors != null ? errors : Collections.emptyList();
	}

	public void addErrorMessage(ErrorMessage errorMessage)
	{
		if (this.errors == null) errors = new ArrayList<>();
		this.errors.add(errorMessage);
	}

	public void addErrorMessages(List<ErrorMessage> errorMessages)
	{
		if (this.errors == null) errors = new ArrayList<>();
		this.errors.addAll(errorMessages);
	}

	public static ErrorMessageResponse create(String errorMessage)
	{
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(errorMessage));
	}

	public static class ErrorMessage
	{
		private static final String DEFAULT_ERROR_MESSAGE = "Unknown error";

		private final String message;
		private final Integer code;

		public ErrorMessage(String message)
		{
			this(message, null);
		}

		public ErrorMessage(String message, Integer code)
		{
			this.message = message != null ? message : DEFAULT_ERROR_MESSAGE;
			this.code = code;
		}

		public String getMessage()
		{
			return message;
		}

		public Integer getCode()
		{
			return code;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((code == null) ? 0 : code.hashCode());
			result = prime * result + ((message == null) ? 0 : message.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ErrorMessage other = (ErrorMessage) obj;
			if (code == null)
			{
				if (other.code != null) return false;
			}
			else if (!code.equals(other.code)) return false;
			if (message == null)
			{
				if (other.message != null) return false;
			}
			else if (!message.equals(other.message)) return false;
			return true;
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errors == null) ? 0 : errors.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ErrorMessageResponse other = (ErrorMessageResponse) obj;
		if (errors == null)
		{
			if (other.errors != null) return false;
		}
		else if (!errors.equals(other.errors)) return false;
		return true;
	}
}
