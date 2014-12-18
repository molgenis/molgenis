package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.ENUM;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.compute.ui.model.JobStatus;
import org.molgenis.data.support.DefaultEntityMetaData;

import static org.molgenis.MolgenisFieldTypes.*;

public class AnalysisJobMetaData extends DefaultEntityMetaData
{
	// TODO workaround for #1810 'EMX misses DefaultValue'
	public static final JobStatus STATUS_DEFAULT = JobStatus.CREATED;
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
	public static final String ANALYSIS = "analysis";

	private AnalysisJobMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(NAME).setNillable(false).setLabelAttribute(true);
		addAttribute(SCHEDULER_ID).setDataType(STRING);
		addAttribute(WORKFLOW_NODE).setDataType(XREF).setNillable(false).setRefEntity(UIWorkflowNodeMetaData.INSTANCE);
		addAttribute(GENERATED_SCRIPT).setDataType(SCRIPT).setNillable(false);
		addAttribute(STATUS).setDataType(ENUM).setNillable(false).setEnumOptions(JobStatus.names())
				.setDefaultValue(STATUS_DEFAULT.toString());
		addAttribute(START_TIME).setDataType(DATETIME);
		addAttribute(END_TIME).setDataType(DATETIME);
		addAttribute(ERROR_MESSAGE).setDataType(TEXT);
		addAttribute(OUTPUT_MESSAGE).setDataType(TEXT);
		addAttribute(PARAMETER_VALUES).setDataType(MREF).setRefEntity(UIParameterValueMetaData.INSTANCE);
		addAttribute(ANALYSIS).setDataType(XREF).setNillable(false).setRefEntity(AnalysisMetaData.INSTANCE);
	}

}
