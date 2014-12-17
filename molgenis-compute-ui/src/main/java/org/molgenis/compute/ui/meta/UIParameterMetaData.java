package org.molgenis.compute.ui.meta;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIParameterMetaData extends DefaultEntityMetaData
{
	public static final UIParameterMetaData INSTANCE = new UIParameterMetaData();
	private static final String ENTITY_NAME = "Parameter";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String DATA_TYPE = "dataType";

	private UIParameterMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(NAME).setNillable(false).setLabelAttribute(true).setLookupAttribute(true);
		addAttribute(DATA_TYPE).setNillable(false);
		addAttribute(TYPE).setNillable(false);
	}

}
