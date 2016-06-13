package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.*;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.LongField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AttributeMetaDataMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "attributes";
	public static final String ATTRIBUTE_META_DATA = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String DATA_TYPE = "dataType";
	public static final String REF_ENTITY = "refEntity";
	public static final String EXPRESSION = "expression";
	public static final String NILLABLE = "nillable";
	public static final String AUTO = "auto";
	public static final String VISIBLE = "visible";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String AGGREGATEABLE = "aggregateable";
	public static final String ENUM_OPTIONS = "enumOptions";
	public static final String RANGE_MIN = "rangeMin";
	public static final String RANGE_MAX = "rangeMax";
	public static final String READ_ONLY = "readOnly";
	public static final String UNIQUE = "unique";
	public static final String PARTS = "parts";
	public static final String TAGS = "tags";
	public static final String VISIBLE_EXPRESSION = "visibleExpression";
	public static final String VALIDATION_EXPRESSION = "validationExpression";
	public static final String DEFAULT_VALUE = "defaultValue";

	private TagMetaData tagMetaData;

	AttributeMetaDataMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_META);
	}

	public void init()
	{
		addAttribute(IDENTIFIER, ROLE_ID).setVisible(false).setAuto(true);
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setNillable(false);
		addAttribute(DATA_TYPE).setDataType(new EnumField()).setEnumOptions(FieldTypeEnum.getOptionsLowercase())
				.setNillable(false);
		addAttribute(PARTS).setDataType(MREF).setRefEntity(this);
		addAttribute(REF_ENTITY); // during bootstrapping the data type is set to XREF and the ref entity to entity meta
		addAttribute(EXPRESSION).setNillable(true);
		addAttribute(NILLABLE).setDataType(BOOL).setNillable(false);
		addAttribute(AUTO).setDataType(BOOL).setNillable(false);
		addAttribute(VISIBLE).setDataType(BOOL).setNillable(false);
		addAttribute(LABEL, ROLE_LOOKUP);
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(AGGREGATEABLE).setDataType(BOOL).setNillable(false);
		addAttribute(ENUM_OPTIONS).setDataType(TEXT);
		addAttribute(RANGE_MIN).setDataType(new LongField());
		addAttribute(RANGE_MAX).setDataType(new LongField());
		addAttribute(READ_ONLY).setDataType(BOOL).setNillable(false);
		addAttribute(UNIQUE).setDataType(BOOL).setNillable(false);
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetaData);
		addAttribute(VISIBLE_EXPRESSION).setDataType(SCRIPT).setNillable(true);
		addAttribute(VALIDATION_EXPRESSION).setDataType(SCRIPT).setNillable(true);
		addAttribute(DEFAULT_VALUE).setDataType(TEXT).setNillable(true);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setTagMetaData(TagMetaData tagMetaData)
	{
		this.tagMetaData = requireNonNull(tagMetaData);
	}
}
