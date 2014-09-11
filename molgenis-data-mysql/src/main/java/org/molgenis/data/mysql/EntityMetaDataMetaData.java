package org.molgenis.data.mysql;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class EntityMetaDataMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "entities";
	public static final String NAME = "name";
	public static final String ID_ATTRIBUTE = "idAttribute";
	public static final String ABSTRACT = "abstract";
	public static final String LABEL = "label";
	public static final String EXTENDS = "extends";
	public static final String DESCRIPTION = "description";

	public EntityMetaDataMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(NAME).setIdAttribute(true).setNillable(false);
		addAttribute(ID_ATTRIBUTE);
		addAttribute(ABSTRACT).setDataType(BOOL);
		addAttribute(LABEL);
		addAttribute(EXTENDS).setDataType(XREF).setRefEntity(this);
		addAttribute(DESCRIPTION).setDataType(TEXT);
	}

}
