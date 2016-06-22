package org.molgenis.file.ingest.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.file.FileMetaMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "FileIngestJobExecution";
	public static final String FILE_INGEST_JOB_EXECUTION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String FILE = "file";
	public static final String FILE_INGEST = "fileIngest";

	public static final String FILE_INGEST_JOB_TYPE = "FileIngesterJob";

	private final FileMetaMetaData fileMetaMetaData;
	private final FileIngestMetaData fileIngestMetaData;
	private final JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	FileIngestJobExecutionMetaData(FileMetaMetaData fileMetaMetaData, FileIngestMetaData fileIngestMetaData,
			JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
		this.fileIngestMetaData = requireNonNull(fileIngestMetaData);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setExtends(jobExecutionMetaData);
		addAttribute(FILE).setLabel("File").setDescription("The imported file.").setDataType(XREF)
				.setRefEntity(fileMetaMetaData).setNillable(true);
		addAttribute(FILE_INGEST).setDataType(XREF).setRefEntity(fileIngestMetaData).setNillable(false);
	}
}
