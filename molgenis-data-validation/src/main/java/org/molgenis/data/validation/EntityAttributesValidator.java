package org.molgenis.data.validation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Range;
import org.molgenis.js.ScriptEvaluator;
import org.mozilla.javascript.EcmaError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

/**
 * Attribute data type validator.
 * 
 * Does not check if xref,mref, categorical values are present. That happens in the EntityValidator.
 */
@Component
public class EntityAttributesValidator
{
	private static final Logger LOG = LoggerFactory.getLogger(EntityAttributesValidator.class);
	private EmailValidator emailValidator;

	public Set<ConstraintViolation> validate(Entity entity, EntityMetaData meta)
	{
		Set<ConstraintViolation> violations = Sets.newLinkedHashSet();

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			ConstraintViolation violation = checkValidationExpression(entity, attr, meta);
			if (violation != null) violations.add(violation);

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

	private ConstraintViolation checkValidationExpression(Entity entity, AttributeMetaData attribute,
			EntityMetaData meta)
	{
		if (StringUtils.isNotBlank(attribute.getValidationExpression()))
		{

			Object result = null;
			try
			{
				result = ScriptEvaluator.eval(attribute.getValidationExpression(), entity, meta);
			}
			catch (EcmaError e)
			{
				LOG.warn("Error evaluation validationExpression", e);
			}

			if ((result == null) || !(result instanceof Boolean))
			{
				throw new MolgenisDataException(String.format(
						"Invalid validation expression '%s' for attribute '%s' of entity '%s'",
						attribute.getValidationExpression(), attribute.getName(), meta.getName()));
			}

			if (!(Boolean) result)
			{
				return createConstraintViolation(entity, attribute, meta);
			}
		}

		return null;
	}

	private ConstraintViolation checkEmail(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		if (emailValidator == null)
		{
			emailValidator = new EmailValidator();
		}

		String email = entity.getString(attribute.getName());
		if (emailValidator.isValid(email, null))
		{
			return null;
		}

		return createConstraintViolation(entity, attribute, meta);
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
			return null;
		}
		catch (MalformedURLException e)
		{
			return createConstraintViolation(entity, attribute, meta);
		}

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
		if ((value != null) && ((value < range.getMin()) || (value > range.getMax())))
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
				return createConstraintViolation(entity, attribute, meta);
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

		return new ConstraintViolation(message, entity.getString(attribute.getName()), entity, attribute, meta, 0);
	}
}
