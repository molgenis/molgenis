package org.molgenis.data.validation.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.constraint.AttributeConstraintViolation;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.Sort.Direction.ASC;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.validation.constraint.AttributeConstraint.*;
import static org.testng.Assert.assertEquals;

// TODO check return value of attr validator in all tests
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

	@Test
	public void validateAttributeInvalidName()
	{
		Attribute attr = createMockAttribute("invalid.name");
		assertEquals(attributeValidator.validate(attr, ValidationMode.ADD),
				singletonList(new AttributeConstraintViolation(NAME, attr)));
	}

	@Test
	public void validateMappedByValidEntity()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = createMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
		assertEquals(attributeValidator.validate(attr, ValidationMode.ADD), emptySet());
	}

	@Test
	public void validateMappedByInvalidEntity()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = createMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
		assertEquals(attributeValidator.validate(attr, ValidationMode.ADD),
				singletonList(new AttributeConstraintViolation(MAPPED_BY_REFERENCE, attr)));
	}

	@Test
	public void validateMappedByInvalidDataType()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = createMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(STRING); // invalid type
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
		assertEquals(attributeValidator.validate(attr, ValidationMode.ADD),
				singletonList(new AttributeConstraintViolation(MAPPED_BY_TYPE, attr)));
	}

	@Test
	public void validateOrderByValid()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = createMockAttribute("attrName");
		when(attr.getRefEntity()).thenReturn(refEntity);
		String mappedByAttrName = "mappedByAttrName";
		Attribute mappedByAttr = when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
		when(mappedByAttr.getDataType()).thenReturn(XREF);
		when(attr.getMappedBy()).thenReturn(mappedByAttr);
		when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
		when(attr.getOrderBy()).thenReturn(new Sort(mappedByAttrName, ASC));
		assertEquals(attributeValidator.validate(attr, ValidationMode.ADD), emptyList());
	}

	@Test
	public void validateOrderByInvalidRefAttribute()
	{
		String entityTypeId = "entityTypeId";
		EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute attr = createMockAttribute("attrName");
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
		assertEquals(attributeValidator.validate(attr, ValidationMode.ADD),
				singletonList(new AttributeConstraintViolation(ORDER_BY_REFERENCE, attr)));
	}

	@Test(dataProvider = "disallowedTransitionProvider")
	public void testDisallowedTransition(Attribute currentAttr, Attribute newAttr)
	{
		when(dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdentifier(), Attribute.class)).thenReturn(
				currentAttr);
		assertEquals(attributeValidator.validate(newAttr, ValidationMode.UPDATE),
				singletonList(new AttributeConstraintViolation(TYPE_UPDATE, newAttr)));
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
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getDataType()).thenReturn(AttributeType.DATE);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()),
				singletonList(new AttributeConstraintViolation(DEFAULT_VALUE_TYPE, attr)));
	}

	@Test
	public void testDefaultValueDateValid()
	{
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("2016-01-01");
		when(attr.getDataType()).thenReturn(AttributeType.DATE);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()), emptyList());
	}

	@Test
	public void testDefaultValueDateTime()
	{
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getDataType()).thenReturn(AttributeType.DATE_TIME);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()),
				singletonList(new AttributeConstraintViolation(DEFAULT_VALUE_TYPE, attr)));
	}

	@Test
	public void testDefaultValueDateTimeValid()
	{
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("2016-10-10T12:00:10+0000");
		when(attr.getDataType()).thenReturn(AttributeType.DATE_TIME);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()), emptyList());
	}

	@Test
	public void testDefaultValueHyperlink()
	{
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("test^");
		when(attr.getDataType()).thenReturn(AttributeType.HYPERLINK);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()),
				singletonList(new AttributeConstraintViolation(DEFAULT_VALUE_HYPERLINK, attr)));
	}

	@Test
	public void testDefaultValueHyperlinkValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("http://www.molgenis.org");
		when(attr.getDataType()).thenReturn(AttributeType.HYPERLINK);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()), emptyList());
	}

	@Test
	public void testDefaultValueEnum()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("myAttribute");
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getEnumOptions()).thenReturn(asList("a", "b", "c"));
		when(attr.getDataType()).thenReturn(AttributeType.ENUM);
		EntityType entityType = createMockEntityType("MyEntityType");
		when(attr.getEntity()).thenReturn(entityType);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()),
				singletonList(new AttributeConstraintViolation(DEFAULT_VALUE_ENUM, attr)));
	}

	@Test
	public void testDefaultValueEnumValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("b");
		when(attr.getEnumOptions()).thenReturn(asList("a", "b", "c"));
		when(attr.getDataType()).thenReturn(AttributeType.ENUM);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()), emptyList());

	}

	@Test
	public void testDefaultValueInt1()
	{
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getDataType()).thenReturn(AttributeType.INT);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()),
				singletonList(new AttributeConstraintViolation(DEFAULT_VALUE_TYPE, attr)));
	}

	@Test
	public void testDefaultValueInt2()
	{
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("1.0");
		when(attr.getDataType()).thenReturn(AttributeType.INT);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()),
				singletonList(new AttributeConstraintViolation(DEFAULT_VALUE_TYPE, attr)));
	}

	@Test
	public void testDefaultValueIntValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("123456");
		when(attr.getDataType()).thenReturn(AttributeType.INT);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()), emptyList());
	}

	@Test
	public void testDefaultValueLong()
	{
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("test");
		when(attr.getDataType()).thenReturn(AttributeType.LONG);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()),
				singletonList(new AttributeConstraintViolation(DEFAULT_VALUE_TYPE, attr)));
	}

	@Test
	public void testDefaultValueLongValid()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDefaultValue()).thenReturn("123456");
		when(attr.getDataType()).thenReturn(AttributeType.LONG);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()), emptyList());
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

		assertEquals(attributeValidator.validateDefaultValue(attr, false).collect(toList()), emptyList());
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
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()), emptyList());
	}

	@Test
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
		Attribute attr = createMockAttribute("myAttribute");
		when(attr.getDefaultValue()).thenReturn("entityId");
		when(attr.getDataType()).thenReturn(AttributeType.XREF);
		when(attr.getRefEntity()).thenReturn(refEntityType);

		when(dataService.query(refEntityTypeId).eq(refIdAttributeName, "entityId").count()).thenReturn(0L);
		Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("entityId").getMock();
		when(entityManager.getReference(refEntityType, "entityId")).thenReturn(refEntity);
		assertEquals(attributeValidator.validateDefaultValue(attr, true).collect(toList()),
				singletonList(new AttributeConstraintViolation(DEFAULT_VALUE_ENTITY_REFERENCE, attr)));
	}

	@DataProvider(name = "allowedTransitionProvider")
	private static Object[][] allowedTransitionProvider()
	{
		Attribute currentAttr1 = createMockAttribute("attr1");
		Attribute currentAttr2 = createMockAttribute("attr2");
		Attribute currentAttr3 = createMockAttribute("attr3");
		when(currentAttr1.getDataType()).thenReturn(BOOL);
		when(currentAttr2.getDataType()).thenReturn(CATEGORICAL);
		when(currentAttr3.getDataType()).thenReturn(COMPOUND);

		Attribute newAttr1 = createMockAttribute("attr1");
		Attribute newAttr2 = createMockAttribute("attr2");
		Attribute newAttr3 = createMockAttribute("attr3");
		when(newAttr1.getDataType()).thenReturn(INT);
		when(newAttr2.getDataType()).thenReturn(INT);
		when(newAttr3.getDataType()).thenReturn(INT);

		return new Object[][] { { currentAttr1, newAttr1 }, { currentAttr2, newAttr2 }, { currentAttr3, newAttr3 } };
	}

	@DataProvider(name = "disallowedTransitionProvider")
	private static Object[][] disallowedTransitionProvider()
	{
		Attribute currentAttr1 = createMockAttribute("attr1");
		Attribute currentAttr2 = createMockAttribute("attr2");
		Attribute currentAttr3 = createMockAttribute("attr3");
		when(currentAttr1.getDataType()).thenReturn(BOOL);
		when(currentAttr2.getDataType()).thenReturn(CATEGORICAL);
		when(currentAttr3.getDataType()).thenReturn(COMPOUND);

		Attribute newAttr1 = createMockAttribute("attr1");
		Attribute newAttr2 = createMockAttribute("attr2");
		Attribute newAttr3 = createMockAttribute("attr3");
		when(newAttr1.getDataType()).thenReturn(ONE_TO_MANY);
		when(newAttr2.getDataType()).thenReturn(HYPERLINK);
		when(newAttr3.getDataType()).thenReturn(FILE);

		return new Object[][] { { currentAttr1, newAttr1 }, { currentAttr2, newAttr2 }, { currentAttr3, newAttr3 } };
	}

	private static Attribute createMockAttribute(String identifier)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("MyEntityType");

		Attribute attr = mock(Attribute.class);
		when(attr.getName()).thenReturn(identifier);
		when(attr.getIdentifier()).thenReturn(identifier);
		when(attr.getEntity()).thenReturn(entityType);
		return attr;
	}

	private static EntityType createMockEntityType(String id)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(id);
		return entityType;
	}
}