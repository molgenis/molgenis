package org.molgenis.data.meta;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;

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

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataTooLong()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getSimpleName()).thenReturn("entity");
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("aString").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttrPart0 = when(mock(AttributeMetaData.class).getName())
				.thenReturn("aCompStringWayTooLongToUseAsAnAttributeName1").getMock();
		when(compoundAttrPart0.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttrPart1 = when(mock(AttributeMetaData.class).getName()).thenReturn("aCompString2")
				.getMock();
		when(compoundAttrPart1.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("aComp").getMock();
		when(compoundAttr.getDataType()).thenReturn(COMPOUND);
		when(compoundAttr.getAttributeParts()).thenReturn(asList(compoundAttrPart0, compoundAttrPart1));
		when(entityMeta.getAttributes()).thenReturn(asList(idAttr, compoundAttr));
		MetaValidationUtils.validateEntityMetaData(entityMeta);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataStartsWithDigit()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getSimpleName()).thenReturn("entity");
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("aString").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttrPart0 = when(mock(AttributeMetaData.class).getName()).thenReturn("aCompString1")
				.getMock();
		when(compoundAttrPart0.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttrPart1 = when(mock(AttributeMetaData.class).getName()).thenReturn("2aCompString")
				.getMock();
		when(compoundAttrPart1.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("aComp").getMock();
		when(compoundAttr.getDataType()).thenReturn(COMPOUND);
		when(compoundAttr.getAttributeParts()).thenReturn(asList(compoundAttrPart0, compoundAttrPart1));
		when(entityMeta.getAttributes()).thenReturn(asList(idAttr, compoundAttr));

		MetaValidationUtils.validateEntityMetaData(entityMeta);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataInvalidChar()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getSimpleName()).thenReturn("entity");
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("aString").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttrPart0 = when(mock(AttributeMetaData.class).getName()).thenReturn("aCompString1")
				.getMock();
		when(compoundAttrPart0.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttrPart1 = when(mock(AttributeMetaData.class).getName()).thenReturn("aCompString2")
				.getMock();
		when(compoundAttrPart1.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("a.Comp").getMock();
		when(compoundAttr.getDataType()).thenReturn(COMPOUND);
		when(compoundAttr.getAttributeParts()).thenReturn(asList(compoundAttrPart0, compoundAttrPart1));
		when(entityMeta.getAttributes()).thenReturn(asList(idAttr, compoundAttr));

		MetaValidationUtils.validateEntityMetaData(entityMeta);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataIdAttributeWithDefaultValue()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getSimpleName()).thenReturn("entity");
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.getDefaultValue()).thenReturn("5");
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAttributes()).thenReturn(singletonList(idAttr));
		MetaValidationUtils.validateEntityMetaData(entityMeta);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityMetaDataUniqueAttributeWithDefaultValue()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getSimpleName()).thenReturn("entity");
		AttributeMetaData uniqueAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("uniqueAttr").getMock();
		when(uniqueAttr.isUnique()).thenReturn(true);
		when(uniqueAttr.getDefaultValue()).thenReturn("5");
		when(entityMeta.getAttributes()).thenReturn(singletonList(uniqueAttr));
		MetaValidationUtils.validateEntityMetaData(entityMeta);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityComputedAttributeWithDefaultValue()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getSimpleName()).thenReturn("entity");
		AttributeMetaData expressionAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("expressionAttr")
				.getMock();
		when(expressionAttr.getExpression()).thenReturn("$('id').value()");
		when(expressionAttr.getDefaultValue()).thenReturn("5");
		when(entityMeta.getAttributes()).thenReturn(singletonList(expressionAttr));
		MetaValidationUtils.validateEntityMetaData(entityMeta);
	}

	@Test
	public void testValidateEntityMetaDataOkayAttributeWithDefaultValue()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getSimpleName()).thenReturn("entity");
		AttributeMetaData attrWithDefaultValue = when(mock(AttributeMetaData.class).getName())
				.thenReturn("attrWithDefaultValue").getMock();
		when(attrWithDefaultValue.getDataType()).thenReturn(STRING);
		when(attrWithDefaultValue.getDefaultValue()).thenReturn("5");
		when(entityMeta.getAttributes()).thenReturn(singletonList(attrWithDefaultValue));

		MetaValidationUtils.validateEntityMetaData(entityMeta);
	}
}
