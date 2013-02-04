package org.molgenis.fieldtypes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.ui.html.DatetimeInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.model.MolgenisModelException;

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
		else
			return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "DATETIME";
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
	public HtmlInput<?> createInput(String name, String xrefEntityClassName) throws HtmlInputException
	{
		return new DatetimeInput(name);
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
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.DATE_TIME;
	}

	@Override
	public List<String> getAllowedOperators()
	{
		return Arrays.asList("EQUALS", "NOT EQUALS", "LESS", "GREATER", "LIKE");
	}
}
