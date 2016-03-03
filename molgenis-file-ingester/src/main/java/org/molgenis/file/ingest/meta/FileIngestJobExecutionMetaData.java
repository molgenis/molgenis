package org.molgenis.file.ingest.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.file.FileMetaMetaData;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "FileIngestJobExecution";
	public static final String FILE = "file";
	public static final String FILE_INGEST = "fileIngest";

	public FileIngestJobExecutionMetaData()
	{
		super(ENTITY_NAME);
		setExtends(new JobExecutionMetaData());
		addAttribute(FILE).setLabel("File").setDescription("The imported file.").setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(new FileMetaMetaData()).setNillable(true);
		addAttribute(FILE_INGEST).setDataType(MolgenisFieldTypes.XREF).setRefEntity(new FileIngestMetaData())
				.setNillable(false);
	}
}
