package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class peirtje extends CodedRuntimeException
{
	private final static String ERROR_CODE = "R01";
	private Attribute attribute;
	private String type;
	private String[] expectedTypes;

	public peirtje(Attribute attribute, String type, String[] expectedTypes)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.type = type;
		this.expectedTypes = expectedTypes;
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s type:%s, expectedTypes:%s", attribute.getName(), type,
				String.join(",",expectedTypes));
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attribute.getName(), type,
					String.join(",",expectedTypes));
		}).orElse(super.getLocalizedMessage());
	}
}
