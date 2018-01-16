package org.molgenis.data.validation;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;

/**
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

	public Set<ConstraintViolation> validate(Entity entity, EntityType meta)
	{
		Set<ConstraintViolation> violations = checkNullableExpressions(entity, meta);
		violations.addAll(checkValidationExpressions(entity, meta));

		for (Attribute attr : meta.getAtomicAttributes())
		{
			ConstraintViolation violation = null;

			AttributeType attrType = attr.getDataType();
			switch (attrType)
			{
				case EMAIL:
					violation = checkEmail(entity, attr, meta);
					break;
				case BOOL:
					violation = checkBoolean(entity, attr, meta);
					break;
				case DATE:
					violation = checkDate(entity, attr, meta);
					break;
				case DATE_TIME:
					violation = checkDateTime(entity, attr, meta);
					break;
				case DECIMAL:
					violation = checkDecimal(entity, attr, meta);
					break;
				case HYPERLINK:
					violation = checkHyperlink(entity, attr, meta);
					break;
				case INT:
					violation = checkInt(entity, attr, meta);
					if ((violation == null) && (attr.getRange() != null))
					{
						violation = checkRange(entity, attr, meta);
					}
					break;
				case LONG:
					violation = checkLong(entity, attr, meta);
					if ((violation == null) && (attr.getRange() != null))
					{
						violation = checkRange(entity, attr, meta);
					}
					break;
				case ENUM:
					violation = checkEnum(entity, attr, meta);
					break;
				case HTML:
					violation = checkText(entity, attr, meta, HTML);
					break;
				case SCRIPT:
					violation = checkText(entity, attr, meta, SCRIPT);
					break;
				case TEXT:
					violation = checkText(entity, attr, meta, TEXT);
					break;
				case STRING:
					violation = checkText(entity, attr, meta, STRING);
					break;
				case CATEGORICAL:
				case FILE:
				case XREF:
					violation = checkXref(entity, attr, meta);
					break;
				case CATEGORICAL_MREF:
				case MREF:
				case ONE_TO_MANY:
					violation = checkMref(entity, attr, meta);
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

	private ConstraintViolation checkMref(Entity entity, Attribute attr, EntityType entityType)
	{
		Iterable<Entity> refEntities;
		try
		{
			refEntities = entity.getEntities(attr.getName());
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attr, entityType, "Not a valid entity, expected an entity list.");
		}
		if (refEntities == null)
		{
			return createConstraintViolation(entity, attr, entityType, "Not a valid entity, expected an entity list.");
		}
		for (Entity refEntity : refEntities)
		{
			if (refEntity == null)
			{
				return createConstraintViolation(entity, attr, entityType, "Not a valid entity, null is not allowed");
			}
			if (!refEntity.getEntityType().getId().equals(attr.getRefEntity().getId()))
			{
				return createConstraintViolation(entity, attr, entityType, "Not a valid entity type.");
			}
		}
		return null;
	}

	private ConstraintViolation checkXref(Entity entity, Attribute attr, EntityType entityType)
	{
		Entity refEntity;
		try
		{
			refEntity = entity.getEntity(attr.getName());
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attr, entityType, "Not a valid entity.");
		}

		if (refEntity == null)
		{
			return null;
		}
		if (!refEntity.getEntityType().getId().equals(attr.getRefEntity().getId()))
		{
			return createConstraintViolation(entity, attr, entityType, "Not a valid entity type.");
		}
		return null;
	}

	private Set<ConstraintViolation> checkNullableExpressions(Entity entity, EntityType entityType)
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

		Set<ConstraintViolation> violations = new LinkedHashSet<>();

		if (!nullableExpressions.isEmpty())
		{
			List<Boolean> results = expressionValidator.resolveBooleanExpressions(nullableExpressions, entity);
			for (int i = 0; i < results.size(); i++)
			{
				if (!results.get(i) && EntityUtils.isNullValue(entity, expressionAttributes.get(i)))
				{
					violations.add(createConstraintViolation(entity, expressionAttributes.get(i), entityType,
							format("Offended nullable expression: %s", nullableExpressions.get(i))));
				}
			}
		}

		return violations;
	}

	private Set<ConstraintViolation> checkValidationExpressions(Entity entity, EntityType meta)
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

		Set<ConstraintViolation> violations = new LinkedHashSet<>();

		if (!validationExpressions.isEmpty())
		{
			List<Boolean> results = expressionValidator.resolveBooleanExpressions(validationExpressions, entity);
			for (int i = 0; i < results.size(); i++)
			{
				if (!results.get(i))
				{
					violations.add(createConstraintViolation(entity, expressionAttributes.get(i), meta,
							format("Offended validation expression: %s", validationExpressions.get(i))));
				}
			}
		}

		return violations;
	}

	private ConstraintViolation checkEmail(Entity entity, Attribute attribute, EntityType entityType)
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
			return createConstraintViolation(entity, attribute, entityType, "Not a valid e-mail address.");
		}

		if (email.length() > EMAIL.getMaxLength())
		{
			return createConstraintViolation(entity, attribute, entityType);
		}

		return null;
	}

	private static ConstraintViolation checkBoolean(Entity entity, Attribute attribute, EntityType entityType)
	{
		try
		{
			entity.getBoolean(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, entityType);
		}
	}

	private static ConstraintViolation checkDateTime(Entity entity, Attribute attribute, EntityType entityType)
	{
		try
		{
			entity.getInstant(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, entityType);
		}
	}

	private static ConstraintViolation checkDate(Entity entity, Attribute attribute, EntityType entityType)
	{
		try
		{
			entity.getLocalDate(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, entityType);
		}
	}

	private static ConstraintViolation checkDecimal(Entity entity, Attribute attribute, EntityType entityType)
	{
		try
		{
			entity.getDouble(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, entityType);
		}
	}

	private ConstraintViolation checkHyperlink(Entity entity, Attribute attribute, EntityType entityType)
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
			return createConstraintViolation(entity, attribute, entityType, "Not a valid hyperlink.");
		}

		if (link.length() > HYPERLINK.getMaxLength())
		{
			return createConstraintViolation(entity, attribute, entityType);
		}

		return null;
	}

	private static ConstraintViolation checkInt(Entity entity, Attribute attribute, EntityType entityType)
	{
		try
		{
			entity.getInt(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, entityType);
		}
	}

	private static ConstraintViolation checkLong(Entity entity, Attribute attribute, EntityType entityType)
	{
		try
		{
			entity.getLong(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, entityType);
		}
	}

	private static ConstraintViolation checkRange(Entity entity, Attribute attr, EntityType entityType)
	{
		Range range = attr.getRange();
		Long value;
		switch (attr.getDataType())
		{
			case INT:
				Integer intValue = entity.getInt(attr.getName());
				value = intValue != null ? intValue.longValue() : null;
				break;
			case LONG:
				value = entity.getLong(attr.getName());
				break;
			default:
				throw new RuntimeException(
						format("Range not allowed for data type [%s]", attr.getDataType().toString()));
		}
		if ((value != null) && ((range.getMin() != null && value < range.getMin()) || (range.getMax() != null
				&& value > range.getMax())))
		{
			return createConstraintViolation(entity, attr, entityType);
		}

		return null;
	}

	private static ConstraintViolation checkText(Entity entity, Attribute attribute, EntityType meta,
			AttributeType fieldType)
	{
		String text = entity.getString(attribute.getName());
		if (text == null)
		{
			return null;
		}

		if (text.length() > fieldType.getMaxLength())
		{
			return createConstraintViolation(entity, attribute, meta);
		}

		return null;
	}

	private ConstraintViolation checkEnum(Entity entity, Attribute attribute, EntityType entityType)
	{
		String value = entity.getString(attribute.getName());
		if (value != null)
		{
			List<String> enumOptions = attribute.getEnumOptions();

			if (!enumOptions.contains(value))
			{
				return createConstraintViolation(entity, attribute, entityType,
						"Value must be one of " + enumOptions.toString());
			}
		}

		return null;
	}

	private static ConstraintViolation createConstraintViolation(Entity entity, Attribute attribute,
			EntityType entityType)
	{
		String message = format("Invalid %s value '%s' for attribute '%s' of entity '%s'.",
				attribute.getDataType().toString().toLowerCase(), entity.get(attribute.getName()), attribute.getLabel(),
				entityType.getId());

		Range range = attribute.getRange();
		if (range != null)
		{
			message += format("Value must be between %d and %d", range.getMin(), range.getMax());
		}

		Long maxLength = attribute.getDataType().getMaxLength();
		if (maxLength != null)
		{
			message += format("Value must be less than or equal to %d characters", maxLength);
		}

		return new ConstraintViolation(message);
	}

	private ConstraintViolation createConstraintViolation(Entity entity, Attribute attribute, EntityType entityType,
			String message)
	{
		Object value = getDataValuesForType(entity, attribute);
		String dataValue = value != null ? value.toString() : null;
		String fullMessage = format("Invalid [%s] value [%s] for attribute [%s] of entity [%s] with type [%s].",
				attribute.getDataType().toString().toLowerCase(), dataValue, attribute.getLabel(),
				entity.getLabelValue(), entityType.getId());
		fullMessage += " " + message;

		return new ConstraintViolation(fullMessage);
	}

	private Object getDataValuesForType(Entity entity, Attribute attribute)
	{
		String attributeName = attribute.getName();
		switch (attribute.getDataType())
		{
			case DATE:
				return entity.getLocalDate(attributeName);
			case DATE_TIME:
				return entity.getInstant(attributeName);
			case BOOL:
				return entity.getBoolean(attributeName);
			case DECIMAL:
			case LONG:
			case INT:
				return entity.getInt(attributeName);
			case HYPERLINK:
			case ENUM:
			case HTML:
			case TEXT:
			case SCRIPT:
			case EMAIL:
			case STRING:
				return entity.getString(attributeName);
			case CATEGORICAL:
			case XREF:
			case FILE:
				Entity refEntity = entity.getEntity(attributeName);
				if (refEntity != null) return refEntity.getIdValue();
				else return "";
			case CATEGORICAL_MREF:
			case MREF:
				List<String> mrefValues = newArrayList();
				for (Entity mrefEntity : entity.getEntities(attributeName))
				{
					if (mrefEntity != null)
					{
						mrefValues.add(mrefEntity.getIdValue().toString());
					}
				}
				return mrefValues;
			case COMPOUND:
				return "";
			default:
				return "";
		}
	}
}
