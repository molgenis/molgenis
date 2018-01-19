package org.molgenis.data.validation;

import org.molgenis.data.RepositoryCapability;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnsupportedRepositoryCollectionCapabilityExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("data_validation");
	}

	@Test(dataProvider = "languageMessageProvider")
	@Override
	public void testGetLocalizedMessage(String lang, String message)
	{
		assertExceptionMessageEquals(new UnsupportedRepositoryCollectionCapabilityException("MyRepositoryCollection",
				RepositoryCapability.WRITABLE), lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		Object[] enParams = { "en", "Entity type 'MyRepositoryCollection' is not 'WRITABLE'." };
		Object[] nlParams = { "nl", "Entiteitsoort 'MyRepositoryCollection' is niet 'WRITABLE'." };
		return new Object[][] { enParams, nlParams };
	}
}