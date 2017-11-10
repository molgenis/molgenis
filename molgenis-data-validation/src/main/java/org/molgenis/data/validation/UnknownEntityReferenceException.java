package org.molgenis.data.validation;

import org.molgenis.util.LocalizedRuntimeException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class UnknownEntityReferenceException extends LocalizedRuntimeException
{
	private static final String BUNDLE_ID = "data_validation";
	private static final String ERROR_CODE = "V01";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public UnknownEntityReferenceException(String entityTypeId, String attributeName, String valueAsString)
	{
		super(BUNDLE_ID, ERROR_CODE);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.valueAsString = requireNonNull(valueAsString);
	}

	@Override
	protected String createMessage()
	{
		return "type:" + entityTypeId + " attribute:" + attributeName + " value:" + valueAsString;
	}

	@Override
	protected String createLocalizedMessage(String messageFormat)
	{
		return format(messageFormat, valueAsString, attributeName, entityTypeId);
	}
}
