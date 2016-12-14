package org.molgenis.data.populate;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityReferenceCreator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.ListEscapeUtils;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.util.EntityUtils.asStream;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;

/**
 * Populate entity values for attributes with default values
 */
@Component
public class DefaultValuePopulator
{
	private final EntityReferenceCreator entityReferenceCreator;

	@Autowired
	public DefaultValuePopulator(EntityReferenceCreator entityReferenceCreator)
	{
		this.entityReferenceCreator = requireNonNull(entityReferenceCreator);
	}

	/**
	 * Populates an entity with default values
	 *
	 * @param entity populated entity
	 */
	public void populate(Entity entity)
	{
		asStream(entity.getEntityType().getAllAttributes()).filter(Attribute::hasDefaultValue)
				.forEach(attr -> populateDefaultValues(entity, attr));
	}

	private void populateDefaultValues(Entity entity, Attribute attr)
	{
		Object defaultValueAsString = getDefaultValue(attr);
		entity.set(attr.getName(), defaultValueAsString);
	}

	private Object getDefaultValue(Attribute attr)
	{
		String valueAsString = attr.getDefaultValue();
		return convertToTypedValue(attr, valueAsString);
	}

	private Object convertToTypedValue(Attribute attr, String valueAsString)
	{
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case BOOL:
				return convertBool(attr, valueAsString);
			case CATEGORICAL:
			case FILE:
			case XREF:
				return convertRef(attr, valueAsString);
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				return convertMref(attr, valueAsString);
			case DATE:
				return convertDate(attr, valueAsString);
			case DATE_TIME:
				return convertDateTime(attr, valueAsString);
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
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
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

	private static Date convertDate(Attribute attr, String valueAsString)
	{
		try
		{
			return getDateFormat().parse(valueAsString);
		}
		catch (ParseException e)
		{
			throw new RuntimeException(
					format("Attribute [%s] value [%s] does not match date format [%s]", attr.getName(), valueAsString,
							MolgenisDateFormat.getDateFormat().toPattern()));
		}
	}

	private static Date convertDateTime(Attribute attr, String valueAsString)
	{
		try
		{
			return getDateTimeFormat().parse(valueAsString);
		}
		catch (ParseException e)
		{
			throw new RuntimeException(
					format("Attribute [%s] value [%s] does not match date format [%s]", attr.getName(), valueAsString,
							MolgenisDateFormat.getDateTimeFormat().toPattern()));
		}
	}

	private static Boolean convertBool(Attribute attr, String valueAsString)
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
					format("Attribute [%s] value [%s] cannot be converter to type [%s]", attr.getName(), valueAsString,
							Boolean.class.getSimpleName()));
		}
	}

	private Entity convertRef(Attribute attr, String idValueAsString)
	{
		EntityType refEntityType = attr.getRefEntity();
		Object idValue = convertToTypedValue(refEntityType.getIdAttribute(), idValueAsString);
		return entityReferenceCreator.getReference(refEntityType, idValue);
	}

	private List<Entity> convertMref(Attribute attr, String idValuesAsString)
	{
		List<String> valuesAsString = ListEscapeUtils.toList(idValuesAsString);
		return valuesAsString.stream().map(refValueAsString -> convertRef(attr, refValueAsString)).collect(toList());
	}
}
