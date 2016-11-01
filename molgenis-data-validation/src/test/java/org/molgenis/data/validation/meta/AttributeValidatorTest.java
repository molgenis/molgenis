package org.molgenis.data.validation.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.molgenis.AttributeType.STRING;
import static org.molgenis.AttributeType.XREF;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;

public class AttributeValidatorTest
{
	private AttributeValidator attributeValidator;
	private DataService dataService;

	@BeforeMethod
	public void beforeMethod()
	{
		dataService = mock(DataService.class);
		attributeValidator = new AttributeValidator(dataService);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Invalid characters in: \\[3attr.name\\] Only letters \\(a-z, A-Z\\), digits \\(0-9\\), underscores \\(_\\) and hashes \\(#\\) are allowed.")
	public void validateAttributeInvalidName()
	{
		Attribute attr = makeMockAttribute("invalid.name");
		attributeValidator.validate(attr);
	}

	@Test
	public void validateMappedByValidEntity()
	{
		String entityName = "entityName";
		EntityType refEntity = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		Attribute attr = makeMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
		attributeValidator.validate(attr);
		verify(dataService, times(1)).findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(), Attribute.class);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "mappedBy attribute \\[mappedByAttrName\\] is not part of entity \\[entityName\\].")
	public void validateMappedByInvalidEntity()
	{
		String entityName = "entityName";
		EntityType refEntity = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		Attribute attr = makeMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
		attributeValidator.validate(attr);
		verify(dataService, times(1)).findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(), Attribute.class);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Invalid mappedBy attribute \\[mappedByAttrName\\] data type \\[STRING\\].")
	public void validateMappedByInvalidDataType()
	{
		String entityName = "entityName";
		EntityType refEntity = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		Attribute attr = makeMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(STRING); // invalid type
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
		attributeValidator.validate(attr);
		verify(dataService, times(1)).findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(), Attribute.class);
	}

	private Attribute makeMockAttribute(String name)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getName()).thenReturn(name);
		when(attr.getIdentifier()).thenReturn("1");
		return attr;
	}
}