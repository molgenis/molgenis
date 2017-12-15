package org.molgenis.i18n.test.exception;

import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.Locale;

public abstract class ExceptionMessageTest extends AbstractMockitoTest
{
	private MessageFormatFactory messageFormatFactory = new MessageFormatFactory();
	protected AllPropertiesMessageSource messageSource;

	@BeforeMethod
	public void exceptionMessageTestBeforeMethod()
	{
		messageSource = new TestAllPropertiesMessageSource(messageFormatFactory);
		MessageSourceHolder.setMessageSource(messageSource);
		LocaleContextHolder.setLocale(Locale.ENGLISH);
	}

	@AfterMethod
	public void exceptionMessageTestAfterMethod()
	{
		MessageSourceHolder.setMessageSource(null);
		LocaleContextHolder.setLocale(null);
	}
}
