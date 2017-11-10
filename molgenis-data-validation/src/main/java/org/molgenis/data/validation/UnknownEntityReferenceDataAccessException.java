package org.molgenis.data.validation;

import org.molgenis.data.MolgenisDataAccessException;

import static java.util.Objects.requireNonNull;

public class UnknownEntityReferenceDataAccessException extends MolgenisDataAccessException
{
	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public UnknownEntityReferenceDataAccessException(String entityTypeId, String attributeName, String valueAsString)
	{

		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.valueAsString = requireNonNull(valueAsString);
	}

	public String getEntityTypeId()
	{
		return entityTypeId;
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	public String getValueAsString()
	{
		return valueAsString;
	}
}
