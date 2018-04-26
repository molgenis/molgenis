package org.molgenis.data.transaction;

import org.molgenis.data.UnknownRepositoryCollectionException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownRepositoryCollectionExceptionTest extends ExceptionMessageTest
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
		assertExceptionMessageEquals(new UnknownRepositoryCollectionException("MyRepositoryCollection"), lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		Object[] enParams = { "en", "Unknown repository collection 'MyRepositoryCollection'." };
		Object[] nlParams = { "nl", "Onbekende opslagplaats verzameling 'MyRepositoryCollection'." };
		return new Object[][] { enParams, nlParams };
	}
}