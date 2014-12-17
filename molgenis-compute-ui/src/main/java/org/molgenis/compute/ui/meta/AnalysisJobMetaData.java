package org.molgenis.compute.ui.meta;

import org.molgenis.data.support.DefaultEntityMetaData;

import static org.molgenis.MolgenisFieldTypes.*;

public class AnalysisJobMetaData extends DefaultEntityMetaData
{
	public static final AnalysisJobMetaData INSTANCE = new AnalysisJobMetaData();
	private static final String ENTITY_NAME = "AnalysisJob";
	public static final String IDENTIFIER = "identifier";
	public static final String SCHEDULER_ID = "schedulerId";
	public static final String WORKFLOW_NODE = "workflowNode";
	public static final String GENERATED_SCRIPT = "generatedScript";
	public static final String STATUS = "status";
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String OUTPUT_MESSAGE = "outputMessage";
	public static final String PARAMETER_VALUES = "parameterValues";
	public static final String NAME = "name";


	private AnalysisJobMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(NAME).setNillable(false).setLabelAttribute(true);
		addAttribute(SCHEDULER_ID).setDataType(STRING);
		addAttribute(WORKFLOW_NODE).setDataType(XREF).setRefEntity(UIWorkflowNodeMetaData.INSTANCE);
		addAttribute(GENERATED_SCRIPT).setDataType(SCRIPT).setNillable(false);
		addAttribute(STATUS);
		addAttribute(START_TIME).setDataType(DATETIME);
		addAttribute(END_TIME).setDataType(DATETIME);
		addAttribute(ERROR_MESSAGE).setDataType(TEXT);
		addAttribute(OUTPUT_MESSAGE).setDataType(TEXT);
		addAttribute(PARAMETER_VALUES).setDataType(MREF).setRefEntity(UIParameterValueMetaData.INSTANCE);
	}

}
