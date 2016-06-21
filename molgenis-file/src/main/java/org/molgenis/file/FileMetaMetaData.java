package org.molgenis.file;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileMetaMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "FileMeta";
	public static final String FILE_META = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String FILENAME = "filename";
	public static final String CONTENT_TYPE = "contentType";
	public static final String SIZE = "size";
	public static final String URL = "url";

	private final OwnedEntityMetaData ownedEntityMetaData;

	@Autowired
	FileMetaMetaData(OwnedEntityMetaData ownedEntityMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.ownedEntityMetaData = requireNonNull(ownedEntityMetaData);
	}

	@Override
	public void init()
	{
		setExtends(ownedEntityMetaData);
		addAttribute(ID, ROLE_ID).setVisible(false).setLabel("Id");
		addAttribute(FILENAME, ROLE_LABEL, ROLE_LOOKUP).setDataType(STRING).setNillable(false).setLabel("Filename");
		addAttribute(CONTENT_TYPE, ROLE_LOOKUP).setDataType(STRING).setLabel("Content-type");
		addAttribute(SIZE).setDataType(STRING).setLabel("Size").setDescription("File size in bytes");
		addAttribute(URL).setDataType(HYPERLINK).setLabel("URL").setDescription("File download URL").setUnique(true);
	}
}
