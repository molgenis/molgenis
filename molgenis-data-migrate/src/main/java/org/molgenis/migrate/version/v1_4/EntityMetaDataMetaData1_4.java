package org.molgenis.migrate.version.v1_4;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class EntityMetaDataMetaData1_4 extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "entities";
	public static final String SIMPLE_NAME = "simpleName";
	public static final String FULL_NAME = "fullName";
	public static final String ID_ATTRIBUTE = "idAttribute";
	public static final String LABEL_ATTRIBUTE = "labelAttribute";
	public static final String ABSTRACT = "abstract";
	public static final String LABEL = "label";
	public static final String EXTENDS = "extends";
	public static final String DESCRIPTION = "description";
	public static final String PACKAGE = "package";
	public static final String TAGS = "tags";

	public EntityMetaDataMetaData1_4()
	{
		super(ENTITY_NAME);
		addAttribute(FULL_NAME).setIdAttribute(true).setUnique(true).setNillable(false);
		addAttribute(SIMPLE_NAME).setNillable(false);
		addAttribute(PACKAGE).setDataType(XREF).setRefEntity(new PackageMetaData1_4());
		addAttribute(ID_ATTRIBUTE);
		addAttribute(LABEL_ATTRIBUTE);
		addAttribute(ABSTRACT).setDataType(BOOL);
		addAttribute(LABEL);
		addAttribute(EXTENDS).setDataType(XREF).setRefEntity(this);
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(TAGS).setDataType(MREF).setRefEntity(new TagMetaData1_4());
	}

}