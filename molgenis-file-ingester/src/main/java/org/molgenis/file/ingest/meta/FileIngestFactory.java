package org.molgenis.file.ingest.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestFactory extends AbstractSystemEntityFactory<FileIngest, FileIngestMetaData, String>
{
	@Autowired
	FileIngestFactory(FileIngestMetaData fileIngestMeta)
	{
		super(FileIngest.class, fileIngestMeta);
	}
}
