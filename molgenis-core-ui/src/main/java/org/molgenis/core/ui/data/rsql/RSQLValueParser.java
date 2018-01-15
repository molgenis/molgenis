package org.molgenis.core.ui.data.rsql;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.data.util.MolgenisDateFormat.*;

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
		catch (DateTimeParseException e)
		{
			throw new MolgenisDataException(
					format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATETIME_MESSAGE, attr.getName(), paramValue));
		}
	}

	private static LocalDate convertDate(Attribute attr, String paramValue)
	{
		try
		{
			return parseLocalDate(paramValue);
		}
		catch (DateTimeParseException e)
		{
			throw new MolgenisDataException(
					format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATE_MESSAGE, attr.getName(), paramValue));
		}
	}

}
