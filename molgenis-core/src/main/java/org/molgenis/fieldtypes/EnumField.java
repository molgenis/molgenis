package org.molgenis.fieldtypes;

import java.text.ParseException;
import java.util.List;
import java.util.Vector;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;

public class EnumField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaPropertyType()
	{
		return "String";
	}

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
		return "ENUM(" + this.toCsv(f.getEnumOptions()) + ")";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "VARCHAR2(255)";
	}

	@Override
	public String getHsqlType()
	{
		return "VARCHAR(1024)";
	}

	@Override
	public String getXsdType()
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
		return Enum.class;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		throw new UnsupportedOperationException("Unable to cast enum type");
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.ENUM;
	}

	@Override
	public Object convert(Object value)
	{
		if (value == null) return null;
		return value.toString();
	}

	public void setEnumOptions(List<String> enumOptions)
	{
		Vector<String> v = enumOptions == null ? null : new Vector<String>(enumOptions);
		if (f == null) f = new Field(null, null, this);
		f.setEnumOptions(v);
	}

	public List<String> getEnumOptions()
	{
		if (f == null)
		{
			return null;
		}

		try
		{
			return f.getEnumOptions();
		}
		catch (MolgenisModelException e)
		{
			throw new RuntimeException(e);
		}
	}
}
