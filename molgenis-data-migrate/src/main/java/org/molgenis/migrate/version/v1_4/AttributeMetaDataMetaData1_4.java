package org.molgenis.migrate.version.v1_4;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.LongField;

public class AttributeMetaDataMetaData1_4 extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "attributes";
	public static final String IDENTIFIER = "identifier";
	public static final String ENTITY = "entity";
	public static final String NAME = "name";
	public static final String DATA_TYPE = "dataType";
	public static final String PART_OF_ATTRIBUTE = "partOfAttribute";
	public static final String REF_ENTITY = "refEntity";
	public static final String NILLABLE = "nillable";
	public static final String AUTO = "auto";
	public static final String ID_ATTRIBUTE = "idAttribute";
	public static final String LOOKUP_ATTRIBUTE = "lookupAttribute";
	public static final String VISIBLE = "visible";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String AGGREGATEABLE = "aggregateable";
	public static final String ENUM_OPTIONS = "enumOptions";
	public static final String RANGE_MIN = "rangeMin";
	public static final String RANGE_MAX = "rangeMax";
	public static final String LABEL_ATTRIBUTE = "labelAttribute";
	public static final String READ_ONLY = "readOnly";
	public static final String UNIQUE = "unique";
	public static final String TAGS = "tags";

	public AttributeMetaDataMetaData1_4()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		addAttribute(ENTITY).setNillable(false).setDataType(XREF).setRefEntity(new EntityMetaDataMetaData1_4());
		addAttribute(NAME).setNillable(false);
		addAttribute(DATA_TYPE);
		addAttribute(PART_OF_ATTRIBUTE).setDataType(STRING);
		addAttribute(REF_ENTITY).setDataType(XREF).setRefEntity(new EntityMetaDataMetaData1_4());
		addAttribute(NILLABLE).setDataType(BOOL).setNillable(false);
		addAttribute(AUTO).setDataType(BOOL).setNillable(false);
		addAttribute(ID_ATTRIBUTE).setDataType(BOOL).setNillable(false);
		addAttribute(LOOKUP_ATTRIBUTE).setDataType(BOOL).setNillable(false);
		addAttribute(VISIBLE).setDataType(BOOL).setNillable(false);
		addAttribute(LABEL);
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(AGGREGATEABLE).setDataType(BOOL).setNillable(false);
		addAttribute(ENUM_OPTIONS).setDataType(TEXT);
		addAttribute(RANGE_MIN).setDataType(new LongField());
		addAttribute(RANGE_MAX).setDataType(new LongField());
		addAttribute(LABEL_ATTRIBUTE).setDataType(BOOL).setNillable(false);
		addAttribute(READ_ONLY).setDataType(BOOL).setNillable(false);
		addAttribute(UNIQUE).setDataType(BOOL).setNillable(false);
		addAttribute(TAGS).setDataType(MREF).setRefEntity(new TagMetaData1_4());
	}
}