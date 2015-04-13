package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;

public class BoolField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaPropertyType()
	{
		return "Boolean";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "" + Boolean.parseBoolean(value.toString());
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "BOOL";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "CHAR";
	}

	@Override
	public String getHsqlType()
	{
		return "INTEGER";
	}

	@Override
	public String getXsdType()
	{
		return "boolean";
	}

	@Override
	public String getFormatString()
	{
		return "%d";
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return "bool";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/Boolean;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return Boolean.class;
	}

	@Override
	public Boolean getTypedValue(String value)
	{
		return Boolean.parseBoolean(value);
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.BOOL;
	}

	@Override
	public Long getMaxLength()
	{
		return null;
	}

	@Override
	public Object convert(Object value)
	{
		if (value == null) return null;
		if (value instanceof Boolean) return (Boolean) value;
		if (value instanceof String) return Boolean.parseBoolean(value.toString());
		if (value instanceof Integer) return (Integer) value > 0;
		throw new RuntimeException("BoolField.convert(" + value + ") failed");
	}

}
