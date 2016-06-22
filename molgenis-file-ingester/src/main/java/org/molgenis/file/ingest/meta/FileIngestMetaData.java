package org.molgenis.file.ingest.meta;

import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.StringField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
public class FileIngestMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "FileIngest";
	public static final String FILE_INGEST = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String URL = "url";
	public static final String LOADER = "loader";
	public static final String ENTITY_META_DATA = "entityMetaData";
	public static final String CRONEXPRESSION = "cronexpression";
	public static final String ACTIVE = "active";
	public static final String FAILURE_EMAIL = "failureEmail";

	public static final ImmutableList<String> LOADERS = ImmutableList.of("CSV");
	private final EntityMetaDataMetaData entityMetaDataMetaData;

	@Autowired
	public FileIngestMetaData(EntityMetaDataMetaData entityMetaDataMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.entityMetaDataMetaData = entityMetaDataMetaData;
	}

	@Override
	public void init()
	{
		addAttribute(ID, ROLE_ID).setAuto(true).setNillable(false);
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setNillable(false);
		addAttribute(DESCRIPTION).setDataType(MolgenisFieldTypes.TEXT).setLabel("Description").setNillable(true);
		addAttribute(URL).setLabel("Url").setDescription("Url of the file to download.").setNillable(false);
		addAttribute(LOADER).setDataType(new EnumField()).setEnumOptions(LOADERS).setLabel("Loader type")
				.setNillable(false);
		addAttribute(ENTITY_META_DATA).setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(entityMetaDataMetaData).setLabel("Target EntityMetaData").setNillable(false);
		addAttribute(CRONEXPRESSION).setLabel("Cronexpression").setNillable(false)
				.setValidationExpression("$('" + CRONEXPRESSION + "').matches(" + StringField.CRON_REGEX + ").value()");
		addAttribute(ACTIVE).setDataType(MolgenisFieldTypes.BOOL).setLabel("Active").setNillable(false);
		addAttribute(FAILURE_EMAIL).setDataType(MolgenisFieldTypes.EMAIL).setLabel("Failure email")
				.setDescription("Leave blank if you don't want to receive emails if the jobs failed.")
				.setNillable(true);
	}
}
