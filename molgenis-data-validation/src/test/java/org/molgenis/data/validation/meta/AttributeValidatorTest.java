package org.molgenis.data.validation.meta;

import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.Sort.Direction.ASC;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.testng.Assert.assertEquals;

public class AttributeValidatorTest
{
	private AttributeValidator attributeValidator;
	private DataService dataService;
	private EntityManager entityManager;

	@BeforeMethod
	public void beforeMethod()
	{
		dataService = mock(DataService.class, RETURNS_DEEP_STUBS);
		entityManager = mock(EntityManager.class);
		attributeValidator = new AttributeValidator(dataService, entityManager);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Invalid characters in: \\[invalid.name\\] Only letters \\(a-z, A-Z\\), digits \\(0-9\\), underscores \\(_\\) and hashes \\(#\\) are allowed.")
	public void validateAttributeInvalidName()
	{
		Attribute attr = makeMockAttribute("invalid.name");
		attributeValidator.validate(attr, ValidationMode.ADD);
	}

	@Test
	public void validateMappedByValidEntity()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = makeMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
		attributeValidator.validate(attr, ValidationMode.ADD);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "mappedBy attribute \\[mappedByAttrName\\] is not part of entity \\[entityTypeId\\].")
	public void validateMappedByInvalidEntity()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = makeMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
		attributeValidator.validate(attr, ValidationMode.ADD);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Invalid mappedBy attribute \\[mappedByAttrName\\] data type \\[STRING\\].")
	public void validateMappedByInvalidDataType()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = makeMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(STRING); // invalid type
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
		attributeValidator.validate(attr, ValidationMode.ADD);
	}

	@Test
	public void validateOrderByValid()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = makeMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
		when(attr.getOrderBy()).thenReturn(new Sort(mappedByAttrName, ASC));
		attributeValidator.validate(attr, ValidationMode.ADD);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Unknown entity \\[entityTypeId\\] attribute \\[fail\\] referred to by entity \\[test\\] attribute \\[attrName\\] sortBy \\[fail,ASC\\]")
	public void validateOrderByInvalidRefAttribute()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = makeMockAttribute("attrName");
		EntityType entity = mock(EntityType.class);
		when(entity.getId()).thenReturn("test");
		when(attr.getEntityType()).thenReturn(entity);
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
		when(attr.getOrderBy()).thenReturn(new Sort("fail", ASC));
		attributeValidator.validate(attr, ValidationMode.ADD);
	}

	@Test(dataProvider = "disallowedTransitionProvider", expectedExceptions = MolgenisDataException.class)
	public void testDisallowedTransition(Attribute currentAttr, Attribute newAttr)
	{
		when(dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdentifier(), Attribute.class)).thenReturn(
				currentAttr);
		attributeValidator.validate(newAttr, ValidationMode.UPDATE);
	}

	@Test(dataProvider = "allowedTransitionProvider")
	public void testAllowedTransition(Attribute currentAttr, Attribute newAttr)
	{
		when(dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdentifier(), Attribute.class)).thenReturn(
				currentAttr);
	}

	@Test
	public void testDefaultValueDate()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getDataType()).thenReturn(AttributeType.DATE);
		try
		{
			attributeValidator.validateDefaultValue(attr, true);
			Assert.fail();
		}
		catch (MolgenisDataException actual)
		{
			assertEquals(actual.getCause().getMessage(),
					"Text 'test' could not be parsed, unparsed text found at index 0");
		}
	}

	@Test
	public void testDefaultValueDateValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("2016-01-01");
		when(attr.getDataType()).thenReturn(AttributeType.DATE);
		attributeValidator.validateDefaultValue(attr, true);
	}

	@Test
	public void testDefaultValueDateTime()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getDataType()).thenReturn(AttributeType.DATE_TIME);
		try
		{
			attributeValidator.validateDefaultValue(attr, true);
			Assert.fail();
		}
		catch (MolgenisDataException actual)
		{
			assertEquals(actual.getCause().getMessage(),
					"Text 'test' could not be parsed, unparsed text found at index 0");
		}
	}

	@Test
	public void testDefaultValueDateTimeValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("2016-10-10T12:00:10+0000");
		when(attr.getDataType()).thenReturn(AttributeType.DATE_TIME);
		attributeValidator.validateDefaultValue(attr, true);
	}

	@Test
	public void testDefaultValueHyperlink()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("test^");
		when(attr.getDataType()).thenReturn(AttributeType.HYPERLINK);
		try
		{
			attributeValidator.validateDefaultValue(attr, true);
			Assert.fail();
		}
		catch (MolgenisDataException actual)
		{
			assertEquals(actual.getMessage(), "Default value [test^] is not a valid hyperlink.");
		}
	}

	@Test
	public void testDefaultValueHyperlinkValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("http://www.molgenis.org");
		when(attr.getDataType()).thenReturn(AttributeType.HYPERLINK);
		attributeValidator.validateDefaultValue(attr, true);
	}

	@Test
	public void testDefaultValueEnum()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getEnumOptions()).thenReturn(asList("a", "b", "c"));
		when(attr.getDataType()).thenReturn(AttributeType.ENUM);
		try
		{
			attributeValidator.validateDefaultValue(attr, true);
			Assert.fail();
		}
		catch (MolgenisDataException actual)
		{
			assertEquals(actual.getMessage(),
					"Invalid default value [test] for enum [null] value must be one of [a, b, c]");
		}
	}

	@Test
	public void testDefaultValueEnumValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("b");
		when(attr.getEnumOptions()).thenReturn(asList("a", "b", "c"));
		when(attr.getDataType()).thenReturn(AttributeType.ENUM);
		attributeValidator.validateDefaultValue(attr, true);

	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Invalid default value \\[test\\] for data type \\[INT\\]")
	public void testDefaultValueInt1()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getDataType()).thenReturn(AttributeType.INT);
		attributeValidator.validateDefaultValue(attr, true);
		Assert.fail();
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Invalid default value \\[1.0\\] for data type \\[INT\\]")
	public void testDefaultValueInt2()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("1.0");
		when(attr.getDataType()).thenReturn(AttributeType.INT);
		attributeValidator.validateDefaultValue(attr, true);
		Assert.fail();
	}

	@Test
	public void testDefaultValueIntValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("123456");
		when(attr.getDataType()).thenReturn(AttributeType.INT);
		attributeValidator.validateDefaultValue(attr, true);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Invalid default value \\[test\\] for data type \\[LONG\\]")
	public void testDefaultValueLong()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getDataType()).thenReturn(AttributeType.LONG);
		attributeValidator.validateDefaultValue(attr, true);
	}

	@Test
	public void testDefaultValueLongValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("123456");
		when(attr.getDataType()).thenReturn(AttributeType.LONG);
		attributeValidator.validateDefaultValue(attr, true);
	}

	@Test
	public void testDefaultXrefSkipEntityReferenceValidation()
	{
		String refEntityTypeId = "refEntityTypeId";
		String refIdAttributeName = "refIdAttributeName";

		Attribute refIdAttribute = mock(Attribute.class);
		when(refIdAttribute.getDataType()).thenReturn(STRING);
		when(refIdAttribute.getName()).thenReturn(refIdAttributeName);
		EntityType refEntityType = mock(EntityType.class);

		when(refEntityType.getId()).thenReturn(refEntityTypeId);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("invalidEntityId");
		when(attr.getDataType()).thenReturn(AttributeType.XREF);
		when(attr.getRefEntity()).thenReturn(refEntityType);

		attributeValidator.validateDefaultValue(attr, false);
	}

	@Test
	public void testDefaultXref()
	{
		String refEntityTypeId = "refEntityTypeId";
		String refIdAttributeName = "refIdAttributeName";

		Attribute refIdAttribute = mock(Attribute.class);
		when(refIdAttribute.getDataType()).thenReturn(STRING);
		when(refIdAttribute.getName()).thenReturn(refIdAttributeName);
		EntityType refEntityType = mock(EntityType.class);

		when(refEntityType.getId()).thenReturn(refEntityTypeId);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("entityId");
		when(attr.getDataType()).thenReturn(AttributeType.XREF);
		when(attr.getRefEntity()).thenReturn(refEntityType);

		when(dataService.query(refEntityTypeId).eq(refIdAttributeName, "entityId").count()).thenReturn(1L);
		Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("entityId").getMock();
		when(entityManager.getReference(refEntityType, "entityId")).thenReturn(refEntity);
		attributeValidator.validateDefaultValue(attr, true);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Default value \\[entityId\\] refers to an unknown entity")
	public void testDefaultXrefInvalid()
	{
		String refEntityTypeId = "refEntityTypeId";
		String refIdAttributeName = "refIdAttributeName";

		Attribute refIdAttribute = mock(Attribute.class);
		when(refIdAttribute.getDataType()).thenReturn(STRING);
		when(refIdAttribute.getName()).thenReturn(refIdAttributeName);
		EntityType refEntityType = mock(EntityType.class);

		when(refEntityType.getId()).thenReturn(refEntityTypeId);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("entityId");
		when(attr.getDataType()).thenReturn(AttributeType.XREF);
		when(attr.getRefEntity()).thenReturn(refEntityType);

		when(dataService.query(refEntityTypeId).eq(refIdAttributeName, "entityId").count()).thenReturn(0L);
		Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("entityId").getMock();
		when(entityManager.getReference(refEntityType, "entityId")).thenReturn(refEntity);
		attributeValidator.validateDefaultValue(attr, true);
	}

	@DataProvider(name = "allowedTransitionProvider")
	private static Object[][] allowedTransitionProvider()
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
	private static Object[][] disallowedTransitionProvider()
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

	private static Attribute makeMockAttribute(String name)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getName()).thenReturn(name);
		when(attr.getIdentifier()).thenReturn(name);
		return attr;
	}
}