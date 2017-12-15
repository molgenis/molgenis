package org.molgenis.i18n.test.exception;

import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

public class TestAllPropertiesMessageSource extends AllPropertiesMessageSource
{
	private final MessageFormatFactory messageFormatFactory;

	public TestAllPropertiesMessageSource(MessageFormatFactory messageFormatFactory)
	{
		this.messageFormatFactory = Objects.requireNonNull(messageFormatFactory);
	}

	@Override
	protected MessageFormat createMessageFormat(String msg, Locale locale)
	{
		return messageFormatFactory.createMessageFormat(msg, locale);
	}
}
