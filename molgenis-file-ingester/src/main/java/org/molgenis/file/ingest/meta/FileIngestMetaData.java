package org.molgenis.file.ingest.meta;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
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
	public static final String BUCKET = "bucket";
	public static final String KEY = "key";
	public static final String PROFILE = "profile";

	public static final ImmutableList<String> LOADERS = ImmutableList.of("CSV");
	public static final String TYPE = "Type";
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
		setLabel("Automatic file import");
		addAttribute(ID, ROLE_ID).setAuto(true).setNillable(false);

		Attribute generalAttr = addAttribute("general").setLabel("General").setDataType(COMPOUND);
		Attribute csvAttr = addAttribute("ingest").setLabel("Automatic CSV Ingest").setDataType(COMPOUND)
				.setVisibleExpression("$('" + TYPE + "').eq('DOWNLOAD').value();");
		Attribute bucketAttr = addAttribute("Amazon").setLabel("Amazon Bucket").setDataType(COMPOUND)
				.setVisibleExpression("$('" + TYPE + "').eq('BUCKET').value();");

		addAttribute(TYPE).setLabel("Type").setDescription("Download or Bucket").setDataType(ENUM)
				.setEnumOptions(FileIngestType.class).setNillable(false).setParent(generalAttr);
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setNillable(false).setParent(generalAttr);
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true).setParent(generalAttr);
		addAttribute(CRONEXPRESSION).setLabel("Cronexpression").setNillable(true)
				.setValidationExpression("$('" + CRONEXPRESSION + "').matches(" + RegexUtils.CRON_REGEX + ").value()")
				.setParent(generalAttr);
		addAttribute(ACTIVE).setDataType(BOOL).setLabel("Active").setNillable(false).setParent(generalAttr);
		addAttribute(FAILURE_EMAIL).setDataType(EMAIL).setLabel("Failure email")
				.setDescription("Leave blank if you don't want to receive emails if the jobs failed.").setNillable(true)
				.setParent(generalAttr);

		addAttribute(URL).setLabel("Url").setDescription("Url of the file to download.").setNillable(true)
				.setParent(csvAttr);
		addAttribute(LOADER).setDataType(ENUM).setEnumOptions(LOADERS).setLabel("Loader type").setNillable(true)
				.setParent(csvAttr);
		addAttribute(ENTITY_META_DATA).setDataType(XREF).setRefEntity(entityTypeMetadata).setLabel("Target EntityType")
				.setNillable(true).setParent(csvAttr);

		addAttribute(BUCKET).setLabel("Bucket").setDescription("Url of the file to download.").setNillable(false)
				.setParent(bucketAttr);
		addAttribute(KEY).setLabel("Key").setDescription("Url of the file to download.").setNillable(false)
				.setParent(bucketAttr);
		addAttribute(PROFILE).setLabel("Profile").setDescription("Url of the file to download.").setNillable(false)
				.setParent(bucketAttr);
	}
}
