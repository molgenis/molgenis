package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIParameterValueMetaData extends DefaultEntityMetaData
{
	public static final UIParameterValueMetaData INSTANCE = new UIParameterValueMetaData();

	private static final String ENTITY_NAME = "ParameteValue";
	public static final String IDENTIFIER = "identifier";
	public static final String PARAMETER = "parameter";
	public static final String VALUE = "value";

	private UIParameterValueMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(PARAMETER).setNillable(false).setDataType(XREF).setRefEntity(UIParameterMetaData.INSTANCE);
		addAttribute(VALUE);
	}

}
