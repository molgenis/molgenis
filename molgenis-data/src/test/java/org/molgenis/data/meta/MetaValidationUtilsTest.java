package org.molgenis.data.meta;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.AttributeType.COMPOUND;
import static org.molgenis.AttributeType.STRING;

public class MetaValidationUtilsTest
{
	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameTooLong()
	{
		MetaValidationUtils.validateName("ThisNameIsTooLongToUseAsAnAttributeName");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameInvalidCharacters()
	{
		MetaValidationUtils.validateName("Invalid.Name");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateNameStartsWithDigit()
	{
		MetaValidationUtils.validateName("6invalid");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testReservedKeyword()
	{
		MetaValidationUtils.validateName("implements");
	}

	@Test
	public void testI18nName()
	{
		MetaValidationUtils.validateName("test-en");
		MetaValidationUtils.validateName("test-eng");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nNameMilti()
	{
		MetaValidationUtils.validateName("test-en-nl");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nTooLong()
	{
		MetaValidationUtils.validateName("test-xxxx");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nMissing()
	{
		MetaValidationUtils.validateName("test-");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nUpperCase()
	{
		MetaValidationUtils.validateName("test-NL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testI18nNumber()
	{
		MetaValidationUtils.validateName("test-n2");
	}
}
