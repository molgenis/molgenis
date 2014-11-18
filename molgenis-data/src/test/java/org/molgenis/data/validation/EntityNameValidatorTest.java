package org.molgenis.data.validation;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class EntityNameValidatorTest
{

	@Test
	public void isValid()
	{
		assertTrue(EntityNameValidator.isValid("Test"));
		assertTrue(EntityNameValidator.isValid("123"));
		assertFalse(EntityNameValidator.isValid(""));
		assertFalse(EntityNameValidator.isValid(" "));
		assertFalse(EntityNameValidator.isValid("test."));
		assertFalse(EntityNameValidator.isValid("tes t"));
		assertFalse(EntityNameValidator.isValid("test&"));
	}
}
