package org.molgenis.data.meta;

import org.molgenis.data.MolgenisDataException;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class NameValidatorTest
{
	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameTooLong()
	{
		NameValidator.validateEntityName("ThisNameIsTooLongToUseAsAnAttributeName");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameInvalidCharacters()
	{
		NameValidator.validateEntityName("Invalid.Name");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameStartsWithDigit()
	{
		NameValidator.validateEntityName("6invalid");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testReservedKeyword()
	{
		NameValidator.validateEntityName("base");
	}


	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nNameMilti()
	{
		NameValidator.validateEntityName("test-en-nl");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nTooLong()
	{
		NameValidator.validateEntityName("test-xxxx");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nMissing()
	{
		NameValidator.validateEntityName("test-");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nUpperCase()
	{
		NameValidator.validateEntityName("test-NL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nNumber()
	{
		NameValidator.validateEntityName("test-n2");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testUnderscore()
	{
		NameValidator.validateEntityName("test_test");
	}

	@Test
	public void testI18nName()
	{
		NameValidator.validateAttributeName("test-en");
		NameValidator.validateAttributeName("test-eng");
	}

	@Test
	public void testUnderscoreAttr()
	{
		NameValidator.validateAttributeName("test_test");
	}

}
