package org.molgenis.data.index.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static org.testng.Assert.assertEquals;

public class IndexAlreadyExistsExceptionTest extends ExceptionMessageTest
{
	private IndexAlreadyExistsException exception;

	@BeforeMethod
	public void beforeMethod()
	{
		messageSource.addMolgenisNamespaces("index");
		exception = new IndexAlreadyExistsException("molgenis");
	}

	@Test
	public void testGetLocalizedMessageEn()
	{
		LocaleContextHolder.setLocale(ENGLISH);
		assertEquals(exception.getLocalizedMessage(), "Index 'molgenis' already exists");
	}

	@Test
	public void testGetLocalizedMessageNl()
	{
		LocaleContextHolder.setLocale(new Locale("nl"));
		assertEquals(exception.getLocalizedMessage(), "Index 'molgenis' bestaat al");
	}

	@Test
	public void testGetMessage()
	{
		assertEquals(exception.getMessage(), "indexName:molgenis");
	}
}