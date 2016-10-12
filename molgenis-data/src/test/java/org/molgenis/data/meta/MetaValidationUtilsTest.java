package org.molgenis.data.meta;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
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
	public void testValidateEntityTypeTooLong()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("aString").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute compoundAttrPart0 = when(mock(Attribute.class).getName())
				.thenReturn("aCompStringWayTooLongToUseAsAnAttributeName1").getMock();
		when(compoundAttrPart0.getDataType()).thenReturn(STRING);
		Attribute compoundAttrPart1 = when(mock(Attribute.class).getName()).thenReturn("aCompString2").getMock();
		when(compoundAttrPart1.getDataType()).thenReturn(STRING);
		Attribute compoundAttr = when(mock(Attribute.class).getName()).thenReturn("aComp").getMock();
		when(compoundAttr.getDataType()).thenReturn(COMPOUND);
		when(compoundAttr.getAttributeParts()).thenReturn(asList(compoundAttrPart0, compoundAttrPart1));
		when(entityType.getAttributes()).thenReturn(asList(idAttr, compoundAttr));
		MetaValidationUtils.validateEntityType(entityType);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityTypeStartsWithDigit()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("aString").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute compoundAttrPart0 = when(mock(Attribute.class).getName()).thenReturn("aCompString1").getMock();
		when(compoundAttrPart0.getDataType()).thenReturn(STRING);
		Attribute compoundAttrPart1 = when(mock(Attribute.class).getName()).thenReturn("2aCompString").getMock();
		when(compoundAttrPart1.getDataType()).thenReturn(STRING);
		Attribute compoundAttr = when(mock(Attribute.class).getName()).thenReturn("aComp").getMock();
		when(compoundAttr.getDataType()).thenReturn(COMPOUND);
		when(compoundAttr.getAttributeParts()).thenReturn(asList(compoundAttrPart0, compoundAttrPart1));
		when(entityType.getAttributes()).thenReturn(asList(idAttr, compoundAttr));

		MetaValidationUtils.validateEntityType(entityType);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityTypeInvalidChar()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("aString").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute compoundAttrPart0 = when(mock(Attribute.class).getName()).thenReturn("aCompString1").getMock();
		when(compoundAttrPart0.getDataType()).thenReturn(STRING);
		Attribute compoundAttrPart1 = when(mock(Attribute.class).getName()).thenReturn("aCompString2").getMock();
		when(compoundAttrPart1.getDataType()).thenReturn(STRING);
		Attribute compoundAttr = when(mock(Attribute.class).getName()).thenReturn("a.Comp").getMock();
		when(compoundAttr.getDataType()).thenReturn(COMPOUND);
		when(compoundAttr.getAttributeParts()).thenReturn(asList(compoundAttrPart0, compoundAttrPart1));
		when(entityType.getAttributes()).thenReturn(asList(idAttr, compoundAttr));

		MetaValidationUtils.validateEntityType(entityType);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityTypeIdAttributeWithDefaultValue()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.getDefaultValue()).thenReturn("5");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttributes()).thenReturn(singletonList(idAttr));
		MetaValidationUtils.validateEntityType(entityType);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityTypeUniqueAttributeWithDefaultValue()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		Attribute uniqueAttr = when(mock(Attribute.class).getName()).thenReturn("uniqueAttr").getMock();
		when(uniqueAttr.isUnique()).thenReturn(true);
		when(uniqueAttr.getDefaultValue()).thenReturn("5");
		when(entityType.getAttributes()).thenReturn(singletonList(uniqueAttr));
		MetaValidationUtils.validateEntityType(entityType);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testValidateEntityComputedAttributeWithDefaultValue()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		Attribute expressionAttr = when(mock(Attribute.class).getName()).thenReturn("expressionAttr").getMock();
		when(expressionAttr.getExpression()).thenReturn("$('id').value()");
		when(expressionAttr.getDefaultValue()).thenReturn("5");
		when(entityType.getAttributes()).thenReturn(singletonList(expressionAttr));
		MetaValidationUtils.validateEntityType(entityType);
	}

	@Test
	public void testValidateEntityTypeOkayAttributeWithDefaultValue()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getSimpleName()).thenReturn("entity");
		Attribute attrWithDefaultValue = when(mock(Attribute.class).getName()).thenReturn("attrWithDefaultValue")
				.getMock();
		when(attrWithDefaultValue.getDataType()).thenReturn(STRING);
		when(attrWithDefaultValue.getDefaultValue()).thenReturn("5");
		when(entityType.getAttributes()).thenReturn(singletonList(attrWithDefaultValue));

		MetaValidationUtils.validateEntityType(entityType);
	}
}
