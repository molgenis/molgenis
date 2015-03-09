package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIWorkflowMetaData extends DefaultEntityMetaData
{
	public static final UIWorkflowMetaData INSTANCE = new UIWorkflowMetaData();

	private static final String ENTITY_NAME = "Workflow";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String NODES = "nodes";
	public static final String TARGET_TYPE = "targetType";
	public static final String GENERATE_SCRIPT = "generateScript";
	public static final String PARAMETERS = "parameters";
	public static final String ACTIVE = "active";

	public UIWorkflowMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(NAME).setUnique(true).setNillable(false).setLabelAttribute(true);
		addAttribute(DESCRIPTION);
		addAttribute(NODES).setNillable(false).setDataType(MREF).setRefEntity(UIWorkflowNodeMetaData.INSTANCE);
		addAttribute(TARGET_TYPE);
		addAttribute(GENERATE_SCRIPT).setDataType(SCRIPT).setVisible(true);
		addAttribute(PARAMETERS).setNillable(false).setDataType(MREF)
				.setRefEntity(UIWorkflowParameterMetaData.INSTANCE);
		addAttribute(ACTIVE).setDataType(BOOL).setNillable(false);
	}
}
