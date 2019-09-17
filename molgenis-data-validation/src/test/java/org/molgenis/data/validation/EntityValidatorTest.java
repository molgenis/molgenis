package org.molgenis.data.validation;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.validation.Errors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityValidatorTest extends AbstractMockitoTest {
  @Mock private ExpressionValidator expressionValidator;
  private EntityValidator entityValidator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityValidator = new EntityValidator(expressionValidator);
  }

  @Test
  public void testSupportsEntity() {
    assertTrue(entityValidator.supports(Entity.class));
  }

  @Test
  public void testSupportsString() {
    assertFalse(entityValidator.supports(String.class));
  }

  @Test
  public void testValidateMaxLengthConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    String value = "x".repeat(256);
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getString(attribute)).thenReturn(value);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.MaxLength", new Object[] {255L}, null);
  }

  @Test
  public void testValidateNotNullConstraintString() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotNull", null, null);
  }

  @Test
  public void testValidateNotNullConstraintMref() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getEntities(attribute)).thenReturn(Collections.emptyList());

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotEmpty", null, null);
  }

  @Test
  public void testValidateNotNullConstraintInt() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getInt(attribute)).thenReturn(null);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotNull", null, null);
  }

  @Test
  public void testValidateNotNullConstraintXref() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getEntity(attribute)).thenReturn(null);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotNull", null, null);
  }

  @Test
  public void testValidateNotNullConstraintDate() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(DATE).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotNull", null, null);
  }

  @Test
  public void testValidateNotNullConstraintDateTime() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(DATE_TIME).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotNull", null, null);
  }

  @Test
  public void testValidateNotNullConstraintDecimal() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(DECIMAL).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getDouble(attribute)).thenReturn(null);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotNull", null, null);
  }

  @Test
  public void testValidateNotNullConstraintLong() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(LONG).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getLong(attribute)).thenReturn(null);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotNull", null, null);
  }

  @Test
  public void testValidateNotNullConstraintBool() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getBoolean(attribute)).thenReturn(null);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.NotNull", null, null);
  }

  @Test(expectedExceptions = UnexpectedEnumException.class)
  public void testValidateNotNullConstraintCompound() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
  }

  @Test
  public void testValidateRangeMinMaxConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    when(attribute.getName()).thenReturn(attributeName);
    long min = 1L;
    long max = 3L;
    when(attribute.getRange()).thenReturn(new Range(min, max));

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    int value = 4;
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getInt(attribute)).thenReturn(value);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.Range", new Object[] {min, max}, null);
  }

  @Test
  public void testValidateRangeMinConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(LONG).getMock();
    when(attribute.getName()).thenReturn(attributeName);
    long min = 1L;
    when(attribute.getRange()).thenReturn(new Range(min, null));

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    long value = 0L;
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getLong(attribute)).thenReturn(value);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.Min", new Object[] {min}, null);
  }

  @Test
  public void testValidateRangeMaxConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(LONG).getMock();
    when(attribute.getName()).thenReturn(attributeName);
    long max = 3L;
    when(attribute.getRange()).thenReturn(new Range(null, max));

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    long value = 4L;
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getLong(attribute)).thenReturn(value);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.Max", new Object[] {max}, null);
  }

  @Test
  public void testValidateNullableExpressionConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(attribute.getName()).thenReturn(attributeName);
    String expression = "MyExpression";
    when(attribute.getNullableExpression()).thenReturn(expression);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors)
        .rejectValue(
            attributeName, "constraints.NullableExpression", new Object[] {expression}, null);
  }

  @Test
  public void testValidateValidationExpressionConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(attribute.getName()).thenReturn(attributeName);
    String expression = "MyExpression";
    when(attribute.getValidationExpression()).thenReturn(expression);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors)
        .rejectValue(
            attributeName, "constraints.ValidationExpression", new Object[] {expression}, null);
  }

  @Test
  public void testValidateEmailConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(EMAIL).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    String value = "invalidEmail";
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getString(attribute)).thenReturn(value);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.Email", null, null);
  }

  @Test
  public void testValidateUriConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(HYPERLINK).getMock();
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    String value = "\\";
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getString(attribute)).thenReturn(value);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors).rejectValue(attributeName, "constraints.Uri", null, null);
  }

  @Test
  public void testValidateEnumConstraint() {
    String attributeName = "attr";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(ENUM).getMock();
    List<String> enumOptions = asList("enum0", "enum1", "enum2");
    when(attribute.getEnumOptions()).thenReturn(enumOptions);
    when(attribute.getName()).thenReturn(attributeName);

    EntityType entityType =
        when(mock(EntityType.class).getAtomicAttributes())
            .thenReturn(singletonList(attribute))
            .getMock();

    String value = "invalidEnum";
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getString(attribute)).thenReturn(value);

    Errors errors = mock(Errors.class);
    entityValidator.validate(entity, errors);
    verify(errors)
        .rejectValue(
            attributeName, "constraints.Enum", new Object[] {String.join(", ", enumOptions)}, null);
  }
}
