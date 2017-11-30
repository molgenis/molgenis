package org.molgenis.data.security.meta;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EditSystemEntityTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "S03";
	private String operation;
	private EntityType entityType;

	public EditSystemEntityTypeException(String operation, EntityType entityType)
	{
		super(ERROR_CODE);
		this.operation = requireNonNull(operation);
		this.entityType = requireNonNull(entityType);
	}

	public String getOperation()
	{
		return operation;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public String getMessage()
	{
		return String.format("operation:%s entityType:%s", operation, entityType.getId());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, operation, entityType.getLabel());
		}).orElseGet(super::getLocalizedMessage);
	}
}
