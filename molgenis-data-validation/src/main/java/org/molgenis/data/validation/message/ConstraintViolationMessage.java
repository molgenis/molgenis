package org.molgenis.data.validation.message;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConstraintViolationMessage
{
	public abstract String getErrorCode();

	public abstract String getMessage();

	public abstract String getLocalizedMessage();

	public static ConstraintViolationMessage create(String newErrorCode, String newMessage, String newLocalizedMessage)
	{
		return new AutoValue_ConstraintViolationMessage(newErrorCode, newMessage, newLocalizedMessage);
	}
}
