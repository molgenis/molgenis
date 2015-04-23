package org.molgenis.fieldtypes;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;

public class DecimalField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaPropertyType()
	{
		return "Double";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "" + Double.parseDouble(value);
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "DOUBLE";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "NUMBER";
	}

	@Override
	public String getHsqlType()
	{
		return "DOUBLE";
	}

	@Override
	public String getXsdType()
	{
		return "decimal";
	}

	@Override
	public String getFormatString()
	{
		return "%.20g";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/Double;";
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return "double";
	}

	@Override
	public Class<?> getJavaType()
	{
		return BigDecimal.class;
	}

	@Override
	public Double getTypedValue(String value) throws ParseException
	{
		return Double.parseDouble(value);
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.DECIMAL;
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
		else if (value instanceof Double) return value;
		else if (value instanceof String || value instanceof Integer) return Double.parseDouble(value.toString());
		throw new RuntimeException("DecimalField.convert(" + value + ") failed");
	}
}
