package org.molgenis.data.security.meta;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EditSystemAttributeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "S02";
	private String operation;
	private Attribute attr;

	public EditSystemAttributeException(String operation, Attribute attr)
	{
		super(ERROR_CODE);
		this.operation = requireNonNull(operation);
		this.attr = requireNonNull(attr);
	}

	@Override
	public String getMessage()
	{
		return String.format("operation:%s attr:%s", operation, attr.getName());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, operation, attr.getLabel());
		}).orElse(super.getLocalizedMessage());
	}
}
