package org.molgenis.data.rsql;

import org.molgenis.data.DateParseException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;

/**
 * Converts RSQL value symbols to the relevant data type.
 */
@Service
public class RSQLValueParser
{
	public Object parse(String valueString, Attribute attribute)
	{
		if (isEmpty(valueString))
		{
			return null;
		}

		AttributeType attrType = attribute.getDataType();
		switch (attrType)
		{
			case BOOL:
				return Boolean.valueOf(valueString);
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return valueString;
			case CATEGORICAL:
			case XREF:
			case FILE:
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				return parse(valueString, attribute.getRefEntity().getIdAttribute());
			case DATE:
				return convertDate(attribute, valueString);
			case DATE_TIME:
				return convertDateTime(attribute, valueString);
			case DECIMAL:
				return Double.valueOf(valueString);
			case INT:
				return Integer.valueOf(valueString);
			case LONG:
				return Long.valueOf(valueString);
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new UnexpectedEnumException(attrType);
		}
	}

	private static Instant convertDateTime(Attribute attr, String paramValue)
	{
		try
		{
			return parseInstant(paramValue);
		}
		catch (java.time.format.DateTimeParseException e)
		{
			throw new DateParseException(attr, paramValue);
		}
	}

	private static LocalDate convertDate(Attribute attr, String paramValue)
	{
		try
		{
			return parseLocalDate(paramValue);
		}
		catch (java.time.format.DateTimeParseException e)
		{
			throw new DateParseException(attr, paramValue);
		}
	}

}
