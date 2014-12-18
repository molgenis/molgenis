package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIWorkflowNodeMetaData extends DefaultEntityMetaData
{
	public static final UIWorkflowNodeMetaData INSTANCE = new UIWorkflowNodeMetaData();

	private static final String ENTITY_NAME = "WorkflowNode";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String PROTOCOL = "protocol";
	public static final String PREVIOUS_NODES = "previousNodes";
	public static final String PARAMETER_MAPPINGS = "parameterMappings";

	private UIWorkflowNodeMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(NAME).setNillable(false).setLabelAttribute(true);
		addAttribute(PROTOCOL).setDataType(XREF).setRefEntity(UIWorkflowProtocolMetaData.INSTANCE).setNillable(false);
		addAttribute(PREVIOUS_NODES).setDataType(MREF).setRefEntity(this);
		addAttribute(PARAMETER_MAPPINGS).setDataType(MREF).setRefEntity(UIParameterMappingMetaData.INSTANCE);
	}
}
