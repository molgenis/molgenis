package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;

import java.text.ParseException;

/**
 * Compound field type is a field type for attributes consisting of multiple attributes
 */
public class CompoundField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaAssignment(String value)
	{
		return "NOT IMPLEMENTED";
	}

	@Override
	public String getJavaPropertyType() throws MolgenisModelException
	{
		Field f_ref = f.getXrefField();
		return getFieldType(f_ref).getJavaPropertyType();
	}

	@Override
	public String getJavaPropertyDefault()
	{
		if (f.getDefaultValue() == null || f.getDefaultValue().isEmpty()) return "null";
		return f.getDefaultValue();
	}

	@Override
	public String getJavaSetterType() throws MolgenisModelException
	{

		return getFieldType(f.getXrefField()).getJavaSetterType();
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return getFieldType(f.getXrefField()).getMysqlType();
	}

	@Override
	public String getPostgreSqlType()
	{
		throw new UnsupportedOperationException("No PostgreSQL data type exists for 'compound' field type");
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return getFieldType(f.getXrefField()).getOracleType();
	}

	@Override
	public String getHsqlType() throws MolgenisModelException
	{
		return getFieldType(f.getXrefField()).getHsqlType();
	}

	@Override
	public String getXsdType() throws MolgenisModelException
	{
		return getFieldType(f.getXrefField()).getXsdType();
	}

	@Override
	public String getFormatString()
	{
		return "";
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		Field f_ref = f.getXrefField();
		return getFieldType(f_ref).getCppPropertyType();
	}

	@Override
	public String getCppJavaPropertyType() throws MolgenisModelException
	{
		Field f_ref = f.getXrefField();
		return getFieldType(f_ref).getCppJavaPropertyType();
	}

	@Override
	public Class<?> getJavaType()
	{
		return null;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		throw new UnsupportedOperationException("Xref conversion not supported.");
	}

	@Override
	public AttributeType getEnumType()
	{
		return AttributeType.COMPOUND;
	}

	@Override
	public Long getMaxLength()
	{
		return null;
	}

	@Override
	public Object convert(Object defaultValue)
	{
		return null;
	}

}