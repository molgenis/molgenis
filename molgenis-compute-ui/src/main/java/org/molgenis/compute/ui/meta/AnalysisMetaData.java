package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class AnalysisMetaData extends DefaultEntityMetaData
{
	public static final AnalysisMetaData INSTANCE = new AnalysisMetaData();

	private static final String ENTITY_NAME = "Analysis";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String CREATION_DATE = "creationDate";
	public static final String WORKFLOW = "workflow";
	public static final String JOBS = "jobs";
	public static final String BACKEND = "backend";
	public static final String SUBMIT_SCRIPT = "submitScript";

	private AnalysisMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(NAME).setNillable(false).setUnique(true).setLabelAttribute(true);
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(CREATION_DATE).setDataType(DATETIME);
		addAttribute(WORKFLOW).setDataType(XREF).setNillable(false).setRefEntity(UIWorkflowMetaData.INSTANCE);
		addAttribute(JOBS).setDataType(MREF).setRefEntity(AnalysisJobMetaData.INSTANCE);
		addAttribute(BACKEND).setDataType(XREF).setNillable(false).setRefEntity(UIBackendMetaData.INSTANCE);
		addAttribute(SUBMIT_SCRIPT).setDataType(SCRIPT);
	}

}
