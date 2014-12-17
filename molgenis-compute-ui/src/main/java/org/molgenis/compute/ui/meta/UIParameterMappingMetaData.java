package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIParameterMappingMetaData extends DefaultEntityMetaData
{
	public static final UIParameterMappingMetaData INSTANCE = new UIParameterMappingMetaData();
	private static final String ENTITY_NAME = "ParameterMapping";
	public static final String IDENTIFIER = "identifier";
	public static final String FROM = "from";
	public static final String TO = "to";

	private UIParameterMappingMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(FROM).setDataType(STRING).setNillable(false);
		addAttribute(TO).setDataType(STRING).setNillable(false);
	}

}
