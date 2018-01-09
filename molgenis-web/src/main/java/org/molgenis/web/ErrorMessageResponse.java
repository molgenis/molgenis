package org.molgenis.web;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
		return create(errorMessage, null);
	}

	public static ErrorMessageResponse create(String errorMessage, @Nullable String errorCode)
	{
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(errorMessage, errorCode));
	}

	public static class ErrorMessage
	{
		private static final String DEFAULT_ERROR_MESSAGE = "Unknown error";

		private final String message;
		private final String code;

		public ErrorMessage(String message)
		{
			this(message, null);
		}

		public ErrorMessage(String message, String code)
		{
			this.message = message != null ? message : DEFAULT_ERROR_MESSAGE;
			this.code = code;
		}

		public String getMessage()
		{
			return message;
		}

		public String getCode()
		{
			return code;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ErrorMessage that = (ErrorMessage) o;
			return Objects.equals(message, that.message) && Objects.equals(code, that.code);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(message, code);
		}

		@Override
		public String toString()
		{
			return "ErrorMessage{" + "message='" + message + '\'' + ", code='" + code + '\'' + '}';
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ErrorMessageResponse that = (ErrorMessageResponse) o;
		return Objects.equals(errors, that.errors);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(errors);
	}

	@Override
	public String toString()
	{
		return "ErrorMessageResponse{" + "errors=" + errors + '}';
	}
}
