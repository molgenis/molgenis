package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;

public class EntityMetaDataMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "entities";
	public static final String SIMPLE_NAME = "simpleName";
	public static final String BACKEND = "backend";
	public static final String FULL_NAME = "fullName";
	public static final String ID_ATTRIBUTE = "idAttribute";
	public static final String LABEL_ATTRIBUTE = "labelAttribute";
	public static final String ABSTRACT = "abstract";
	public static final String LABEL = "label";
	public static final String EXTENDS = "extends";
	public static final String DESCRIPTION = "description";
	public static final String PACKAGE = "package";
	public static final String TAGS = "tags";
	public static final String ATTRIBUTES = "attributes";

	public static final EntityMetaDataMetaData INSTANCE = new EntityMetaDataMetaData();

	private EntityMetaDataMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(FULL_NAME).setIdAttribute(true).setUnique(true).setNillable(false);
		addAttribute(SIMPLE_NAME).setNillable(false);
		addAttribute(BACKEND);
		addAttribute(PACKAGE).setDataType(XREF).setRefEntity(PackageRepository.META_DATA);
		addAttribute(ID_ATTRIBUTE);
		addAttribute(LABEL_ATTRIBUTE);
		addAttribute(ABSTRACT).setDataType(BOOL);
		addAttribute(LABEL).setLabelAttribute(true).setLookupAttribute(true);
		addAttribute(EXTENDS).setDataType(XREF).setRefEntity(this);
		addAttribute(DESCRIPTION).setDataType(TEXT).setLookupAttribute(false);
		addAttribute(TAGS).setDataType(MREF).setRefEntity(TagMetaData.INSTANCE);
		addAttribute(ATTRIBUTES).setDataType(MREF).setRefEntity(AttributeMetaDataMetaData.INSTANCE);
	}

}
