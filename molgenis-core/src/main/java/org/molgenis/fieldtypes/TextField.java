package org.molgenis.fieldtypes;

import java.text.ParseException;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;

public class TextField extends FieldType
{
	private static final long serialVersionUID = 1L;
	private static final long MAX_TEXT_LENGTH = 4294967295L;

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "\"" + value + "\"";
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "LONGTEXT";
	}

	@Override
	public String getOracleType()
	{
		return "VARCHAR";
	}

	@Override
	public String getHsqlType() throws MolgenisModelException
	{
		// these guys don't have TEXT?
		return "VARCHAR";
	}

	@Override
	public String getXsdType() throws MolgenisModelException
	{
		return "text";
	}

	@Override
	public String getJavaPropertyType() throws MolgenisModelException
	{
		return "String";
	}

	@Override
	public String getFormatString()
	{
		return "%s";
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return "string";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/String;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return String.class;
	}

	@Override
	public String getTypedValue(String value) throws ParseException
	{
		return value;
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.TEXT;
	}

	@Override
	public Long getMaxLength()
	{
		return MAX_TEXT_LENGTH;
	}

	@Override
	public Object convert(Object value)
	{
		if (value == null) return null;
		return value.toString();
	}
}
