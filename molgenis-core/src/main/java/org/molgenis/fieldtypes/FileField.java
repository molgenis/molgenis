package org.molgenis.fieldtypes;

import java.io.File;
import java.text.ParseException;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;

public class FileField extends FieldType
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
		return "VARCHAR(1024)";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "VARCHAR(1024)";
	}

	@Override
	public String getHsqlType()
	{
		return "VARCHAR(1024)";
	}

	@Override
	public String getXsdType()
	{
		return "url";
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
		return File.class;
	}

	@Override
	public File getTypedValue(String value) throws ParseException
	{
		File file = new File(value);
		if (file.exists())
		{
			return file;
		}
		else
		{
			throw new ParseException("File " + value + " not found.", 0);
		}
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.FILE;
	}
}
