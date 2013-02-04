package org.molgenis.fieldtypes;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.framework.ui.html.LongInput;
import org.molgenis.model.MolgenisModelException;

public class LongField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaPropertyType()
	{
		return "Long";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "" + Long.parseLong(value) + "L";
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "BIGINT";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return "NUMBER (19,0)";
	}

	@Override
	public String getHsqlType()
	{
		return "LONG";
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
		return new LongInput(name);
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return "long";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/Long;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return Long.class;
	}

	@Override
	public Long getTypedValue(String value) throws ParseException
	{
		return Long.parseLong(value);
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.LONG;
	}

	@Override
	public List<String> getAllowedOperators()
	{
		return Arrays.asList("EQUALS", "NOT EQUALS", "LESS", "GREATER");
	}

}
