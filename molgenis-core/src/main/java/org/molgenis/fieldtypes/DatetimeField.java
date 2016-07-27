package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.util.MolgenisDateFormat;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DatetimeField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaPropertyType() throws MolgenisModelException
	{
		return "java.util.Date";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "java.sql.Timestamp.valueOf(\"" + value + "\")";
	}

	@Override
	public String getJavaPropertyDefault()
	{
		if (f.isAuto()) return "new java.sql.Date(new java.util.Date().getTime())";
		else return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "DATETIME";
	}

	@Override
	public String getPostgreSqlType()
	{
		return "timestamp";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "DATE";
	}

	@Override
	public String getXsdType()
	{
		return "dateTime";
	}

	@Override
	public String getJavaSetterType() throws MolgenisModelException
	{
		return "Timestamp";
	}

	@Override
	public String getHsqlType()
	{
		return "DATETIME";
	}

	@Override
	public String getFormatString()
	{
		return "%s";
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return "time_t";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/util/Date;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return Date.class;
	}

	@Override
	public Date getTypedValue(String value) throws ParseException
	{
		return new SimpleDateFormat("yyyy.MM.dd G HH:mm:ss").parse(value);
	}

	@Override
	public AttributeType getEnumType()
	{
		return AttributeType.DATE_TIME;
	}

	@Override
	public Long getMaxLength()
	{
		return null;
	}

	@Override
	public List<String> getAllowedOperators()
	{
		return Arrays.asList("EQUALS", "NOT EQUALS", "LESS", "GREATER", "LIKE");
	}

	@Override
	public Object convert(Object value)
	{
		if (value == null) return null;
		if (value instanceof java.sql.Timestamp) return new java.util.Date(((Timestamp) value).getTime());
		if (value instanceof java.util.Date) return value;
		if (value instanceof String)
		{
			try
			{
				return MolgenisDateFormat.getDateTimeFormatSimple().parse(value.toString());
			}
			catch (Exception e)
			{
				try
				{
					return MolgenisDateFormat.getDateTimeFormat().parse(value.toString());
				}
				catch (Exception e1)
				{
					throw new RuntimeException("DateField.convert(" + value + ") failed: " + e1.getMessage());
				}
			}
		}
		throw new RuntimeException("DateField.convert(" + value + ") failed");
	}
}
