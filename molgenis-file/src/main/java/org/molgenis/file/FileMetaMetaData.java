package org.molgenis.file;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileMetaMetaData extends SystemEntityMetaDataImpl
{
	private OwnedEntityMetaData ownedEntityMetaData;

	@Override
	public void init()
	{
		setName(FileMeta.ENTITY_NAME);
		setExtends(ownedEntityMetaData);
		addAttribute(FileMeta.ID, ROLE_ID).setVisible(false).setLabel("Id");
		addAttribute(FileMeta.FILENAME, ROLE_LABEL, ROLE_LOOKUP).setDataType(STRING).setNillable(false)
				.setLabel("Filename");
		addAttribute(FileMeta.CONTENT_TYPE, ROLE_LOOKUP).setDataType(STRING).setLabel("Content-type");
		addAttribute(FileMeta.SIZE).setDataType(STRING).setLabel("Size").setDescription("File size in bytes");
		addAttribute(FileMeta.URL).setDataType(HYPERLINK).setLabel("URL").setDescription("File download URL")
				.setUnique(true);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setOwnedEntityMetaData(OwnedEntityMetaData ownedEntityMetaData)
	{
		this.ownedEntityMetaData = requireNonNull(ownedEntityMetaData);
	}
}
