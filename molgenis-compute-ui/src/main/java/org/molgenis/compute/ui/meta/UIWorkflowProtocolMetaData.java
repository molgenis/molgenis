package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIWorkflowProtocolMetaData extends DefaultEntityMetaData
{
	public static final UIWorkflowProtocolMetaData INSTANCE = new UIWorkflowProtocolMetaData();
	private static final String ENTITY_NAME = "WorkflowProtocol";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String TEMPLATE = "template";
	public static final String PARAMETERS = "parameters";

	private UIWorkflowProtocolMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(NAME).setNillable(false).setUnique(true).setLabelAttribute(true);
		addAttribute(TEMPLATE).setDataType(SCRIPT).setNillable(false);
		addAttribute(PARAMETERS).setDataType(MREF).setRefEntity(UIParameterMetaData.INSTANCE);
	}
}
