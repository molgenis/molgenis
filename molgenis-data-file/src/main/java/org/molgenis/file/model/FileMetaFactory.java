package org.molgenis.file.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileMetaFactory extends AbstractSystemEntityFactory<FileMeta, FileMetaMetaData, String>
{
	@Autowired
	FileMetaFactory(FileMetaMetaData fileMetaMetaData, EntityPopulator entityPopulator)
	{
		super(FileMeta.class, fileMetaMetaData, entityPopulator);
	}
}
