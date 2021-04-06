package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.annotation.Nonnull;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Spring validator for {@link Entity}.
 *
 * @see EntityAttributesValidator
 */
@Component
public class EntityValidator implements Validator {
  private static final String ERROR_CODE_NOT_NULL = "constraints.NotNull";
  private static final String ERROR_CODE_ENUM = "constraints.Enum";
  private static final String ERROR_CODE_NULLABLE_EXPRESSION = "constraints.NullableExpression";
  private static final String ERROR_CODE_VALIDATION_EXPRESSION = "constraints.ValidationExpression";
  private static final String ERROR_CODE_MAX_LENGTH = "constraints.MaxLength";
  private static final String ERROR_CODE_NOT_EMPTY = "constraints.NotEmpty";
  private static final String ERROR_CODE_RANGE = "constraints.Range";
  private static final String ERROR_CODE_MIN = "constraints.Min";
  private static final String ERROR_CODE_MAX = "constraints.Max";
  private static final String ERROR_CODE_EMAIL = "constraints.Email";
  private static final String ERROR_CODE_URI = "constraints.Uri";

  private final ExpressionValidator expressionValidator;
  private final EmailValidator emailValidator;

  public EntityValidator(ExpressionValidator expressionValidator) {
    this.expressionValidator = requireNonNull(expressionValidator);
    this.emailValidator = new EmailValidator();
  }

  @Override
  public boolean supports(@Nonnull Class<?> clazz) {
    return Entity.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@Nonnull Object target, @Nonnull Errors errors) {
    Entity entity = (Entity) target;

    entity
        .getEntityType()
        .getAtomicAttributes()
        .forEach(attribute -> validate(entity, attribute, errors));
  }

  private void validate(Entity entity, Attribute attribute, Errors errors) {
    if (!attribute.isMappedBy()) {
      validateSizeConstraint(entity, attribute, errors);
      validateNotNullConstraint(entity, attribute, errors);
      validateRangeConstraint(entity, attribute, errors);
      validateNullableExpressionConstraint(entity, attribute, errors);
      validateValidationExpressionConstraint(entity, attribute, errors);
      validateEmailConstraint(entity, attribute, errors);
      validateUriConstraint(entity, attribute, errors);
      validateEnumConstraint(entity, attribute, errors);
    }
  }

  private void validateEnumConstraint(Entity entity, Attribute attribute, Errors errors) {
    if (attribute.getDataType() != AttributeType.ENUM) {
      return;
    }

    String enumValue = entity.getString(attribute);
    if (enumValue == null) {
      return;
    }

    List<String> enumOptions = attribute.getEnumOptions();
    if (!enumOptions.contains(enumValue)) {
      errors.rejectValue(
          attribute.getName(),
          ERROR_CODE_ENUM,
          new Object[] {String.join(", ", enumOptions)},
          null);
    }
  }

  private void validateNullableExpressionConstraint(
      Entity entity, Attribute attribute, Errors errors) {
    String nullableExpression = attribute.getNullableExpression();
    if (nullableExpression == null) {
      return;
    }
    boolean valid = expressionValidator.resolveBooleanExpression(nullableExpression, entity);
    if (!valid) {
      errors.rejectValue(
          attribute.getName(),
          ERROR_CODE_NULLABLE_EXPRESSION,
          new Object[] {nullableExpression},
          null);
    }
  }

  private void validateValidationExpressionConstraint(
      Entity entity, Attribute attribute, Errors errors) {
    String validationExpression = attribute.getValidationExpression();
    if (validationExpression == null) {
      return;
    }
    boolean valid = expressionValidator.resolveBooleanExpression(validationExpression, entity);
    if (!valid) {
      errors.rejectValue(
          attribute.getName(),
          ERROR_CODE_VALIDATION_EXPRESSION,
          new Object[] {validationExpression},
          null);
    }
  }

  private void validateSizeConstraint(Entity entity, Attribute attribute, Errors errors) {
    AttributeType attributeType = attribute.getDataType();
    Integer maxLength = attribute.getMaxLength();
    if (maxLength == null) {
      return;
    }

    switch (attributeType) {
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        String value = entity.getString(attribute);
        if (value != null && value.length() > maxLength) {
          errors.rejectValue(
              attribute.getName(), ERROR_CODE_MAX_LENGTH, new Object[] {maxLength}, null);
        }
        break;
      case BOOL:
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case COMPOUND:
      case DATE:
      case DATE_TIME:
      case DECIMAL:
      case FILE:
      case INT:
      case LONG:
      case MREF:
      case ONE_TO_MANY:
      case XREF:
      default:
        throw new UnexpectedEnumException(attributeType);
    }
  }

  private void validateNotNullConstraint(Entity entity, Attribute attribute, Errors errors) {
    if (attribute.isNillable()) {
      return;
    }

    boolean valid;
    String errorCode;

    AttributeType attributeType = attribute.getDataType();
    switch (attributeType) {
      case BOOL:
        valid = entity.getBoolean(attribute) != null;
        errorCode = ERROR_CODE_NOT_NULL;
        break;
      case CATEGORICAL:
      case FILE:
      case XREF:
        valid = entity.getEntity(attribute) != null;
        errorCode = ERROR_CODE_NOT_NULL;
        break;
      case DATE:
        valid = entity.getLocalDate(attribute) != null;
        errorCode = ERROR_CODE_NOT_NULL;
        break;
      case DATE_TIME:
        valid = entity.getInstant(attribute) != null;
        errorCode = ERROR_CODE_NOT_NULL;
        break;
      case DECIMAL:
        valid = entity.getDouble(attribute) != null;
        errorCode = ERROR_CODE_NOT_NULL;
        break;
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        valid = entity.getString(attribute) != null;
        errorCode = ERROR_CODE_NOT_NULL;
        break;
      case INT:
        valid = entity.getInt(attribute) != null;
        errorCode = ERROR_CODE_NOT_NULL;
        break;
      case LONG:
        valid = entity.getLong(attribute) != null;
        errorCode = ERROR_CODE_NOT_NULL;
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        valid = !Iterables.isEmpty(entity.getEntities(attribute));
        errorCode = ERROR_CODE_NOT_EMPTY;
        break;
      case COMPOUND:
      default:
        throw new UnexpectedEnumException(attributeType);
    }

    if (!valid) {
      errors.rejectValue(attribute.getName(), errorCode, null, null);
    }
  }

  private void validateRangeConstraint(Entity entity, Attribute attribute, Errors errors) {
    if (attribute.getRange() == null) {
      return;
    }

    AttributeType attributeType = attribute.getDataType();
    switch (attributeType) {
      case INT:
        validateRangeConstraint(attribute, entity.getInt(attribute), errors);
        break;
      case LONG:
        validateRangeConstraint(attribute, entity.getLong(attribute), errors);
        break;
      case BOOL:
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case COMPOUND:
      case DATE:
      case DATE_TIME:
      case DECIMAL:
      case EMAIL:
      case ENUM:
      case FILE:
      case HTML:
      case HYPERLINK:
      case MREF:
      case ONE_TO_MANY:
      case SCRIPT:
      case STRING:
      case TEXT:
      case XREF:
      default:
        throw new UnexpectedEnumException(attributeType);
    }
  }

  private void validateRangeConstraint(Attribute attribute, Number number, Errors errors) {
    if (number == null) {
      return;
    }
    long value = number.longValue();

    Range range = attribute.getRange();
    if (range == null) {
      return;
    }

    Long min = range.getMin();
    Long max = range.getMax();
    if (min != null && max != null) {
      if (value < min || value > max) {
        errors.rejectValue(attribute.getName(), ERROR_CODE_RANGE, new Object[] {min, max}, null);
      }
    } else if (min != null) {
      if (value < min) {
        errors.rejectValue(attribute.getName(), ERROR_CODE_MIN, new Object[] {min}, null);
      }
    } else if (max != null && value > max) {
      errors.rejectValue(attribute.getName(), ERROR_CODE_MAX, new Object[] {max}, null);
    }
  }

  private void validateEmailConstraint(Entity entity, Attribute attribute, Errors errors) {
    if (attribute.getDataType() != AttributeType.EMAIL) {
      return;
    }

    String emailValue = entity.getString(attribute);
    if (emailValue == null) {
      return;
    }

    boolean valid = emailValidator.isValid(emailValue, null);
    if (!valid) {
      errors.rejectValue(attribute.getName(), ERROR_CODE_EMAIL, null, null);
    }
  }

  private void validateUriConstraint(Entity entity, Attribute attribute, Errors errors) {
    if (attribute.getDataType() != AttributeType.HYPERLINK) {
      return;
    }

    String hyperlinkValue = entity.getString(attribute);
    if (hyperlinkValue == null) {
      return;
    }

    try {
      new URI(hyperlinkValue);
    } catch (URISyntaxException e) {
      errors.rejectValue(attribute.getName(), ERROR_CODE_URI, null, null);
    }
  }
}
