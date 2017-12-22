package org.molgenis.data.importer.wizard;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class EntityTypeNotImportableExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("core-ui");
	}

	@Test
	public void testGetMessage()
	{
		assertEquals(new EntityTypeNotImportableException(asList("EntityType0", "EntityType1")).getMessage(),
				"ids:EntityType0,EntityType1");
	}

	@Test
	public void testGetLocalizedMessageArguments()
	{
		assertEquals(new EntityTypeNotImportableException(asList("EntityType0", "EntityType1")).getLocalizedMessage(),
				"You are trying to upload entities that are not compatible with the already existing entities: EntityType0, EntityType1.");
	}
}