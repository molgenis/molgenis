package org.molgenis.fieldtypes;

import java.text.ParseException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.model.MolgenisModelException;

@Deprecated
public class ListField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaPropertyDefault() throws MolgenisModelException
	{
		return "new java.util.ArrayList<?>()";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		return "NOT IMPLEMENTED";
	}

	@Override
	public String getJavaPropertyType()
	{
		return "java.util.List<?>";
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		// should never happen?
		return "LIST CANNOT BE IN SQL";
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		// should never happen?
		return "LIST CANNOT BE IN SQL";
	}

	@Override
	public String getJavaSetterType() throws MolgenisModelException
	{
		return "List";
	}

	@Override
	public String getHsqlType()
	{
		return "LIST CANNOT BE IN SQL";
	}

	@Override
	public String getXsdType()
	{
		return "";
	}

	@Override
	public String getFormatString()
	{
		return "";
	}

	/**
	 * Since this class is deprecated, this method is not implemented.
	 */
	@Override
	public HtmlInput<?> createInput(String name, String xrefEntityClassName) throws HtmlInputException
	{
		throw new HtmlInputException("Class deprecated, will not return input.");
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return "vector<Entity>";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/util/List;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return java.util.List.class;
	}

	@Override
	public java.util.List<?> getTypedValue(String value) throws ParseException
	{
		return Arrays.asList(StringUtils.split(value, ","));
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.LIST;
	}
}
