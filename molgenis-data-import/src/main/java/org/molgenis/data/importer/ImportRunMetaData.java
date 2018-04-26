package org.molgenis.data.importer;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class ImportRunMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "ImportRun";
	public static final String IMPORT_RUN = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String STARTDATE = "startDate";
	public static final String ENDDATE = "endDate";
	public static final String USERNAME = "username";
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

		setLabel("Import");
		setDescription("Data import reports");
		addAttribute(ID, ROLE_ID).setAuto(true)
								 .setVisible(false)
								 .setDescription("automatically generated internal id, only for internal use.");
		addAttribute(STARTDATE).setDataType(DATE_TIME).setNillable(false);
		addAttribute(ENDDATE).setDataType(DATE_TIME).setNillable(true);
		addAttribute(USERNAME).setNillable(false);
		addAttribute(STATUS).setDataType(ENUM)
							.setNillable(false)
							.setEnumOptions(Arrays.asList("RUNNING", "FINISHED", "FAILED"));
		addAttribute(MESSAGE).setDataType(TEXT).setNillable(true);
		addAttribute(PROGRESS).setDataType(INT).setNillable(false);
		addAttribute(IMPORTEDENTITIES).setDataType(TEXT).setNillable(true);
		addAttribute(NOTIFY).setDataType(BOOL)
							.setNillable(true)
							.setDescription("Boolean to indicate whether or not to send an email on job completion");

		setRowLevelSecured(true);
	}
}
