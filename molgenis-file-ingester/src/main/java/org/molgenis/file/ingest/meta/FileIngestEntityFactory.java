package org.molgenis.file.ingest.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestEntityFactory extends AbstractSystemEntityFactory<FileIngest, FileIngestMetaData, String>
{
	@Autowired
	FileIngestEntityFactory(FileIngestMetaData fileIngestMeta)
	{
		super(FileIngest.class, fileIngestMeta);
	}
}
