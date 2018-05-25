package org.molgenis.data;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class UnknownEntityExceptionTest extends ExceptionMessageTest
{
	@Mock
	private EntityType entityType;

	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("data");
	}

	@Test(dataProvider = "languageMessageProvider")
	@Override
	public void testGetLocalizedMessage(String lang, String message)
	{
		when(entityType.getLabel("en")).thenReturn("Books");
		assertExceptionMessageEquals(new UnknownEntityException(entityType, 5), lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		return new Object[][] { new Object[] { "en", "Unknown entity '5' of type 'Books'." } };
	}

	@Test(dataProvider = "languageMessageProviderIdOnly")
	public void testGetLocalizedMessageIdOnly(String lang, String message)
	{
		assertExceptionMessageEquals(new UnknownEntityException("org_example_Books", 5), lang, message);
	}

	@DataProvider(name = "languageMessageProviderIdOnly")
	public Object[][] languageMessageProviderIdOnly()
	{
		return new Object[][] { new Object[] { "en", "Unknown entity '5' of type 'org_example_Books'." } };
	}
}