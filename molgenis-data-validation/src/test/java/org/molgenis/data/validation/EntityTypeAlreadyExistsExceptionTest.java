package org.molgenis.data.validation;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EntityTypeAlreadyExistsExceptionTest extends ExceptionMessageTest
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
		assertExceptionMessageEquals(new EntityTypeAlreadyExistsException("MyEntityType"), lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		Object[] enParams = { "en", "Duplicate entity type 'MyEntityType'." };
		Object[] nlParams = { "nl", "Dubbele entiteitsoort 'MyEntityType'." };
		return new Object[][] { enParams, nlParams };
	}
}