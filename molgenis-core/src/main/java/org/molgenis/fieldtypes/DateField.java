package org.molgenis.fieldtypes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.util.MolgenisDateFormat;

public class DateField extends FieldType
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
		return "java.sql.Date.valueOf(\"" + value + "\")";
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
		return "DATE";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "DATE";
	}

	@Override
	public String getJavaSetterType() throws MolgenisModelException
	{
		return "Date";
	}

	@Override
	public String getHsqlType()
	{
		return "DATE";
	}

	@Override
	public String getXsdType()
	{
		return "date";
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
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.DATE;
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
		if (value instanceof java.util.Date) return value;
		try
		{
			if (value instanceof String)
			{
				return MolgenisDateFormat.getDateFormat().parse(value.toString());
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("DateField.convert(" + value + ") failed: " + e.getMessage());
		}
		throw new RuntimeException("DateField.convert(" + value + ") failed");
	}
}
