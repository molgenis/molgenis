package org.molgenis.file.ingest.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.file.FileMetaMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "FileIngestJobExecution";
	public static final String FILE = "file";
	public static final String FILE_INGEST = "fileIngest";

	private static FileIngestJobExecutionMetaData INSTANCE;
	private final FileIngestMetaData fileIngestMetaData;

	@Autowired
	public FileIngestJobExecutionMetaData(FileIngestMetaData fileIngestMetaData)
	{
		super(ENTITY_NAME);
		this.fileIngestMetaData = fileIngestMetaData;
		INSTANCE = this;
	}

	@Override
	public void init()
	{
		setExtends(new JobExecutionMetaData());
		addAttribute(FILE).setLabel("File").setDescription("The imported file.").setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(new FileMetaMetaData()).setNillable(true);
		addAttribute(FILE_INGEST).setDataType(MolgenisFieldTypes.XREF).setRefEntity(fileIngestMetaData)
				.setNillable(false);
	}

	// access bean from classes other than spring-managed beans
	public static FileIngestJobExecutionMetaData get() {
		return INSTANCE;
	}
}
