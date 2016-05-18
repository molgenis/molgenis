package org.molgenis.file;

import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class FileMetaMetaData extends SystemEntityMetaDataImpl
{
	@Override
	public void init()
	{
		setName(FileMeta.ENTITY_NAME);
		setExtends(new OwnedEntityMetaData());
		addAttribute(FileMeta.ID, ROLE_ID).setVisible(false).setLabel("Id");
		addAttribute(FileMeta.FILENAME, ROLE_LABEL, ROLE_LOOKUP).setDataType(STRING).setNillable(false)
				.setLabel("Filename");
		addAttribute(FileMeta.CONTENT_TYPE, ROLE_LOOKUP).setDataType(STRING).setLabel("Content-type");
		addAttribute(FileMeta.SIZE).setDataType(STRING).setLabel("Size").setDescription("File size in bytes");
		addAttribute(FileMeta.URL).setDataType(HYPERLINK).setLabel("URL").setDescription("File download URL")
				.setUnique(true);
	}
}
