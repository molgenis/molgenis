package org.molgenis.data.security.exception;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.molgenis.data.security.EntityPermission.DELETE;

@Test
public class EntityPermissionExceptionTest extends ExceptionMessageTest
{
	@Mock
	private EntityType entityType;
	@Mock
	private Entity entity;

	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("data-security");
		when(entity.getIdValue()).thenReturn("book");
		when(entity.getEntityType()).thenReturn(entityType);
		when(entityType.getLabel("en")).thenReturn("Books");
	}

	@Test(dataProvider = "languageMessageProvider")
	@Override
	public void testGetLocalizedMessage(String lang, String message)
	{
		assertExceptionMessageEquals(new EntityPermissionDeniedException(DELETE, entity), lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		Object[] enParams = { "en", "No 'Delete' permission on entity 'book' of type 'Books'" };
		return new Object[][] { enParams };
	}
}