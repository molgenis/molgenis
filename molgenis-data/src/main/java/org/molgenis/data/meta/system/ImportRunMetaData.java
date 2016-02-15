package org.molgenis.data.meta.system;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.ENUM;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.Arrays;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.stereotype.Component;

@Component
public class ImportRunMetaData extends DefaultEntityMetaData
{
	public static final String ID = "id";
	public static final String STARTDATE = "startDate";
	public static final String ENDDATE = "endDate";
	public static final String USERNAME = "userName";
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";
	public static final String PROGRESS = "progress";
	public static final String IMPORTEDENTITIES = "importedEntities";

	public static final ImportRunMetaData INSTANCE = new ImportRunMetaData();

	public ImportRunMetaData()
	{
		super("ImportRun");
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.");
		addAttribute(STARTDATE).setDataType(DATETIME).setNillable(false).setDescription("");
		addAttribute(ENDDATE).setDataType(DATETIME).setNillable(true).setDescription("");
		addAttribute(USERNAME).setNillable(false).setDescription("");
		addAttribute(STATUS).setDataType(new EnumField()).setNillable(false)
				.setEnumOptions(Arrays.asList("RUNNING", "FINISHED", "FAILED")).setDescription("");
		addAttribute(MESSAGE).setDataType(TEXT).setNillable(true).setDescription("");
		addAttribute(PROGRESS).setDataType(INT).setNillable(false).setDescription("");
		addAttribute(IMPORTEDENTITIES).setDataType(TEXT).setNillable(true).setDescription("");
	}
}
