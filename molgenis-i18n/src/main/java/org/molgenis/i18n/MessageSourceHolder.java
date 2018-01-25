package org.molgenis.i18n;

import org.springframework.context.MessageSource;

public class MessageSourceHolder
{
	private static MessageSource messageSource;

	private MessageSourceHolder()
	{
	}

	public static MessageSource getMessageSource()
	{
		return messageSource;
	}

	public static void setMessageSource(MessageSource messageSource)
	{
		MessageSourceHolder.messageSource = messageSource;
	}
}