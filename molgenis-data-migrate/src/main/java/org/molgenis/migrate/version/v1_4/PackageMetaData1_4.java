package org.molgenis.migrate.version.v1_4;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class PackageMetaData1_4 extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "packages";
	public static final String FULL_NAME = "fullName";
	public static final String SIMPLE_NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String PARENT = "parent";
	public static final String TAGS = "tags";

	public PackageMetaData1_4()
	{
		super(ENTITY_NAME);

		addAttribute(FULL_NAME).setIdAttribute(true).setNillable(false).setLabelAttribute(true);
		addAttribute(SIMPLE_NAME);
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(PARENT).setDataType(XREF).setRefEntity(this);
		addAttribute(TAGS).setDataType(MREF).setRefEntity(new TagMetaData1_4());
	}

}