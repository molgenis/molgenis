package org.molgenis.data.validation;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.AttributeMetadata.DEFAULT_VALUE;
import static org.molgenis.data.meta.model.AttributeMetadata.REF_ENTITY_TYPE;

public class DefaultValueReferenceValidatorImplTest
{
	private DataService dataService;
	private EntityType entityType;
	private DefaultValueReferenceValidatorImpl defaultValueReferenceValidator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class, RETURNS_DEEP_STUBS);
		entityType = when(mock(EntityType.class).getIdValue()).thenReturn("entityTypeId").getMock();
		when(entityType.getLabel()).thenReturn("My Entity Type");
		defaultValueReferenceValidator = new DefaultValueReferenceValidatorImpl(dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testDefaultValueReferenceValidatorImpl()
	{
		new DefaultValueReferenceValidatorImpl(null);
	}

	@Test
	public void testValidateEntityNotReferenced()
	{
		Attribute attribute = createMockAttribute(AttributeType.XREF, "otherId");
		initializeDataServiceMock(Stream.of(attribute));

		Entity entity = createMockEntity("id");
		defaultValueReferenceValidator.validateEntityNotReferenced(entity);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'My Entity Type' with id 'id' is referenced as default value by attribute\\(s\\): 'myAttribute'")
	public void testValidateEntityNotReferencedValidationException()
	{
		Attribute attribute = createMockAttribute(AttributeType.XREF, "id");
		initializeDataServiceMock(Stream.of(attribute));

		Entity entity = createMockEntity("id");
		defaultValueReferenceValidator.validateEntityNotReferenced(entity);
	}

	@Test
	public void testValidateEntitiesNotReferenced()
	{
		Attribute attribute = createMockAttribute(AttributeType.MREF, "id0,id1");
		initializeDataServiceMock(Stream.of(attribute));

		Entity entity = createMockEntity("id2");
		//noinspection ResultOfMethodCallIgnored
		defaultValueReferenceValidator.validateEntitiesNotReferenced(Stream.of(entity), entityType).count();
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'My Entity Type' with id 'id1' is referenced as default value by attribute\\(s\\): 'myAttribute'")
	public void testValidateEntitiesNotReferencedValidationException()
	{
		Attribute attribute = createMockAttribute(AttributeType.MREF, "id0,id1");
		initializeDataServiceMock(Stream.of(attribute));

		Entity entity = createMockEntity("id1");
		//noinspection ResultOfMethodCallIgnored
		defaultValueReferenceValidator.validateEntitiesNotReferenced(Stream.of(entity), entityType).count();
	}

	@Test
	public void testValidateEntityNotReferencedById()
	{
		Attribute attribute = createMockAttribute(AttributeType.XREF, "otherId");
		initializeDataServiceMock(Stream.of(attribute));

		defaultValueReferenceValidator.validateEntityNotReferencedById("id", entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'My Entity Type' with id 'id' is referenced as default value by attribute\\(s\\): 'myAttribute'")
	public void testValidateEntityNotReferencedByIdValidationException()
	{
		Attribute attribute = createMockAttribute(AttributeType.XREF, "id");
		initializeDataServiceMock(Stream.of(attribute));

		defaultValueReferenceValidator.validateEntityNotReferencedById("id", entityType);
	}

	@Test
	public void testValidateEntitiesNotReferencedById()
	{
		Attribute attribute = createMockAttribute(AttributeType.MREF, "id0,id1");
		initializeDataServiceMock(Stream.of(attribute));

		//noinspection ResultOfMethodCallIgnored
		defaultValueReferenceValidator.validateEntitiesNotReferencedById(Stream.of("id2"), entityType).count();
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'My Entity Type' with id 'id1' is referenced as default value by attribute\\(s\\): 'myAttribute'")
	public void testValidateEntitiesNotReferencedByIdValidationException()
	{
		Attribute attribute = createMockAttribute(AttributeType.MREF, "id0,id1");
		initializeDataServiceMock(Stream.of(attribute));

		//noinspection ResultOfMethodCallIgnored
		defaultValueReferenceValidator.validateEntitiesNotReferencedById(Stream.of("id1"), entityType).count();
	}

	@Test
	public void testValidateEntityTypeNotReferenced()
	{
		initializeDataServiceMock(Stream.empty());
		defaultValueReferenceValidator.validateEntityTypeNotReferenced(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'My Entity Type' entities are referenced as default value by attributes")
	public void testValidateEntityTypeNotReferencedValidationException()
	{
		Attribute attribute = createMockAttribute(AttributeType.MREF, "id0,id1");
		initializeDataServiceMock(Stream.of(attribute));
		defaultValueReferenceValidator.validateEntityTypeNotReferenced(entityType);
	}

	private Attribute createMockAttribute(AttributeType attributeType, String defaultValue)
	{
		Attribute idAttribute = mock(Attribute.class);
		when(idAttribute.getDataType()).thenReturn(STRING);

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getIdAttribute()).thenReturn(idAttribute);

		Attribute attribute = mock(Attribute.class);
		when(attribute.getName()).thenReturn("myAttribute");
		when(attribute.getDataType()).thenReturn(attributeType);
		when(attribute.getDefaultValue()).thenReturn(defaultValue);
		when(attribute.getRefEntity()).thenReturn(refEntityType);
		return attribute;
	}

	private Entity createMockEntity(String entityId)
	{
		Entity entity = mock(Entity.class);
		when(entity.getEntityType()).thenReturn(entityType).getMock();
		when(entity.getIdValue()).thenReturn(entityId);
		return entity;
	}

	private void initializeDataServiceMock(Stream<Attribute> attributeStream)
	{
		when(dataService.query(AttributeMetadata.ATTRIBUTE_META_DATA, Attribute.class)
						.eq(REF_ENTITY_TYPE, "entityTypeId")
						.and()
						.not()
						.eq(DEFAULT_VALUE, null)
						.findAll()).thenReturn(attributeStream);
	}
}