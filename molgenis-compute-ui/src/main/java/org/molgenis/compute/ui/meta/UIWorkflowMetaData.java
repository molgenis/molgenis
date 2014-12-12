package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIWorkflowMetaData extends DefaultEntityMetaData
{
	public static final UIWorkflowMetaData INSTANCE = new UIWorkflowMetaData();

	public static final String ENTITY_NAME = "Workflow";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String NODES = "nodes";
	public static final String TARGET_TYPE = "targetType";
	public static final String GENERATE_SCRIPT = "generateScript";
	public static final String WORKFLOW_FILE = "workflowFile";
	public static final String PARAMETERS_FILE = "parametersFile";
	public static final String PARAMETERS = "parameters";

	private UIWorkflowMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(NAME).setUnique(true).setLabelAttribute(true);
		addAttribute(DESCRIPTION);
		addAttribute(NODES).setNillable(false).setDataType(MREF).setRefEntity(UIWorkflowNodeMetaData.INSTANCE);
		addAttribute(TARGET_TYPE);
		addAttribute(GENERATE_SCRIPT).setDataType(SCRIPT);
		addAttribute(WORKFLOW_FILE).setDataType(STRING).setVisible(false);
		addAttribute(PARAMETERS_FILE).setDataType(STRING).setVisible(false);
		addAttribute(PARAMETERS).setNillable(false).setDataType(MREF)
				.setRefEntity(UIWorkflowParameterMetaData.INSTANCE);
	}
}
