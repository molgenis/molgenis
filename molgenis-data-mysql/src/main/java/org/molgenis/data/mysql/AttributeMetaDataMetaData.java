package org.molgenis.data.mysql;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.LongField;

public class AttributeMetaDataMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "attributes";
	public static final String IDENTIFIER = "identifier";
	public static final String ENTITY = "entity";
	public static final String NAME = "name";
	public static final String DATA_TYPE = "dataType";
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

	public AttributeMetaDataMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(INT).setAuto(true);
		addAttribute(ENTITY).setNillable(false).setDataType(XREF).setRefEntity(MysqlEntityMetaDataRepository.META_DATA);
		addAttribute(NAME).setNillable(false);
		addAttribute(DATA_TYPE);
		addAttribute(REF_ENTITY).setDataType(XREF).setRefEntity(MysqlEntityMetaDataRepository.META_DATA);
		addAttribute(NILLABLE).setDataType(BOOL);
		addAttribute(AUTO).setDataType(BOOL);
		addAttribute(ID_ATTRIBUTE).setDataType(BOOL);
		addAttribute(LOOKUP_ATTRIBUTE).setDataType(BOOL);
		addAttribute(VISIBLE).setDataType(BOOL);
		addAttribute(LABEL);
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(AGGREGATEABLE).setDataType(BOOL);
		addAttribute(ENUM_OPTIONS).setDataType(TEXT);
		addAttribute(RANGE_MIN).setDataType(new LongField());
		addAttribute(RANGE_MAX).setDataType(new LongField());
		addAttribute(LABEL_ATTRIBUTE).setDataType(BOOL);
		addAttribute(READ_ONLY).setDataType(BOOL);
		addAttribute(UNIQUE).setDataType(BOOL);
	}
}
