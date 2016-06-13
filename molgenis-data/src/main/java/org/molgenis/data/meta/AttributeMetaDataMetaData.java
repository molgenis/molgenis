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
		setLabel("Attribute");
		setDescription("Meta data for attributes");

		addAttribute(IDENTIFIER, ROLE_ID).setVisible(false).setAuto(true);
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setLabel("Name");
		addAttribute(DATA_TYPE).setDataType(new EnumField()).setEnumOptions(FieldTypeEnum.getOptionsLowercase())
				.setNillable(false).setLabel("Data type");
		addAttribute(PARTS).setDataType(MREF).setRefEntity(this).setLabel("Attribute parts");
		// during bootstrapping the data type is set to XREF and the ref entity to entity meta
		addAttribute(REF_ENTITY).setLabel("Referenced entity");
		addAttribute(EXPRESSION).setNillable(true).setLabel("Expression").setDescription("Computed value expression in Magma JavaScript");
		addAttribute(NILLABLE).setDataType(BOOL).setNillable(false).setLabel("Nillable");
		addAttribute(AUTO).setDataType(BOOL).setNillable(false).setLabel("Auto").setDescription("Auto generated values");
		addAttribute(VISIBLE).setDataType(BOOL).setNillable(false).setLabel("Visible");
		addAttribute(LABEL, ROLE_LOOKUP).setLabel("Label");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		addAttribute(AGGREGATEABLE).setDataType(BOOL).setNillable(false).setLabel("Aggregatable");
		addAttribute(ENUM_OPTIONS).setDataType(TEXT).setLabel("Enum values").setDescription("For data type ENUM");
		addAttribute(RANGE_MIN).setDataType(new LongField()).setLabel("Range min");
		addAttribute(RANGE_MAX).setDataType(new LongField()).setLabel("Range max");
		addAttribute(READ_ONLY).setDataType(BOOL).setNillable(false).setLabel("Read-only");
		addAttribute(UNIQUE).setDataType(BOOL).setNillable(false).setLabel("Unique");
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetaData).setLabel("Tags");
		addAttribute(VISIBLE_EXPRESSION).setDataType(SCRIPT).setNillable(true).setLabel("Visible expression");
		addAttribute(VALIDATION_EXPRESSION).setDataType(SCRIPT).setNillable(true).setLabel("Validation expression");
		addAttribute(DEFAULT_VALUE).setDataType(TEXT).setNillable(true).setLabel("Default value");
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setTagMetaData(TagMetaData tagMetaData)
	{
		this.tagMetaData = requireNonNull(tagMetaData);
	}
}
