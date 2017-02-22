package org.molgenis.data.meta;

import org.molgenis.data.MolgenisDataException;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class NameValidatorTest
{
	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameTooLong()
	{
		NameValidator.validateEntityOrPackageName("ThisNameIsTooLongToUseAsAnAttributeName");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameInvalidCharacters()
	{
		NameValidator.validateEntityOrPackageName("Invalid.Name");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameStartsWithDigit()
	{
		NameValidator.validateEntityOrPackageName("6invalid");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testReservedKeyword()
	{
		NameValidator.validateEntityOrPackageName("base");
	}


	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nNameMilti()
	{
		NameValidator.validateEntityOrPackageName("test-en-nl");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nTooLong()
	{
		NameValidator.validateEntityOrPackageName("test-xxxx");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nMissing()
	{
		NameValidator.validateEntityOrPackageName("test-");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nUpperCase()
	{
		NameValidator.validateEntityOrPackageName("test-NL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nNumber()
	{
		NameValidator.validateEntityOrPackageName("test-n2");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testUnderscore()
	{
		NameValidator.validateEntityOrPackageName("test_test");
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
