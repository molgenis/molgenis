package org.molgenis.data.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.testng.annotations.Test;

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

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testReservedKeywordMysqlLowerCase()
	{
		MetaValidationUtils.validateName("select");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testReservedKeywordMysqlUpperCase()
	{
		MetaValidationUtils.validateName("SELECT");
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

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataTooLong()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");

		List<AttributeMetaData> compAttrs = new ArrayList<>();
		compAttrs.add(new DefaultAttributeMetaData("aCompStringWayTooLongToUseAsAnAttributeName1")
				.setDataType(MolgenisFieldTypes.STRING));
		compAttrs.add(new DefaultAttributeMetaData("aCompString2").setDataType(MolgenisFieldTypes.STRING));
		emd.addAttribute("aComp").setDataType(MolgenisFieldTypes.COMPOUND).setAttributesMetaData(compAttrs);
		emd.addAttribute("aString", ROLE_ID);

		MetaValidationUtils.validateEntityMetaData(emd);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataStartsWithDigit()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");

		List<AttributeMetaData> compAttrs = new ArrayList<>();
		compAttrs.add(new DefaultAttributeMetaData("aCompString1").setDataType(MolgenisFieldTypes.STRING));
		compAttrs.add(new DefaultAttributeMetaData("2aCompString").setDataType(MolgenisFieldTypes.STRING));
		emd.addAttribute("aComp").setDataType(MolgenisFieldTypes.COMPOUND).setAttributesMetaData(compAttrs);
		emd.addAttribute("aString", ROLE_ID);

		MetaValidationUtils.validateEntityMetaData(emd);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataInvalidChar()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");

		List<AttributeMetaData> compAttrs = new ArrayList<>();
		compAttrs.add(new DefaultAttributeMetaData("aCompString1").setDataType(MolgenisFieldTypes.STRING));
		compAttrs.add(new DefaultAttributeMetaData("aCompString2").setDataType(MolgenisFieldTypes.STRING));
		emd.addAttribute("a.Comp").setDataType(MolgenisFieldTypes.COMPOUND).setAttributesMetaData(compAttrs);
		emd.addAttribute("aString", ROLE_ID);

		MetaValidationUtils.validateEntityMetaData(emd);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataIdAttributeWithDefaultValue()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");
		emd.addAttribute("id", ROLE_ID).setDefaultValue("5");

		MetaValidationUtils.validateEntityMetaData(emd);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataUniqueAttributeWithDefaultValue()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");
		emd.addAttribute("id", ROLE_ID);
		emd.addAttribute("uniqueAttribute").setUnique(true).setDefaultValue("5");

		MetaValidationUtils.validateEntityMetaData(emd);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityComputedAttributeWithDefaultValue()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");
		emd.addAttribute("id", ROLE_ID);
		emd.addAttribute("expressionAttribute").setExpression("$('id').value()").setDefaultValue("5");

		MetaValidationUtils.validateEntityMetaData(emd);
	}

	@Test
	public void testValidateEntityMetaDataOkayAttributeWithDefaultValue()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");
		emd.addAttribute("id", ROLE_ID);
		emd.addAttribute("blah").setDefaultValue("5");

		MetaValidationUtils.validateEntityMetaData(emd);
	}
}
