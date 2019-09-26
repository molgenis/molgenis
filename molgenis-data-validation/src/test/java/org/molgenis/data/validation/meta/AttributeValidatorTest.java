package org.molgenis.data.validation.meta;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.Sort.Direction.ASC;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.validation.meta.AttributeValidator.ValidationMode.ADD;
import static org.molgenis.data.validation.meta.AttributeValidator.ValidationMode.ADD_SKIP_ENTITY_VALIDATION;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.TemplateExpressionSyntaxException;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;

class AttributeValidatorTest {
  private AttributeValidator attributeValidator;
  private DataService dataService;
  private EntityManager entityManager;

  @BeforeEach
  void beforeMethod() {
    dataService = mock(DataService.class);
    entityManager = mock(EntityManager.class);
    attributeValidator = new AttributeValidator(dataService, entityManager);
  }

  @Test
  void validateAttributeInvalidName() {
    Attribute attr = makeMockAttribute("invalid.name");
    @SuppressWarnings("deprecation")
    Exception exception =
        assertThrows(
            MolgenisValidationException.class, () -> attributeValidator.validate(attr, ADD));
    assertThat(exception.getMessage())
        .containsPattern(
            "Invalid characters in: \\[invalid.name\\] Only letters \\(a-z, A-Z\\), digits \\(0-9\\), underscores \\(_\\) and hashes \\(#\\) are allowed.");
  }

  @Test
  void validateMappedByValidEntity() {
    String entityTypeId = "entityTypeId";
    EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute attr = makeMockAttribute("attrName");
    when(attr.getRefEntity()).thenReturn(refEntity);
    String mappedByAttrName = "mappedByAttrName";
    Attribute mappedByAttr =
        when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
    when(mappedByAttr.getDataType()).thenReturn(XREF);
    when(attr.getMappedBy()).thenReturn(mappedByAttr);
    when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
    attributeValidator.validate(attr, ADD);
  }

  @Test
  void validateMappedByInvalidEntity() {
    String entityTypeId = "entityTypeId";
    EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute attr = makeMockAttribute("attrName");
    when(attr.getRefEntity()).thenReturn(refEntity);
    String mappedByAttrName = "mappedByAttrName";
    Attribute mappedByAttr =
        when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
    when(mappedByAttr.getDataType()).thenReturn(XREF);
    when(attr.getMappedBy()).thenReturn(mappedByAttr);
    when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
    @SuppressWarnings("deprecation")
    Exception exception =
        assertThrows(MolgenisDataException.class, () -> attributeValidator.validate(attr, ADD));
    assertThat(exception.getMessage())
        .containsPattern(
            "mappedBy attribute \\[mappedByAttrName\\] is not part of entity \\[entityTypeId\\].");
  }

  @Test
  void validateMappedByInvalidDataType() {
    String entityTypeId = "entityTypeId";
    EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute attr = makeMockAttribute("attrName");
    when(attr.getRefEntity()).thenReturn(refEntity);
    String mappedByAttrName = "mappedByAttrName";
    Attribute mappedByAttr =
        when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
    when(mappedByAttr.getDataType()).thenReturn(STRING); // invalid type
    when(attr.getMappedBy()).thenReturn(mappedByAttr);
    when(refEntity.getAttribute(mappedByAttrName)).thenReturn(null);
    @SuppressWarnings("deprecation")
    Exception exception =
        assertThrows(MolgenisDataException.class, () -> attributeValidator.validate(attr, ADD));
    assertThat(exception.getMessage())
        .containsPattern(
            "Invalid mappedBy attribute \\[mappedByAttrName\\] data type \\[STRING\\].");
  }

  @Test
  void validateOrderByValid() {
    String entityTypeId = "entityTypeId";
    EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute attr = makeMockAttribute("attrName");
    when(attr.getRefEntity()).thenReturn(refEntity);
    String mappedByAttrName = "mappedByAttrName";
    Attribute mappedByAttr =
        when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
    when(mappedByAttr.getDataType()).thenReturn(XREF);
    when(attr.getMappedBy()).thenReturn(mappedByAttr);
    when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
    when(attr.getOrderBy()).thenReturn(new Sort(mappedByAttrName, ASC));
    attributeValidator.validate(attr, ADD);
  }

  @Test
  void validateOrderByInvalidRefAttribute() {
    String entityTypeId = "entityTypeId";
    EntityType refEntity = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute attr = makeMockAttribute("attrName");
    EntityType entity = mock(EntityType.class);
    when(entity.getId()).thenReturn("test");
    when(attr.getEntityType()).thenReturn(entity);
    when(attr.getRefEntity()).thenReturn(refEntity);
    String mappedByAttrName = "mappedByAttrName";
    Attribute mappedByAttr =
        when(mock(Attribute.class).getName()).thenReturn(mappedByAttrName).getMock();
    when(mappedByAttr.getDataType()).thenReturn(XREF);
    when(attr.getMappedBy()).thenReturn(mappedByAttr);
    when(refEntity.getAttribute(mappedByAttrName)).thenReturn(mappedByAttr);
    when(attr.getOrderBy()).thenReturn(new Sort("fail", ASC));
    @SuppressWarnings("deprecation")
    Exception exception =
        assertThrows(MolgenisDataException.class, () -> attributeValidator.validate(attr, ADD));
    assertThat(exception.getMessage())
        .containsPattern(
            "Unknown entity \\[entityTypeId\\] attribute \\[fail\\] referred to by entity \\[test\\] attribute \\[attrName\\] sortBy \\[fail,ASC\\]");
  }

  @SuppressWarnings("deprecation")
  @ParameterizedTest
  @MethodSource("disallowedTransitionProvider")
  void testDisallowedTransition(Attribute currentAttr, Attribute newAttr) {
    when(dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdentifier(), Attribute.class))
        .thenReturn(currentAttr);
    assertThrows(
        MolgenisDataException.class,
        () -> attributeValidator.validate(newAttr, ValidationMode.UPDATE));
  }

  @ParameterizedTest
  @MethodSource("allowedTransitionProvider")
  void testAllowedTransition(Attribute currentAttr, Attribute newAttr) {
    when(dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdentifier(), Attribute.class))
        .thenReturn(currentAttr);
    assertDoesNotThrow(() -> attributeValidator.validate(newAttr, ValidationMode.UPDATE));
  }

  @Test
  void testDefaultValueDate() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("test");
    when(attr.getDataType()).thenReturn(AttributeType.DATE);
    try {
      attributeValidator.validateDefaultValue(attr, true);
      fail();
    } catch (MolgenisDataException actual) {
      assertEquals(
          "Text 'test' could not be parsed, unparsed text found at index 0",
          actual.getCause().getMessage());
    }
  }

  @Test
  void testDefaultValueDateValid() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("2016-01-01");
    when(attr.getDataType()).thenReturn(AttributeType.DATE);
    attributeValidator.validateDefaultValue(attr, true);
  }

  @Test
  void testDefaultValueDateTime() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("test");
    when(attr.getDataType()).thenReturn(AttributeType.DATE_TIME);
    try {
      attributeValidator.validateDefaultValue(attr, true);
      fail();
    } catch (MolgenisDataException actual) {
      assertEquals(
          "Text 'test' could not be parsed, unparsed text found at index 0",
          actual.getCause().getMessage());
    }
  }

  @Test
  void testDefaultValueDateTimeValid() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("2016-10-10T12:00:10+0000");
    when(attr.getDataType()).thenReturn(AttributeType.DATE_TIME);
    attributeValidator.validateDefaultValue(attr, true);
  }

  @Test
  void testDefaultValueHyperlink() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("test^");
    when(attr.getDataType()).thenReturn(AttributeType.HYPERLINK);
    try {
      attributeValidator.validateDefaultValue(attr, true);
      fail();
    } catch (MolgenisDataException actual) {
      assertEquals("Default value [test^] is not a valid hyperlink.", actual.getMessage());
    }
  }

  @Test
  void testDefaultValueHyperlinkValid() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("http://www.molgenis.org");
    when(attr.getDataType()).thenReturn(AttributeType.HYPERLINK);
    attributeValidator.validateDefaultValue(attr, true);
  }

  @Test
  void testDefaultValueEnum() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("test");
    when(attr.getEnumOptions()).thenReturn(asList("a", "b", "c"));
    when(attr.getDataType()).thenReturn(AttributeType.ENUM);
    try {
      attributeValidator.validateDefaultValue(attr, true);
      fail();
    } catch (MolgenisDataException actual) {
      assertEquals(
          "Invalid default value [test] for enum [null] value must be one of [a, b, c]",
          actual.getMessage());
    }
  }

  @Test
  void testDefaultValueEnumValid() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("b");
    when(attr.getEnumOptions()).thenReturn(asList("a", "b", "c"));
    when(attr.getDataType()).thenReturn(AttributeType.ENUM);
    attributeValidator.validateDefaultValue(attr, true);
  }

  @Test
  void testDefaultValueInt1() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("test");
    when(attr.getDataType()).thenReturn(AttributeType.INT);
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> attributeValidator.validateDefaultValue(attr, true));
    assertThat(exception.getMessage())
        .containsPattern("Invalid default value \\[test\\] for data type \\[INT\\]");
  }

  @Test
  void testDefaultValueInt2() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("1.0");
    when(attr.getDataType()).thenReturn(AttributeType.INT);
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> attributeValidator.validateDefaultValue(attr, true));
    assertThat(exception.getMessage())
        .containsPattern("Invalid default value \\[1.0\\] for data type \\[INT\\]");
  }

  @Test
  void testDefaultValueIntValid() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("123456");
    when(attr.getDataType()).thenReturn(AttributeType.INT);
    attributeValidator.validateDefaultValue(attr, true);
  }

  @Test
  void testDefaultValueLong() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("test");
    when(attr.getDataType()).thenReturn(LONG);
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> attributeValidator.validateDefaultValue(attr, true));
    assertThat(exception.getMessage())
        .containsPattern("Invalid default value \\[test\\] for data type \\[LONG\\]");
  }

  @Test
  void testDefaultValueLongValid() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDefaultValue()).thenReturn("123456");
    when(attr.getDataType()).thenReturn(LONG);
    attributeValidator.validateDefaultValue(attr, true);
  }

  @Test
  void testDefaultXrefSkipEntityReferenceValidation() {
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
  void testDefaultXref() {
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

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class, RETURNS_SELF);
    when(repository.query()).thenReturn(query);
    when(query.eq(refIdAttributeName, "entityId").count()).thenReturn(1L);
    MetaDataService metaDataService = mock(MetaDataService.class);
    when(metaDataService.getRepository(refEntityType)).thenReturn(Optional.of(repository));
    when(dataService.getMeta()).thenReturn(metaDataService);

    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("entityId").getMock();
    when(entityManager.getReference(refEntityType, "entityId")).thenReturn(refEntity);
    attributeValidator.validateDefaultValue(attr, true);
  }

  @Test
  void testDefaultXrefInvalid() {
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

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class, RETURNS_SELF);
    when(repository.query()).thenReturn(query);
    when(query.eq(refIdAttributeName, "entityId").count()).thenReturn(0L);
    MetaDataService metaDataService = mock(MetaDataService.class);
    when(metaDataService.getRepository(refEntityType)).thenReturn(Optional.of(repository));
    when(dataService.getMeta()).thenReturn(metaDataService);

    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("entityId").getMock();
    when(entityManager.getReference(refEntityType, "entityId")).thenReturn(refEntity);
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> attributeValidator.validateDefaultValue(attr, true));
    assertThat(exception.getMessage())
        .containsPattern("Default value \\[entityId\\] refers to an unknown entity");
  }

  @Test
  void testUpdateIdAttribute() {
    String attributeId = "attributeId";
    String attributeName = "attributeName";

    Attribute currentAttribute = mock(Attribute.class);
    when(currentAttribute.getIdentifier()).thenReturn(attributeId);
    when(currentAttribute.getName()).thenReturn(attributeName);
    when(currentAttribute.isIdAttribute()).thenReturn(true);

    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn(attributeId);
    when(attribute.getName()).thenReturn(attributeName);
    when(attribute.isIdAttribute()).thenReturn(false);

    when(dataService.findOneById(ATTRIBUTE_META_DATA, attributeId, Attribute.class))
        .thenReturn(currentAttribute);
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> attributeValidator.validate(attribute, ValidationMode.UPDATE));
    assertThat(exception.getMessage()).containsPattern("ID attribute update not allowed");
  }

  private static Object[][] allowedTransitionProvider() {
    Attribute currentAttr1 = makeMockAttribute("attr1");
    Attribute currentAttr2 = makeMockAttribute("attr2");
    Attribute currentAttr3 = makeMockAttribute("attr3");
    when(currentAttr1.getDataType()).thenReturn(BOOL);
    when(currentAttr2.getDataType()).thenReturn(CATEGORICAL);
    when(currentAttr3.getDataType()).thenReturn(LONG);

    Attribute newAttr1 = makeMockAttribute("attr1");
    Attribute newAttr2 = makeMockAttribute("attr2");
    Attribute newAttr3 = makeMockAttribute("attr3");
    when(newAttr1.getDataType()).thenReturn(INT);
    when(newAttr2.getDataType()).thenReturn(INT);
    when(newAttr3.getDataType()).thenReturn(INT);

    return new Object[][] {
      {currentAttr1, newAttr1}, {currentAttr2, newAttr2}, {currentAttr3, newAttr3}
    };
  }

  private static Object[][] disallowedTransitionProvider() {
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

    return new Object[][] {
      {currentAttr1, newAttr1}, {currentAttr2, newAttr2}, {currentAttr3, newAttr3}
    };
  }

  @Test
  void testTemplateExpressionInvalid() {
    Attribute attr = makeMockAttribute("name");
    when(attr.getExpression()).thenReturn("test");
    when(attr.getDataType()).thenReturn(STRING);
    assertThrows(
        TemplateExpressionSyntaxException.class,
        () -> attributeValidator.validate(attr, ADD_SKIP_ENTITY_VALIDATION));
  }

  @Test
  void testTemplateExpressionValid() {
    Attribute attr = makeMockAttribute("name");
    when(attr.getExpression()).thenReturn("{template:xref.id}");
    when(attr.getDataType()).thenReturn(STRING);
    attributeValidator.validate(attr, ADD_SKIP_ENTITY_VALIDATION);
  }

  private static Attribute makeMockAttribute(String name) {
    Attribute attr = mock(Attribute.class);
    when(attr.getName()).thenReturn(name);
    when(attr.getIdentifier()).thenReturn(name);
    return attr;
  }
}
