package org.molgenis.data.meta;

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

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataTooLong()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");

		List<AttributeMetaData> compAttrs = new ArrayList<>();
		compAttrs.add(new DefaultAttributeMetaData("aCompStringWayTooLongToUseAsAnAttributeName1")
				.setDataType(MolgenisFieldTypes.STRING));
		compAttrs.add(new DefaultAttributeMetaData("aCompString2").setDataType(MolgenisFieldTypes.STRING));
		emd.addAttribute("aComp").setDataType(MolgenisFieldTypes.COMPOUND).setAttributesMetaData(compAttrs);
		emd.addAttribute("aString").setDataType(MolgenisFieldTypes.STRING).setIdAttribute(true);

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
		emd.addAttribute("aString").setDataType(MolgenisFieldTypes.STRING).setIdAttribute(true);

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
		emd.addAttribute("aString").setDataType(MolgenisFieldTypes.STRING).setIdAttribute(true);

		MetaValidationUtils.validateEntityMetaData(emd);
	}
}
