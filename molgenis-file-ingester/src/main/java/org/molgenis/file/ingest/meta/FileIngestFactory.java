package org.molgenis.file.ingest.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestFactory extends AbstractSystemEntityFactory<FileIngest, FileIngestMetaData, String>
{
	@Autowired
	FileIngestFactory(FileIngestMetaData fileIngestMeta, EntityPopulator entityPopulator)
	{
		super(FileIngest.class, fileIngestMeta, entityPopulator);
	}
}
