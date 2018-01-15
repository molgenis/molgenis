package org.molgenis.data.file.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class FileMetaFactory extends AbstractSystemEntityFactory<FileMeta, FileMetaMetaData, String>
{
	FileMetaFactory(FileMetaMetaData fileMetaMetaData, EntityPopulator entityPopulator)
	{
		super(FileMeta.class, fileMetaMetaData, entityPopulator);
	}
}
