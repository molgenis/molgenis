package org.molgenis.data.validation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.FieldType;
import org.springframework.stereotype.Component;

/**
 * Attribute data type validator.
 * 
 * Does not check if xref,mref, categorical values are present. That happens in the EntityValidator.
 */
@Component
public class EntityAttributesValidator
{
	private EmailValidator emailValidator;

	public Set<ConstraintViolation> validate(Entity entity, EntityMetaData meta)
	{
		Set<ConstraintViolation> violations = checkValidationExpressions(entity, meta);

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			ConstraintViolation violation = null;

			switch (attr.getDataType().getEnumType())
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
					violation = checkText(entity, attr, meta, MolgenisFieldTypes.HTML);
					break;
				case SCRIPT:
					violation = checkText(entity, attr, meta, MolgenisFieldTypes.SCRIPT);
					break;
				case TEXT:
					violation = checkText(entity, attr, meta, MolgenisFieldTypes.TEXT);
					break;
				case STRING:
					violation = checkText(entity, attr, meta, MolgenisFieldTypes.STRING);
					break;
				default:
					break;
			}

			if (violation != null)
			{
				violations.add(violation);
			}
		}

		return violations;
	}

	private Set<ConstraintViolation> checkValidationExpressions(Entity entity, EntityMetaData meta)
	{
		List<String> validationExpressions = new ArrayList<>();
		List<AttributeMetaData> expressionAttributes = new ArrayList<>();

		for (AttributeMetaData attribute : meta.getAtomicAttributes())
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
			List<Boolean> results = ValidationUtils.resolveBooleanExpressions(validationExpressions, entity, meta);
			for (int i = 0; i < results.size(); i++)
			{
				if (!results.get(i))
				{
					violations.add(createConstraintViolation(entity, expressionAttributes.get(i), meta));
				}
			}
		}

		return violations;
	}

	private ConstraintViolation checkEmail(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
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
			return createConstraintViolation(entity, attribute, meta, "Not a valid e-mail address.");
		}

		if (email.length() > MolgenisFieldTypes.EMAIL.getMaxLength())
		{
			return createConstraintViolation(entity, attribute, meta);
		}

		return null;
	}

	private ConstraintViolation checkBoolean(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		try
		{
			entity.getBoolean(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, meta);
		}
	}

	private ConstraintViolation checkDateTime(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		try
		{
			entity.getUtilDate(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, meta);
		}
	}

	private ConstraintViolation checkDate(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		try
		{
			entity.getDate(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, meta);
		}
	}

	private ConstraintViolation checkDecimal(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		try
		{
			entity.getDouble(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, meta);
		}
	}

	private ConstraintViolation checkHyperlink(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		String link = entity.getString(attribute.getName());
		if (link == null)
		{
			return null;
		}

		try
		{
			new URL(link);
		}
		catch (MalformedURLException e)
		{
			return createConstraintViolation(entity, attribute, meta, "Not a valid hyperlink.");
		}

		if (link.length() > MolgenisFieldTypes.HYPERLINK.getMaxLength())
		{
			return createConstraintViolation(entity, attribute, meta);
		}

		return null;
	}

	private ConstraintViolation checkInt(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		try
		{
			entity.getInt(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, meta);
		}
	}

	private ConstraintViolation checkLong(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		try
		{
			entity.getLong(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			return createConstraintViolation(entity, attribute, meta);
		}
	}

	private ConstraintViolation checkRange(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		Range range = attribute.getRange();
		Long value = entity.getLong(attribute.getName());
		if ((value != null)
				&& ((range.getMin() != null && value < range.getMin()) || (range.getMax() != null && value > range
						.getMax())))
		{
			return createConstraintViolation(entity, attribute, meta);
		}

		return null;
	}

	private ConstraintViolation checkText(Entity entity, AttributeMetaData attribute, EntityMetaData meta,
			FieldType fieldType)
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

	private ConstraintViolation checkEnum(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		String value = entity.getString(attribute.getName());
		if (value != null)
		{
			List<String> enumOptions = attribute.getEnumOptions();

			// Keep OMX/JPA happy
			if (enumOptions == null)
			{
				return null;
			}

			if (!enumOptions.contains(value))
			{
				return createConstraintViolation(entity, attribute, meta,
						"Value must be one of " + enumOptions.toString());
			}
		}

		return null;
	}

	private ConstraintViolation createConstraintViolation(Entity entity, AttributeMetaData attribute,
			EntityMetaData meta)
	{
		String message = String.format("Invalid %s value '%s' for attribute '%s' of entity '%s'.", attribute
				.getDataType().getEnumType().toString().toLowerCase(), entity.getString(attribute.getName()),
				attribute.getLabel(), meta.getName());

		Range range = attribute.getRange();
		if (range != null)
		{
			message += String.format("Value must be between %d and %d", range.getMin(), range.getMax());
		}

		Long maxLength = attribute.getDataType().getMaxLength();
		if (maxLength != null)
		{
			message += String.format("Value must be less than or equal to %d characters", maxLength);
		}

		return new ConstraintViolation(message, entity.getString(attribute.getName()), entity, attribute, meta, null);
	}

	private ConstraintViolation createConstraintViolation(Entity entity, AttributeMetaData attribute,
			EntityMetaData meta, String message)
	{
		String fullMessage = String.format("Invalid %s value '%s' for attribute '%s' of entity '%s'.", attribute
				.getDataType().getEnumType().toString().toLowerCase(), entity.getString(attribute.getName()),
				attribute.getLabel(), meta.getName());
		fullMessage += " " + message;

		return new ConstraintViolation(fullMessage, entity.getString(attribute.getName()), entity, attribute, meta,
				null);
	}
}
