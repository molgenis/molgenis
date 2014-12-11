package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIParameterMappingMetaData extends DefaultEntityMetaData
{
	public static final UIParameterMappingMetaData INSTANCE = new UIParameterMappingMetaData();
	public static final String ENTITY_NAME = "ParameterMapping";
	public static final String IDENTIFIER = "identifier";
	public static final String FROM = "from";
	public static final String TO = "to";

	private UIParameterMappingMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(FROM).setDataType(XREF).setRefEntity(UIParameterMetaData.INSTANCE).setNillable(false);
		addAttribute(TO).setDataType(XREF).setRefEntity(UIParameterMetaData.INSTANCE).setNillable(false);
	}

}
