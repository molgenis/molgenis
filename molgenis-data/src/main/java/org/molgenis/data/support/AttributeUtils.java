package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityReferenceCreator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.ListEscapeUtils;
import org.molgenis.util.UnexpectedEnumException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.util.EntityUtils.getTypedValue;
import static org.molgenis.data.util.MolgenisDateFormat.*;
import static org.springframework.util.StringUtils.capitalize;

public class AttributeUtils
{
	private static final EntityReferenceCreator DEFAULT_ENTITY_REFERENCE_CREATOR = new DefaultEntityReferenceCreator();

	private AttributeUtils()
	{
	}

	public static String getI18nAttributeName(String attrName, String languageCode)
	{
		return attrName + capitalize(languageCode.toLowerCase());
	}

	/**
	 * Returns whether this attribute can be used as ID attribute
	 *
	 * @param attr attribute
	 * @return true if this attribute can be used as ID attribute
	 */
	public static boolean isIdAttributeTypeAllowed(Attribute attr)

	{
		return getValidIdAttributeTypes().contains(attr.getDataType());
	}

	public static Set<AttributeType> getValidIdAttributeTypes()
	{
		return EnumSet.of(STRING, INT, LONG, EMAIL, HYPERLINK);
	}

	/**
	 * Returns the attribute default value string as typed value corresponding to the attribute type.
	 */
	public static Object getDefaultTypedValue(Attribute attribute)
	{
		return getDefaultTypedValue(attribute, DEFAULT_ENTITY_REFERENCE_CREATOR);
	}

	public static Object getDefaultTypedValue(Attribute attribute, EntityReferenceCreator entityReferenceCreator)
	{
		return getDefaultTypedValue(attribute, attribute.getDefaultValue(), entityReferenceCreator);
	}

	public static Object getDefaultTypedValue(Attribute attribute, String valueAsString)
	{
		return getDefaultTypedValue(attribute, valueAsString, DEFAULT_ENTITY_REFERENCE_CREATOR);
	}

	public static Object getDefaultTypedValue(Attribute attribute, String valueAsString,
			EntityReferenceCreator entityReferenceCreator)
	{
		AttributeType attrType = attribute.getDataType();
		switch (attrType)
		{
			case BOOL:
				return convertBool(attribute, valueAsString);
			case CATEGORICAL:
			case FILE:
			case XREF:
				return convertRef(attribute, valueAsString, entityReferenceCreator);
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				return convertMref(attribute, valueAsString, entityReferenceCreator);
			case DATE:
				return convertDate(attribute, valueAsString);
			case DATE_TIME:
				return convertDateTime(attribute, valueAsString);
			case DECIMAL:
				return convertDecimal(valueAsString);
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return valueAsString;
			case INT:
				return convertInt(valueAsString);
			case LONG:
				return convertLong(valueAsString);
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new UnexpectedEnumException(attrType);
		}
	}

	private static Long convertLong(String valueAsString)
	{
		return Long.valueOf(valueAsString);
	}

	private static Integer convertInt(String valueAsString)
	{
		return Integer.valueOf(valueAsString);
	}

	private static Double convertDecimal(String valueAsString)
	{
		return Double.valueOf(valueAsString);
	}

	private static LocalDate convertDate(Attribute attribute, String valueAsString)
	{
		try
		{
			return parseLocalDate(valueAsString);
		}
		catch (DateTimeParseException e)
		{
			throw new RuntimeException(
					format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATE_MESSAGE, attribute.getName(), valueAsString));
		}
	}

	private static Instant convertDateTime(Attribute attribute, String valueAsString)
	{
		try
		{
			return parseInstant(valueAsString);
		}
		catch (DateTimeParseException e)
		{
			throw new RuntimeException(
					format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATETIME_MESSAGE, attribute.getName(), valueAsString));
		}
	}

	private static Boolean convertBool(Attribute attribute, String valueAsString)
	{
		if (valueAsString.equalsIgnoreCase(TRUE.toString()))
		{
			return true;
		}
		else if (valueAsString.equalsIgnoreCase(FALSE.toString()))
		{
			return false;
		}
		else
		{
			throw new RuntimeException(
					format("Attribute [%s] value [%s] cannot be converter to type [%s]", attribute.getName(),
							valueAsString, Boolean.class.getSimpleName()));
		}
	}

	private static Entity convertRef(Attribute attribute, String idValueAsString,
			EntityReferenceCreator entityReferenceCreator)
	{
		EntityType refEntityType = attribute.getRefEntity();
		Object idValue = getTypedValue(idValueAsString, refEntityType.getIdAttribute());
		return entityReferenceCreator.getReference(refEntityType, idValue);
	}

	private static Iterable<Entity> convertMref(Attribute attribute, String idValuesAsString,
			EntityReferenceCreator entityReferenceCreator)
	{
		List<String> valuesAsString = ListEscapeUtils.toList(idValuesAsString);
		EntityType refEntityType = attribute.getRefEntity();
		Attribute refIdAttribute = refEntityType.getIdAttribute();
		List<Object> idValues = valuesAsString.stream()
											  .map(idValueAsString -> getTypedValue(idValueAsString, refIdAttribute))
											  .collect(toList());
		return entityReferenceCreator.getReferences(refEntityType, idValues);
	}

	private static class DefaultEntityReferenceCreator implements EntityReferenceCreator
	{
		@Override
		public Entity getReference(EntityType entityType, Object id)
		{
			return new EntityReference(entityType, id);
		}

		@Override
		public Iterable<Entity> getReferences(EntityType entityType, Iterable<?> ids)
		{
			return () -> stream(ids.spliterator(), false).map(id -> getReference(entityType, id)).iterator();
		}
	}
}
