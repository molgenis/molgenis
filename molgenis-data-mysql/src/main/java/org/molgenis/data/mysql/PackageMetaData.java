package org.molgenis.data.mysql;

import static org.molgenis.MolgenisFieldTypes.TEXT;

import org.molgenis.data.support.DefaultEntityMetaData;

public class PackageMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "package";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";

	public PackageMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(NAME).setIdAttribute(true).setNillable(false).setLabelAttribute(true);
		addAttribute(DESCRIPTION).setDataType(TEXT);
	}

}
