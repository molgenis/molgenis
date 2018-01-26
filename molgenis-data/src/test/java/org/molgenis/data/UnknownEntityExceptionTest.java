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
		when(entityType.getLabel(lang)).thenReturn("Entity Type label " + lang);
		assertExceptionMessageEquals(new UnknownEntityException(entityType, "MyEntity"), lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		Object[] enParams = { "en", "Unknown entity 'MyEntity' of entity type 'Entity Type label en'." };
		Object[] nlParams = { "nl", "Onbekende entiteit 'MyEntity' van entiteitsoort 'Entity Type label nl'." };
		return new Object[][] { enParams, nlParams };
	}
}