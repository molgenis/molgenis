package org.molgenis.file.ingest.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.file.FileMetaMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionMetaData extends SystemEntityMetaDataImpl
{
	public static final String SIMPLE_NAME = "FileIngestJobExecution";
	public static final String FILE_INGEST_JOB_EXECUTION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String FILE = "file";
	public static final String FILE_INGEST = "fileIngest";

	public static final String FILE_INGEST_JOB_TYPE = "FileIngesterJob";

	private FileMetaMetaData fileMetaMetaData;
	private FileIngestMetaData fileIngestMetaData;
	private JobExecutionMetaData jobExecutionMetaData;

	FileIngestJobExecutionMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setExtends(jobExecutionMetaData);
		addAttribute(FILE).setLabel("File").setDescription("The imported file.").setDataType(XREF)
				.setRefEntity(fileMetaMetaData).setNillable(true);
		addAttribute(FILE_INGEST).setDataType(XREF).setRefEntity(fileIngestMetaData).setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setFileMetaMetaData(FileMetaMetaData fileMetaMetaData)
	{
		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
	}

	@Autowired
	public void setFileIngestMetaData(FileIngestMetaData fileIngestMetaData)
	{
		this.fileIngestMetaData = requireNonNull(fileIngestMetaData);
	}

	@Autowired
	public void setJobExecutionMetaData(JobExecutionMetaData jobExecutionMetaData)
	{
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}
}
