package org.molgenis.data.validation;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class ValidationMessage implements Serializable
{
	private static final long serialVersionUID = 1L;

	public abstract String getErrorCode();

	public abstract String getMessage();

	public abstract String getLocalizedMessage();

	public static ValidationMessage create(String newErrorCode, String newMessage, String newLocalizedMessage)
	{
		return new AutoValue_ValidationMessage(newErrorCode, newMessage, newLocalizedMessage);
	}
}
