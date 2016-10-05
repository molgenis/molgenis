package org.molgenis.data.meta.model;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class AttributeMetaDataMetaData extends SystemEntityType
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
	private EntityTypeMetadata entityTypeMeta;

	public AttributeMetaDataMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_META);
	}

	public void init()
	{
		setLabel("Attribute");
		setDescription("Meta data for attributes");

		addAttribute(IDENTIFIER, ROLE_ID).setVisible(false).setAuto(true).setLabel("Identifier");
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setReadOnly(true).setLabel("Name");
		addAttribute(DATA_TYPE).setDataType(ENUM).setEnumOptions(AttributeType.getOptionsLowercase()).setNillable(false)
				.setLabel("Data type");
		addAttribute(PARTS).setDataType(MREF).setRefEntity(this).setLabel("Attribute parts");
		addAttribute(REF_ENTITY).setDataType(XREF).setRefEntity(entityTypeMeta).setLabel("Referenced entity")
				.setValidationExpression(getRefEntityValidationExpression());
		addAttribute(EXPRESSION).setNillable(true).setLabel("Expression")
				.setDescription("Computed value expression in Magma JavaScript");
		addAttribute(NILLABLE).setDataType(BOOL).setNillable(false).setLabel("Nillable");
		addAttribute(AUTO).setDataType(BOOL).setNillable(false).setLabel("Auto")
				.setDescription("Auto generated values");
		addAttribute(VISIBLE).setDataType(BOOL).setNillable(false).setLabel("Visible");
		addAttribute(LABEL, ROLE_LOOKUP).setLabel("Label");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		addAttribute(AGGREGATEABLE).setDataType(BOOL).setNillable(false).setLabel("Aggregatable");
		addAttribute(ENUM_OPTIONS).setDataType(TEXT).setLabel("Enum values").setDescription("For data type ENUM")
				.setValidationExpression(getEnumOptionsValidationExpression());
		addAttribute(RANGE_MIN).setDataType(LONG).setLabel("Range min");
		addAttribute(RANGE_MAX).setDataType(LONG).setLabel("Range max");
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

	@Autowired
	public void setEntityTypeMetaData(EntityTypeMetadata entityTypeMeta)
	{
		this.entityTypeMeta = requireNonNull(entityTypeMeta);
	}

	private static String getEnumOptionsValidationExpression()
	{
		return "$('" + ENUM_OPTIONS + "').isNull().and($('" + DATA_TYPE + "').eq('" + getValueString(ENUM)
				+ "').not()).or(" + "$('" + ENUM_OPTIONS + "').isNull().not().and($('" + DATA_TYPE + "').eq('"
				+ getValueString(ENUM) + "'))).value()";
	}

	private static String getRefEntityValidationExpression()
	{
		String regex =
				"/^(" + getValueString(CATEGORICAL) + '|' + getValueString(CATEGORICAL_MREF) + '|' + getValueString(
						FILE) + '|' + getValueString(MREF) + '|' + getValueString(XREF) + ")$/";

		return "$('" + REF_ENTITY + "').isNull().and($('" + DATA_TYPE + "').matches(" + regex + ").not()).or(" + "$('"
				+ REF_ENTITY + "').isNull().not().and($('" + DATA_TYPE + "').matches(" + regex + "))).value()";
	}
}
