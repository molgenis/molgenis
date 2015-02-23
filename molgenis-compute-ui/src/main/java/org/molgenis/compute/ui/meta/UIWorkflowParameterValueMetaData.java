package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIWorkflowParameterValueMetaData extends DefaultEntityMetaData
{
	public static final UIWorkflowParameterValueMetaData INSTANCE = new UIWorkflowParameterValueMetaData();

	private static final String ENTITY_NAME = "ParameteValue";
	public static final String IDENTIFIER = "identifier";
	public static final String VALUE = "value";

	private UIWorkflowParameterValueMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(VALUE);
	}

}
