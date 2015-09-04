package org.molgenis.file;

import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class FileMetaMetaData extends DefaultEntityMetaData
{
	public FileMetaMetaData()
	{
		super(FileMeta.ENTITY_NAME, FileMeta.class);
		setExtends(new OwnedEntityMetaData());
		addAttribute(FileMeta.ID).setDataType(STRING).setIdAttribute(true).setNillable(false).setVisible(false)
				.setLabel("Id");
		addAttribute(FileMeta.FILENAME).setDataType(STRING).setLabelAttribute(true).setLookupAttribute(true)
				.setNillable(false).setLabel("Filename");
		addAttribute(FileMeta.CONTENT_TYPE).setDataType(STRING).setLookupAttribute(true).setLabel("Content-type");
		addAttribute(FileMeta.SIZE).setDataType(STRING).setLabel("Size").setDescription("File size in bytes");
		addAttribute(FileMeta.URL).setDataType(HYPERLINK).setLabel("URL").setDescription("File download URL")
				.setUnique(true);
	}
}
