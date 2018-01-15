package org.molgenis.data.validation;

import org.molgenis.data.*;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.MolgenisDateFormat;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.util.MolgenisDateFormat.*;

/**
 * Validates {@link Query queries} based on the {@link EntityType entity type} that will be queried. Converts query
 * values to the correct class type if possible.
 *
 * @see <a href="https://github.com/molgenis/molgenis/issues/5248">https://github.com/molgenis/molgenis/issues/5248</a>
 */
@Component
public class QueryValidator
{
	private final EntityManager entityManager;

	public QueryValidator(EntityManager entityManager)
	{
		this.entityManager = requireNonNull(entityManager);
	}

	/**
	 * Validates query based on the given entity type, converts query values to the expected type if necessary.
	 *
	 * @param query      query
	 * @param entityType entity type
	 * @throws MolgenisValidationException if query is invalid
	 */
	public void validate(Query<? extends Entity> query, EntityType entityType)
	{
		query.getRules().forEach(queryRule -> validateQueryRule(queryRule, entityType));
	}

	private void validateQueryRule(QueryRule queryRule, EntityType entityType)
	{
		QueryRule.Operator operator = queryRule.getOperator();
		switch (operator)
		{
			case AND:
			case NOT:
			case OR:
				break;
			case EQUALS:
			case FUZZY_MATCH:
			case FUZZY_MATCH_NGRAM:
			case GREATER:
			case GREATER_EQUAL:
			case LESS:
			case LESS_EQUAL:
			case LIKE:
			{
				Attribute attr = getQueryRuleAttribute(queryRule, entityType);
				Object value = toQueryRuleValue(queryRule.getValue(), attr);
				queryRule.setValue(value);
				break;
			}
			case SEARCH:
			{
				Object queryRuleValue = queryRule.getValue();
				if (queryRuleValue != null && !(queryRuleValue instanceof String))
				{
					// fix value type
					queryRule.setValue(queryRuleValue.toString());
				}
				break;
			}
			case IN:
			case RANGE:
			{
				Attribute attr = getQueryRuleAttribute(queryRule, entityType);
				Object queryRuleValue = queryRule.getValue();
				if (queryRuleValue != null)
				{
					if (!(queryRuleValue instanceof Iterable<?>))
					{
						throw new MolgenisValidationException(new ConstraintViolation(
								format("Query rule with operator [%s] value is of type [%s] instead of [Iterable]",
										operator, queryRuleValue.getClass().getSimpleName())));
					}

					// fix value types
					Iterable<?> queryRuleValues = (Iterable<?>) queryRuleValue;
					List<Object> values = stream(queryRuleValues.spliterator(), false).map(
							value -> toQueryRuleValue(value, attr)).collect(toList());
					queryRule.setValue(values);
				}
				break;
			}
			case DIS_MAX:
			case NESTED:
			case SHOULD:
				queryRule.getNestedRules().forEach(nestedQueryRule -> validateQueryRule(nestedQueryRule, entityType));
				break;
			default:
				throw new UnexpectedEnumException(operator);
		}
	}

	private Attribute getQueryRuleAttribute(QueryRule queryRule, EntityType entityType)
	{
		try
		{
			return QueryUtils.getQueryRuleAttribute(queryRule, entityType);
		}
		catch (UnknownAttributeException e)
		{
			throw new MolgenisValidationException(new ConstraintViolation(e.getMessage()));
		}
	}

	private Object toQueryRuleValue(Object queryRuleValue, Attribute attr)
	{
		Object value;
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case BOOL:
				value = convertBool(attr, queryRuleValue);
				break;
			case EMAIL:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				value = convertString(attr, queryRuleValue);
				break;
			case ENUM:
				value = convertEnum(attr, queryRuleValue);
				break;
			case CATEGORICAL:
			case XREF:
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				value = convertRef(attr, queryRuleValue);
				break;
			case DATE:
				value = convertDate(attr, queryRuleValue);
				break;
			case DATE_TIME:
				value = convertDateTime(attr, queryRuleValue);
				break;
			case DECIMAL:
				value = convertDecimal(attr, queryRuleValue);
				break;
			case FILE:
				value = convertFile(attr, queryRuleValue);
				break;
			case INT:
				value = convertInt(attr, queryRuleValue);
				break;
			case LONG:
				value = convertLong(attr, queryRuleValue);
				break;
			case COMPOUND:
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Attribute [%s] type [%s] is not allowed", attr.getName(), attrType.toString())));
			default:
				throw new UnexpectedEnumException(attrType);
		}
		return value;
	}

	private static String convertEnum(Attribute attr, Object value)
	{
		if (value == null)
		{
			return null;
		}

		String stringValue;
		if (value instanceof String)
		{
			stringValue = (String) value;
		}
		else if (value instanceof Enum)
		{
			stringValue = value.toString();
		}
		else
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
							value.getClass().getSimpleName(), String.class.getSimpleName(),
							Enum.class.getSimpleName())));
		}

		if (!attr.getEnumOptions().contains(stringValue))
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value [%s] is not a valid enum option", attr.getName(), stringValue)));
		}

		return stringValue;
	}

	private static Long convertLong(Attribute attr, Object value)
	{
		if (value instanceof Long)
		{
			return (Long) value;
		}

		if (value == null)
		{
			return null;
		}

		// try to convert value
		Long longValue;
		if (value instanceof String)
		{
			try
			{
				longValue = Long.valueOf((String) value);
			}
			catch (NumberFormatException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Attribute [%s] value [%s] cannot be converter to type [%s]", attr.getName(), value,
								Long.class.getSimpleName())));
			}
		}
		else if (value instanceof Number)
		{
			longValue = ((Number) value).longValue();
		}
		else
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
							value.getClass().getSimpleName(), String.class.getSimpleName(),
							Number.class.getSimpleName())));
		}
		return longValue;
	}

	private static Integer convertInt(Attribute attr, Object value)
	{
		if (value instanceof Integer)
		{
			return (Integer) value;
		}

		if (value == null)
		{
			return null;
		}

		// try to convert value
		Integer integerValue;
		if (value instanceof String)
		{
			try
			{
				integerValue = Integer.valueOf((String) value);
			}
			catch (NumberFormatException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Attribute [%s] value [%s] cannot be converter to type [%s]", attr.getName(), value,
								Integer.class.getSimpleName())));
			}
		}
		else if (value instanceof Number)
		{
			integerValue = ((Number) value).intValue();
		}
		else
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
							value.getClass().getSimpleName(), String.class.getSimpleName(),
							Number.class.getSimpleName())));
		}
		return integerValue;
	}

	private FileMeta convertFile(Attribute attr, Object paramValue)
	{
		Entity entity = convertRef(attr, paramValue);
		if (entity == null)
		{
			return null;
		}
		if (!(entity instanceof FileMeta))
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value is of type [%s] instead of [%s]", attr.getName(),
							entity.getClass().getSimpleName(), FileMeta.class.getSimpleName())));
		}
		return (FileMeta) entity;
	}

	private static Double convertDecimal(Attribute attr, Object value)
	{
		if (value instanceof Double)
		{
			return (Double) value;
		}

		if (value == null)
		{
			return null;
		}

		// try to convert value
		Double doubleValue;
		if (value instanceof String)
		{
			try
			{
				doubleValue = Double.valueOf((String) value);
			}
			catch (NumberFormatException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Attribute [%s] value [%s] cannot be converter to type [%s]", attr.getName(), value,
								Double.class.getSimpleName())));
			}
		}
		else if (value instanceof Number)
		{
			doubleValue = ((Number) value).doubleValue();
		}
		else
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
							value.getClass().getSimpleName(), String.class.getSimpleName(),
							Number.class.getSimpleName())));
		}
		return doubleValue;
	}

	private static Instant convertDateTime(Attribute attr, Object value)
	{
		if (value instanceof Instant)
		{
			return (Instant) value;
		}

		if (value == null)
		{
			return null;
		}

		// try to convert value
		Instant dateValue;
		if (value instanceof String)
		{
			String paramStrValue = (String) value;
			try
			{
				dateValue = MolgenisDateFormat.parseInstant(paramStrValue);
			}
			catch (DateTimeParseException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATETIME_MESSAGE, attr.getName(), paramStrValue)));
			}
		}
		else
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
							value.getClass().getSimpleName(), String.class.getSimpleName(),
							Instant.class.getSimpleName())));
		}
		return dateValue;
	}

	private static LocalDate convertDate(Attribute attr, Object value)
	{
		if (value instanceof LocalDate)
		{
			return (LocalDate) value;
		}

		if (value == null)
		{
			return null;
		}

		// try to convert value
		LocalDate dateValue;
		if (value instanceof String)
		{
			String paramStrValue = (String) value;
			try
			{
				dateValue = parseLocalDate(paramStrValue);
			}
			catch (DateTimeParseException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATE_MESSAGE, attr.getName(), paramStrValue)));
			}
		}
		else
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value is of type [%s] instead of [%s] or [%s].", attr.getName(),
							value.getClass().getSimpleName(), LocalDate.class.getSimpleName(),
							String.class.getSimpleName())));
		}
		return dateValue;
	}

	private Entity convertRef(Attribute attr, Object value)
	{
		if (value instanceof Entity)
		{
			return (Entity) value;
		}

		if (value == null)
		{
			return null;
		}

		// try to convert value
		Object idValue = toQueryRuleValue(value, attr.getRefEntity().getIdAttribute());
		return entityManager.getReference(attr.getRefEntity(), idValue);
	}

	private static String convertString(@SuppressWarnings("unused") Attribute attr, Object value)
	{
		if (value instanceof String)
		{
			return (String) value;
		}

		if (value == null)
		{
			return null;
		}

		return value.toString();
	}

	private static Boolean convertBool(Attribute attr, Object value)
	{
		if (value instanceof Boolean)
		{
			return (Boolean) value;
		}

		if (value == null)
		{
			return null;
		}

		Boolean booleanValue;
		if (value instanceof String)
		{
			String stringValue = (String) value;
			if (stringValue.equalsIgnoreCase(TRUE.toString()))
			{
				booleanValue = true;
			}
			else if (stringValue.equalsIgnoreCase(FALSE.toString()))
			{
				booleanValue = false;
			}
			else
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Attribute [%s] value [%s] cannot be converter to type [%s]", attr.getName(), value,
								Boolean.class.getSimpleName())));
			}
		}
		else
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
							value.getClass().getSimpleName(), String.class.getSimpleName(),
							Boolean.class.getSimpleName())));
		}
		return booleanValue;
	}
}
