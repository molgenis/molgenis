package org.molgenis.file.ingest.meta;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.file.model.FileMetaMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class FileIngestJobExecutionMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "FileIngestJobExecution";
	public static final String FILE_INGEST_JOB_EXECUTION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String URL = "url";
	public static final String LOADER = "loader";
	public static final List<String> LOADERS = Collections.singletonList("CSV");

	public static final String FILE = "file";
	public static final String ENTITY_META_DATA = "entityType";
	public static final String FILE_INGEST_JOB_TYPE = "FileIngesterJob";

	private final FileMetaMetaData fileMetaMetaData;
	private final JobExecutionMetaData jobExecutionMetaData;
	private final EntityTypeMetadata entityTypeMetadata;

	@Autowired
	FileIngestJobExecutionMetaData(FileMetaMetaData fileMetaMetaData, JobExecutionMetaData jobExecutionMetaData,
			EntityTypeMetadata entityTypeMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
	}

	@Override
	public void init()
	{
		setLabel("File ingest job execution");
		setExtends(jobExecutionMetaData);
		addAttribute(FILE).setLabel("File").setDescription("The imported file.").setDataType(XREF)
				.setRefEntity(fileMetaMetaData).setNillable(true);
		addAttribute(URL).setLabel("Url").setDescription("Url of the file to download.").setNillable(false);
		addAttribute(LOADER).setDataType(ENUM).setEnumOptions(LOADERS).setLabel("Loader type").setNillable(false);
		addAttribute(ENTITY_META_DATA).setDataType(XREF).setRefEntity(entityTypeMetadata).setLabel("Target EntityType")
				.setNillable(false);
	}
}
