package org.molgenis.fieldtypes;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.model.MolgenisModelException;

public class CategoricalType extends FieldType
{
	private static final long serialVersionUID = 1L;

	private Map<String, String> categoryMapping = new HashMap<String, String>();

	@Override
	public String getJavaPropertyType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getJavaPropertyDefault() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getJavaAssignment(String value) throws MolgenisModelException
	{
		return null;
	}

	@Override
	public Class<?> getJavaType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getXsdType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getHsqlType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getFormatString()
	{
		return null;
	}

	@Override
	public HtmlInput<?> createInput(String name, String xrefEntityClassNames) throws HtmlInputException
	{
		return null;
	}

	@Override
	public String getCppJavaPropertyType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		return null;
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.CATEGORICAL;
	}

	/**
	 * @return For a categorical variable type, return the map of value->label
	 *         pairs.
	 */
	public Map<String, String> getCategoryMapping()
	{
		return categoryMapping;
	}
}
