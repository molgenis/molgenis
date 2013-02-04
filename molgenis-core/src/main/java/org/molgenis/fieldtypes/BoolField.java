package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.ui.html.BoolInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.HtmlInputException;
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
	public HtmlInput<?> createInput(String name, String xrefEntityClassName) throws HtmlInputException
	{
		return new BoolInput(name);
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
}
