package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.data.support.DefaultEntityMetaData;

public class PackageMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "packages";
	public static final String FULL_NAME = "fullName";
	public static final String SIMPLE_NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String PARENT = "parent";
	public static final String TAGS = "tags";

	public static final PackageMetaData INSTANCE = new PackageMetaData();

	private PackageMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(FULL_NAME, ROLE_ID, ROLE_LABEL).setNillable(false);
		addAttribute(SIMPLE_NAME);
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(PARENT).setDataType(XREF).setRefEntity(this);
		addAttribute(TAGS).setDataType(MREF).setRefEntity(TagMetaData.INSTANCE);
	}

}
