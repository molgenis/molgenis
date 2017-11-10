package org.molgenis.data;

import org.molgenis.util.LocalizedRuntimeException;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class UnknownEntityTypeException extends LocalizedRuntimeException
{
	private static final String BUNDLE_ID = "data";
	private static final String ERROR_CODE = "D02";

	private final String entityTypeId;

	public UnknownEntityTypeException(String entityTypeId)
	{
		super(BUNDLE_ID, ERROR_CODE);
		this.entityTypeId = requireNonNull(entityTypeId);
	}

	@Override
	protected String createMessage()
	{
		return format("id:%s", entityTypeId);
	}

	@Override
	protected String createLocalizedMessage(ResourceBundle resourceBundle, Locale locale)
	{
		return format(resourceBundle.getString("unknown_entity_type"), entityTypeId);
	}
}

