package org.molgenis.data.rsql;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;

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
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	private static Date convertDateTime(Attribute attr, String paramValue)
	{
		try
		{
			return getDateTimeFormat().parse(paramValue);
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException(
					format("Attribute [%s] value [%s] does not match date format [%s]", attr.getName(), paramValue,
							MolgenisDateFormat.getDateTimeFormat().toPattern()));
		}
	}

	private static Date convertDate(Attribute attr, String paramValue)
	{
		try
		{
			return getDateFormat().parse(paramValue);
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException(
					format("Attribute [%s] value [%s] does not match date format [%s].", attr.getName(), paramValue,
							MolgenisDateFormat.getDateFormat().toPattern()));
		}
	}

}
