package org.molgenis.compute.ui.meta;

import org.molgenis.compute.ui.model.AnalysisStatus;
import org.molgenis.data.support.DefaultEntityMetaData;

import static org.molgenis.MolgenisFieldTypes.*;

public class AnalysisMetaData extends DefaultEntityMetaData
{
	// TODO workaround for #1810 'EMX misses DefaultValue'
	public static final AnalysisStatus STATUS_DEFAULT = AnalysisStatus.CREATED;
	public static final AnalysisMetaData INSTANCE = new AnalysisMetaData();

	private static final String ENTITY_NAME = "Analysis";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String CREATION_DATE = "creationDate";
	public static final String WORKFLOW = "workflow";
	public static final String BACKEND = "backend";
	public static final String SUBMIT_SCRIPT = "submitScript";
	public static final String STATUS = "status";
	public static final String USER = "user";
	public static final String WAS_RUN = "was_run";

	private AnalysisMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(NAME).setNillable(false).setUnique(true).setLabelAttribute(true).setLabel("Name");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		addAttribute(CREATION_DATE).setDataType(DATETIME).setLabel("Creation date");
		addAttribute(WORKFLOW).setDataType(XREF).setNillable(false).setRefEntity(UIWorkflowMetaData.INSTANCE)
				.setLabel("Workflow");
		addAttribute(BACKEND).setDataType(XREF).setRefEntity(UIBackendMetaData.INSTANCE).setLabel("Backend");
		addAttribute(SUBMIT_SCRIPT).setDataType(SCRIPT).setLabel("Submit script");
		addAttribute(STATUS).setDataType(ENUM).setNillable(false).setEnumOptions(AnalysisStatus.names())
				.setDefaultValue(STATUS_DEFAULT.toString()).setLabel("Status");
		// FIXME user xref to MolgenisUser when https://github.com/molgenis/molgenis/issues/2054 is fixed
		addAttribute(USER).setNillable(false).setLabel("User");
		addAttribute(WAS_RUN).setDataType(BOOL).
				setDefaultValue(false).setNillable(false).setLabel("was Run");
	}

}
