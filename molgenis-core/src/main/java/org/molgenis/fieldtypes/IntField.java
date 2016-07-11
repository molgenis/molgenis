package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.model.MolgenisModelException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class IntField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaPropertyType() throws MolgenisModelException
	{
		return "Integer";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "" + Integer.parseInt(value);
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "INTEGER";
	}

	@Override
	public String getPostgreSqlType()
	{
		return "integer"; // alias: int, int4
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "NUMBER (10,0)";
	}

	@Override
	public String getJavaSetterType() throws MolgenisModelException
	{
		return "Int";
	}

	@Override
	public String getHsqlType()
	{
		return "INT";
	}

	@Override
	public String getXsdType()
	{
		return "int";
	}

	@Override
	public String getFormatString()
	{
		return "%d";
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return "int";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/Integer;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return Integer.class;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		return Integer.parseInt(value);
	}

	@Override
	public AttributeType getEnumType()
	{
		return AttributeType.INT;
	}

	@Override
	public Long getMaxLength()
	{
		return null;
	}

	@Override
	public List<String> getAllowedOperators()
	{
		return Arrays.asList("EQUALS", "NOT EQUALS", "LESS", "GREATER");
	}

	@Override
	public Object convert(Object value)
	{
		if (value == null) return null;
		if (value instanceof Integer) return value;
		if (value instanceof String) return Integer.parseInt(value.toString());
		if (value instanceof Number) return ((Number) value).intValue();
		throw new RuntimeException("IntField.convert(" + value + ") failed");
	}
}
