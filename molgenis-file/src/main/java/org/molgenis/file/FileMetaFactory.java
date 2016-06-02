package org.molgenis.file;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileMetaFactory extends AbstractEntityFactory<FileMeta, FileMetaMetaData, String>
{
	@Autowired
	FileMetaFactory(FileMetaMetaData fileMetaMetaData)
	{
		super(FileMeta.class, fileMetaMetaData, String.class);
	}
}
