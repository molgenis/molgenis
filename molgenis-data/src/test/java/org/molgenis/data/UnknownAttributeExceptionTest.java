package org.molgenis.data;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UnknownAttributeExceptionTest extends ExceptionMessageTest
{
	@Mock
	private EntityType entityType;

	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("data");
		when(entityType.getLabel(any())).then((invocation ->
		{
			String language = invocation.getArgument(0);
			return language.equals("en") ? "MyEntityType" : "MijnEntiteitSoort";
		}));
	}

	@Test(dataProvider = "languageMessageProvider")
	@Override
	public void testGetLocalizedMessage(String lang, String message)
	{
		assertExceptionMessageEquals(new UnknownAttributeException(entityType, "MyAttribute"), lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		Object[] enParams = { "en", "Unknown attribute 'MyAttribute' of entity type 'MyEntityType'." };
		Object[] nlParams = { "nl", "Onbekend attribuut 'MyAttribute' van entiteitsoort 'MijnEntiteitSoort'." };
		return new Object[][] { enParams, nlParams };
	}
}