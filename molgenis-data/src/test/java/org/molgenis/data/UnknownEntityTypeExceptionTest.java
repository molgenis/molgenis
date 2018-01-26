package org.molgenis.data;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownEntityTypeExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("data");
	}

	@Test(dataProvider = "languageMessageProvider")
	@Override
	public void testGetLocalizedMessage(String lang, String message)
	{
		assertExceptionMessageEquals(new UnknownEntityTypeException("MyEntityType"), lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		Object[] enParams = { "en", "Unknown entity type 'MyEntityType'." };
		Object[] nlParams = { "nl", "Onbekende entiteitsoort 'MyEntityType'." };
		return new Object[][] { enParams, nlParams };
	}
}