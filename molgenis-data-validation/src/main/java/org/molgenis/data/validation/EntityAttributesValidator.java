package org.molgenis.data.validation;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.AttributeValue;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.constraint.AttributeValueConstraint;
import org.molgenis.data.validation.constraint.AttributeValueValidationResult;
import org.molgenis.data.validation.constraint.ValidationResult;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.validation.constraint.AttributeValueConstraint.NOT_NULL;
import static org.molgenis.data.validation.constraint.AttributeValueConstraint.RANGE;

/**
 * <p>
 * Attribute data type validator.
 * <p>
 * Does not check if xref,mref, categorical values are present. That happens in the EntityValidator.
 */
@Component
public class EntityAttributesValidator
{
	private final ExpressionValidator expressionValidator;
	private EmailValidator emailValidator;

	public EntityAttributesValidator(ExpressionValidator expressionValidator)
	{
		this.expressionValidator = requireNonNull(expressionValidator);
	}

	public List<ValidationResult> validate(Entity entity, EntityType meta)
	{
		List<ValidationResult> violations = checkNullableExpressions(entity, meta);
		violations.addAll(checkValidationExpressions(entity, meta));

		for (Attribute attr : meta.getAtomicAttributes())
		{
			AttributeValueValidationResult violation = null;

			AttributeType attrType = attr.getDataType();
			switch (attrType)
			{
				case EMAIL:
					violation = checkEmail(entity, attr);
					break;
				case BOOL:
					violation = checkBoolean(entity, attr);
					break;
				case DATE:
					violation = checkDate(entity, attr);
					break;
				case DATE_TIME:
					violation = checkDateTime(entity, attr);
					break;
				case DECIMAL:
					violation = checkDecimal(entity, attr);
					break;
				case HYPERLINK:
					violation = checkHyperlink(entity, attr);
					break;
				case INT:
					violation = checkInt(entity, attr);
					if ((violation == null) && (attr.getRange() != null))
					{
						violation = checkRange(entity, attr);
					}
					break;
				case LONG:
					violation = checkLong(entity, attr);
					if ((violation == null) && (attr.getRange() != null))
					{
						violation = checkRange(entity, attr);
					}
					break;
				case ENUM:
					violation = checkEnum(entity, attr);
					break;
				case HTML:
					violation = checkText(entity, attr, HTML);
					break;
				case SCRIPT:
					violation = checkText(entity, attr, SCRIPT);
					break;
				case TEXT:
					violation = checkText(entity, attr, TEXT);
					break;
				case STRING:
					violation = checkText(entity, attr, STRING);
					break;
				case CATEGORICAL:
				case FILE:
				case XREF:
					violation = checkXref(entity, attr);
					break;
				case CATEGORICAL_MREF:
				case MREF:
				case ONE_TO_MANY:
					violation = checkMref(entity, attr);
					break;
				case COMPOUND:
					// no op
					break;
				default:
					throw new UnexpectedEnumException(attrType);
			}

			if (violation != null)
			{
				violations.add(violation);
			}
		}

		return violations;
	}

	private AttributeValueValidationResult checkMref(Entity entity, Attribute attr)
	{
		Iterable<Entity> refEntities;
		try
		{
			refEntities = entity.getEntities(attr.getName());
		}
		catch (Exception e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attr, entity.get(attr.getName())));
		}
		if (refEntities == null)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attr, entity.get(attr.getName())));
		}
		for (Entity refEntity : refEntities)
		{
			if (refEntity == null)
			{
				return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
						AttributeValue.create(attr, entity.get(attr.getName())));
			}
			if (!refEntity.getEntityType().getId().equals(attr.getRefEntity().getId()))
			{
				return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
						AttributeValue.create(attr, entity.get(attr.getName())));
			}
		}
		return null;
	}

	private AttributeValueValidationResult checkXref(Entity entity, Attribute attr)
	{
		Entity refEntity;
		try
		{
			refEntity = entity.getEntity(attr.getName());
		}
		catch (Exception e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attr, entity.get(attr.getName())));
		}

		if (refEntity == null)
		{
			return null;
		}
		if (!refEntity.getEntityType().getId().equals(attr.getRefEntity().getId()))
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attr, entity.get(attr.getName())));
		}
		return null;
	}

	private List<ValidationResult> checkNullableExpressions(Entity entity, EntityType entityType)
	{
		List<String> nullableExpressions = new ArrayList<>();
		List<Attribute> expressionAttributes = new ArrayList<>();

		for (Attribute attribute : entityType.getAtomicAttributes())
		{
			String nullableExpression = attribute.getNullableExpression();
			if (nullableExpression != null)
			{
				expressionAttributes.add(attribute);
				nullableExpressions.add(nullableExpression);
			}
		}

		List<ValidationResult> violations = new ArrayList<>();

		if (!nullableExpressions.isEmpty())
		{
			List<Boolean> results = expressionValidator.resolveBooleanExpressions(nullableExpressions, entity);
			for (int i = 0; i < results.size(); i++)
			{
				Attribute expressionAttribute = expressionAttributes.get(i);
				if (!results.get(i) && EntityUtils.isNullValue(entity, expressionAttribute))
				{
					Object value = entity.get(expressionAttribute.getName());
					violations.add(new AttributeValueValidationResult(NOT_NULL,
							AttributeValue.create(expressionAttribute, value)));
				}
			}
		}

		return violations;
	}

	private Set<AttributeValueValidationResult> checkValidationExpressions(Entity entity, EntityType meta)
	{
		List<String> validationExpressions = new ArrayList<>();
		List<Attribute> expressionAttributes = new ArrayList<>();

		for (Attribute attribute : meta.getAtomicAttributes())
		{
			if (StringUtils.isNotBlank(attribute.getValidationExpression()))
			{
				expressionAttributes.add(attribute);
				validationExpressions.add(attribute.getValidationExpression());
			}
		}

		Set<AttributeValueValidationResult> violations = new LinkedHashSet<>();

		if (!validationExpressions.isEmpty())
		{
			List<Boolean> results = expressionValidator.resolveBooleanExpressions(validationExpressions, entity);
			for (int i = 0; i < results.size(); i++)
			{
				if (!results.get(i))
				{
					Attribute expressionAttribute = expressionAttributes.get(i);
					violations.add(new AttributeValueValidationResult(AttributeValueConstraint.EXPRESSION,
							AttributeValue.create(expressionAttribute, entity.get(expressionAttribute.getName()))));
				}
			}
		}

		return violations;
	}

	private AttributeValueValidationResult checkEmail(Entity entity, Attribute attribute)
	{
		String email = entity.getString(attribute.getName());
		if (email == null)
		{
			return null;
		}

		if (emailValidator == null)
		{
			emailValidator = new EmailValidator();
		}

		if (!emailValidator.isValid(email, null))
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.EMAIL,
					AttributeValue.create(attribute, email));
		}

		if (email.length() > EMAIL.getMaxLength())
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.MAX_LENGTH,
					AttributeValue.create(attribute, email));
		}

		return null;
	}

	private static AttributeValueValidationResult checkBoolean(Entity entity, Attribute attribute)
	{
		try
		{
			entity.getBoolean(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attribute, entity.get(attribute.getName())));
		}
	}

	private static AttributeValueValidationResult checkDateTime(Entity entity, Attribute attribute)
	{
		try
		{
			entity.getInstant(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attribute, entity.get(attribute.getName())));
		}
	}

	private static AttributeValueValidationResult checkDate(Entity entity, Attribute attribute)
	{
		try
		{
			entity.getLocalDate(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attribute, entity.get(attribute.getName())));
		}
	}

	private static AttributeValueValidationResult checkDecimal(Entity entity, Attribute attribute)
	{
		try
		{
			entity.getDouble(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attribute, entity.get(attribute.getName())));
		}
	}

	private AttributeValueValidationResult checkHyperlink(Entity entity, Attribute attribute)
	{
		String link = entity.getString(attribute.getName());
		if (link == null)
		{
			return null;
		}

		try
		{
			new URI(link);
		}
		catch (URISyntaxException e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.HYPERLINK,
					AttributeValue.create(attribute, link));
		}

		if (link.length() > HYPERLINK.getMaxLength())
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.MAX_LENGTH,
					AttributeValue.create(attribute, link));
		}

		return null;
	}

	private static AttributeValueValidationResult checkInt(Entity entity, Attribute attribute)
	{
		try
		{
			entity.getInt(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attribute, entity.get(attribute.getName())));
		}
	}

	private static AttributeValueValidationResult checkLong(Entity entity, Attribute attribute)
	{
		try
		{
			entity.getLong(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.TYPE,
					AttributeValue.create(attribute, entity.get(attribute.getName())));
		}
	}

	private static AttributeValueValidationResult checkRange(Entity entity, Attribute attribute)
	{
		Range range = attribute.getRange();
		Long value;
		switch (attribute.getDataType())
		{
			case INT:
				Integer intValue = entity.getInt(attribute.getName());
				value = intValue != null ? intValue.longValue() : null;
				break;
			case LONG:
				value = entity.getLong(attribute.getName());
				break;
			default:
				throw new UnexpectedEnumException(attribute.getDataType());
		}
		if ((value != null) && ((range.getMin() != null && value < range.getMin()) || (range.getMax() != null
				&& value > range.getMax())))
		{
			return new AttributeValueValidationResult(RANGE,
					AttributeValue.create(attribute, entity.get(attribute.getName())));
		}

		return null;
	}

	private static AttributeValueValidationResult checkText(Entity entity, Attribute attribute,
			AttributeType fieldType)
	{
		String text = entity.getString(attribute.getName());
		if (text == null)
		{
			return null;
		}

		if (text.length() > fieldType.getMaxLength())
		{
			return new AttributeValueValidationResult(AttributeValueConstraint.MAX_LENGTH,
					AttributeValue.create(attribute, text));
		}

		return null;
	}

	private AttributeValueValidationResult checkEnum(Entity entity, Attribute attribute)
	{
		String value = entity.getString(attribute.getName());
		if (value != null)
		{
			List<String> enumOptions = attribute.getEnumOptions();

			if (!enumOptions.contains(value))
			{
				return new AttributeValueValidationResult(AttributeValueConstraint.ENUM,
						AttributeValue.create(attribute, value));
			}
		}

		return null;
	}
}
