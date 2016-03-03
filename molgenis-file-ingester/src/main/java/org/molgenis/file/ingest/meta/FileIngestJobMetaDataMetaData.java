package org.molgenis.file.ingest.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.jobs.JobMetaDataMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.file.FileMetaMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobMetaDataMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "FileIngestJobMetaData";
	public static final String FILE = "file";
	public static final String FILE_INGEST = "fileIngest";

	@Autowired
	public FileIngestJobMetaDataMetaData(FileIngestMetaData fileIngestMetaData)
	{
		super(ENTITY_NAME);
		setExtends(new JobMetaDataMetaData());
		addAttribute(FILE).setLabel("File").setDescription("The imported file.").setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(new FileMetaMetaData()).setNillable(true);
		addAttribute(FILE_INGEST).setDataType(MolgenisFieldTypes.XREF).setRefEntity(fileIngestMetaData)
				.setNillable(false);
	}
}
