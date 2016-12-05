package org.molgenis.data.meta;

import org.molgenis.data.MolgenisDataException;
import org.testng.annotations.Test;

public class NameValidatorTest
{
	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameTooLong()
	{
		NameValidator.validateName("ThisNameIsTooLongToUseAsAnAttributeName");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameInvalidCharacters()
	{
		NameValidator.validateName("Invalid.Name");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameStartsWithDigit()
	{
		NameValidator.validateName("6invalid");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testReservedKeyword()
	{
		NameValidator.validateName("base");
	}

	@Test
	public void testI18nName()
	{
		NameValidator.validateName("test-en");
		NameValidator.validateName("test-eng");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nNameMilti()
	{
		NameValidator.validateName("test-en-nl");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nTooLong()
	{
		NameValidator.validateName("test-xxxx");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nMissing()
	{
		NameValidator.validateName("test-");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nUpperCase()
	{
		NameValidator.validateName("test-NL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nNumber()
	{
		NameValidator.validateName("test-n2");
	}
}
