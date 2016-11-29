package org.molgenis.file.ingest.meta;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.util.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class FileIngestMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "FileIngest";
	public static final String FILE_INGEST = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String URL = "url";
	public static final String LOADER = "loader";
	public static final String ENTITY_META_DATA = "entityType";
	public static final String CRONEXPRESSION = "cronexpression";
	public static final String ACTIVE = "active";
	public static final String FAILURE_EMAIL = "failureEmail";

	public static final ImmutableList<String> LOADERS = ImmutableList.of("CSV");
	private final EntityTypeMetadata entityTypeMetadata;

	@Autowired
	public FileIngestMetaData(EntityTypeMetadata entityTypeMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.entityTypeMetadata = entityTypeMetadata;
	}

	@Override
	public void init()
	{
		setLabel("File ingest");
		addAttribute(ID, ROLE_ID).setAuto(true).setNillable(false);
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setNillable(false);
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true);
		addAttribute(URL).setLabel("Url").setDescription("Url of the file to download.").setNillable(false);
		addAttribute(LOADER).setDataType(ENUM).setEnumOptions(LOADERS).setLabel("Loader type").setNillable(false);
		addAttribute(ENTITY_META_DATA).setDataType(XREF).setRefEntity(entityTypeMetadata)
				.setLabel("Target EntityType").setNillable(false);
		addAttribute(CRONEXPRESSION).setLabel("Cronexpression").setNillable(false)
				.setValidationExpression("$('" + CRONEXPRESSION + "').matches(" + RegexUtils.CRON_REGEX + ").value()");
		addAttribute(ACTIVE).setDataType(BOOL).setLabel("Active").setNillable(false);
		addAttribute(FAILURE_EMAIL).setDataType(EMAIL).setLabel("Failure email")
				.setDescription("Leave blank if you don't want to receive emails if the jobs failed.")
				.setNillable(true);
	}
}
