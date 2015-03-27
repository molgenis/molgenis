package org.molgenis.fieldtypes;

import java.text.ParseException;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;

public class StringField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaAssignment(String value) throws MolgenisModelException
	{
		if (value == null || value.equals("")) return "null";
		return "\"" + value + "\"";
	}

	@Override
	public String getJavaPropertyDefault() throws MolgenisModelException
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getJavaPropertyType() throws MolgenisModelException
	{
		return "String";
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		// Changed from varchar to text to allow for more columns
		return "TEXT";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "VARCHAR2(" + f.getVarCharLength() + ")";
	}

	@Override
	public String getHsqlType() throws MolgenisModelException
	{
		return "VARCHAR(" + f.getVarCharLength() + ")";
	}

	@Override
	public String getXsdType() throws MolgenisModelException
	{
		return "string";
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
		return FieldTypeEnum.STRING;
	}

	@Override
	public Object convert(Object value)
	{
		if (value == null) return null;
		return value.toString();
	}

}
