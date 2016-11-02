package org.molgenis.data.validation.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.molgenis.AttributeType.*;
import static org.molgenis.data.Sort.Direction.ASC;
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

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Invalid characters in: \\[invalid.name\\] Only letters \\(a-z, A-Z\\), digits \\(0-9\\), underscores \\(_\\) and hashes \\(#\\) are allowed.")
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

	@Test
	public void validateOrderByValid()
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
		when(attr.getOrderBy()).thenReturn(new Sort(mappedByAttrName, ASC));
		attributeValidator.validate(attr);
		verify(dataService, times(1)).findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(), Attribute.class);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Unknown entity \\[entityName\\] attribute \\[fail\\] referred to by entity \\[test\\] attribute \\[attrName\\] sortBy \\[fail,ASC\\]")
	public void validateOrderByInvalidRefAttribute()
	{
		String entityName = "entityName";
		EntityType refEntity = when(mock(EntityType.class).getName()).thenReturn(entityName).getMock();
		Attribute attr = makeMockAttribute("attrName");
		EntityType entity = mock(EntityType.class);
		when(entity.getName()).thenReturn("test");
		when(attr.getEntityType()).thenReturn(entity);
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
		when(attr.getOrderBy()).thenReturn(new Sort("fail", ASC));
		attributeValidator.validate(attr);
		verify(dataService, times(1)).findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(), Attribute.class);
	}

	@Test(dataProvider = "disallowedTransitionProvider", expectedExceptions = MolgenisDataException.class)
	public void testDisallowedTransition(Attribute currentAttr, Attribute newAttr)
	{
		when(dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdentifier(), Attribute.class))
				.thenReturn(currentAttr);
		attributeValidator.validate(newAttr);
	}

	@Test(dataProvider = "allowedTransitionProvider")
	public void testAllowedTransition(Attribute currentAttr, Attribute newAttr)
	{
		when(dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdentifier(), Attribute.class))
				.thenReturn(currentAttr);
	}

	@DataProvider(name = "allowedTransitionProvider")
	private Object[][] allowedTransitionProvider()
	{
		Attribute currentAttr1 = makeMockAttribute("attr1");
		Attribute currentAttr2 = makeMockAttribute("attr2");
		Attribute currentAttr3 = makeMockAttribute("attr3");
		when(currentAttr1.getDataType()).thenReturn(BOOL);
		when(currentAttr2.getDataType()).thenReturn(CATEGORICAL);
		when(currentAttr3.getDataType()).thenReturn(COMPOUND);

		Attribute newAttr1 = makeMockAttribute("attr1");
		Attribute newAttr2 = makeMockAttribute("attr2");
		Attribute newAttr3 = makeMockAttribute("attr3");
		when(newAttr1.getDataType()).thenReturn(INT);
		when(newAttr2.getDataType()).thenReturn(INT);
		when(newAttr3.getDataType()).thenReturn(INT);

		return new Object[][] { { currentAttr1, newAttr1 }, { currentAttr2, newAttr2 }, { currentAttr3, newAttr3 } };
	}

	@DataProvider(name = "disallowedTransitionProvider")
	private Object[][] disallowedTransitionProvider()
	{
		Attribute currentAttr1 = makeMockAttribute("attr1");
		Attribute currentAttr2 = makeMockAttribute("attr2");
		Attribute currentAttr3 = makeMockAttribute("attr3");
		when(currentAttr1.getDataType()).thenReturn(BOOL);
		when(currentAttr2.getDataType()).thenReturn(CATEGORICAL);
		when(currentAttr3.getDataType()).thenReturn(COMPOUND);

		Attribute newAttr1 = makeMockAttribute("attr1");
		Attribute newAttr2 = makeMockAttribute("attr2");
		Attribute newAttr3 = makeMockAttribute("attr3");
		when(newAttr1.getDataType()).thenReturn(ONE_TO_MANY);
		when(newAttr2.getDataType()).thenReturn(HYPERLINK);
		when(newAttr3.getDataType()).thenReturn(FILE);

		return new Object[][] { { currentAttr1, newAttr1 }, { currentAttr2, newAttr2 }, { currentAttr3, newAttr3 } };
	}

	private Attribute makeMockAttribute(String name)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getName()).thenReturn(name);
		when(attr.getIdentifier()).thenReturn(name);
		return attr;
	}
}