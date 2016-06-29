package org.molgenis.data.meta.system;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class ImportRunMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "ImportRun";
	public static final String IMPORT_RUN = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String STARTDATE = "startDate";
	public static final String ENDDATE = "endDate";
	public static final String USERNAME = "userName";
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";
	public static final String PROGRESS = "progress";
	public static final String IMPORTEDENTITIES = "importedEntities";
	public static final String NOTIFY = "notify";

	ImportRunMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.");
		addAttribute(STARTDATE).setDataType(DATE_TIME).setNillable(false).setDescription("");
		addAttribute(ENDDATE).setDataType(DATE_TIME).setNillable(true).setDescription("");
		addAttribute(USERNAME).setNillable(false).setDescription("");
		addAttribute(STATUS).setDataType(ENUM).setNillable(false)
				.setEnumOptions(Arrays.asList("RUNNING", "FINISHED", "FAILED")).setDescription("");
		addAttribute(MESSAGE).setDataType(TEXT).setNillable(true).setDescription("");
		addAttribute(PROGRESS).setDataType(INT).setNillable(false).setDescription("");
		addAttribute(IMPORTEDENTITIES).setDataType(TEXT).setNillable(true).setDescription("");
		addAttribute(NOTIFY).setDataType(BOOL).setNillable(true)
				.setDescription("Boolean to indicate whether or not to send an email on job completion");
	}
}
