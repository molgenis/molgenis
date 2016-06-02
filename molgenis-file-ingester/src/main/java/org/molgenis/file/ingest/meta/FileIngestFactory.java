package org.molgenis.file.ingest.meta;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestFactory extends AbstractEntityFactory<FileIngest, FileIngestMetaData, String>
{
	@Autowired
	FileIngestFactory(FileIngestMetaData tagMetaData)
	{
		super(FileIngest.class, tagMetaData, String.class);
	}
}
