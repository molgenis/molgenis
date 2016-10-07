package org.molgenis.data.meta.model;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.support.EntityTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class AttributeMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Attribute";
	public static final String ATTRIBUTE_META_DATA = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME;

	// Columns witin the Attribute repository
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String REF_ENTITY_TYPE = "refEntityType";

	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";

	public static final String IS_NULLABLE = "isNullable";
	public static final String IS_AUTO = "isAuto";
	public static final String IS_VISIBLE = "isVisible";
	public static final String IS_UNIQUE = "isUnique";
	public static final String IS_READ_ONLY = "isReadOnly";
	public static final String IS_AGGREGATABLE = "isAggregatable";

	public static final String EXPRESSION = "expression";
	public static final String ENUM_OPTIONS = "enumOptions";
	public static final String RANGE_MIN = "rangeMin";
	public static final String RANGE_MAX = "rangeMax";
	public static final String PARTS = "parts";
	public static final String TAGS = "tags";
	public static final String VISIBLE_EXPRESSION = "visibleExpression";
	public static final String VALIDATION_EXPRESSION = "validationExpression";
	public static final String DEFAULT_VALUE = "defaultValue";

	/**
	 * For attributes with data type ONE_TO_MANY defines the attribute in the referenced entity that owns the relationship.
	 */
	public static final String MAPPED_BY = "mappedBy";

	private TagMetaData tagMetaData;
	private EntityTypeMetadata entityTypeMeta;

	public AttributeMetadata()
	{
		super(SIMPLE_NAME, PACKAGE_META);
	}

	public void init()
	{
		setLabel("Attribute");
		setDescription("Meta data for attributes");

		addAttribute(ID, ROLE_ID).setVisible(false).setAuto(true).setLabel("Identifier");
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setReadOnly(true).setLabel("Name");
		addAttribute(TYPE).setDataType(ENUM).setEnumOptions(AttributeType.getOptionsLowercase()).setNillable(false)
				.setLabel("Data type");
		addAttribute(PARTS).setDataType(MREF).setRefEntity(this).setLabel("Attribute parts");
		addAttribute(REF_ENTITY_TYPE).setDataType(XREF).setRefEntity(entityTypeMeta).setLabel("Referenced entity")
				.setValidationExpression(getRefEntityValidationExpression());
		addAttribute(MAPPED_BY).setDataType(XREF).setRefEntity(this).setLabel("Mapped by").setDescription(
				"Attribute in the referenced entity that owns the relationship of a onetomany attribute")
				.setValidationExpression(getMappedByValidationExpression()).setReadOnly(true);
		addAttribute(EXPRESSION).setNillable(true).setLabel("Expression")
				.setDescription("Computed value expression in Magma JavaScript");
		addAttribute(IS_NULLABLE).setDataType(BOOL).setNillable(false).setLabel("Nillable");
		addAttribute(IS_AUTO).setDataType(BOOL).setNillable(false).setLabel("Auto")
				.setDescription("Auto generated values");
		addAttribute(IS_VISIBLE).setDataType(BOOL).setNillable(false).setLabel("Visible");
		addAttribute(LABEL, ROLE_LOOKUP).setLabel("Label");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		addAttribute(IS_AGGREGATABLE).setDataType(BOOL).setNillable(false).setLabel("Aggregatable");
		addAttribute(ENUM_OPTIONS).setDataType(TEXT).setLabel("Enum values").setDescription("For data type ENUM")
				.setValidationExpression(getEnumOptionsValidationExpression());
		addAttribute(RANGE_MIN).setDataType(LONG).setLabel("Range min");
		addAttribute(RANGE_MAX).setDataType(LONG).setLabel("Range max");
		addAttribute(IS_READ_ONLY).setDataType(BOOL).setNillable(false).setLabel("Read-only");
		addAttribute(IS_UNIQUE).setDataType(BOOL).setNillable(false).setLabel("Unique");
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

	private static String getMappedByValidationExpression()
	{
		return "$('" + MAPPED_BY + "').isNull().and($('" + TYPE + "').eq('" + getValueString(ONE_TO_MANY)
				+ "').not()).or(" + "$('" + MAPPED_BY + "').isNull().not().and($('" + TYPE + "').eq('" + getValueString(
				ONE_TO_MANY) + "'))).value()";
	}

	private static String getEnumOptionsValidationExpression()
	{
		return "$('" + ENUM_OPTIONS + "').isNull().and($('" + TYPE + "').eq('" + getValueString(ENUM) + "').not()).or("
				+ "$('" + ENUM_OPTIONS + "').isNull().not().and($('" + TYPE + "').eq('" + getValueString(ENUM)
				+ "'))).value()";
	}

	private static String getRefEntityValidationExpression()
	{
		String regex = "/^(" + Arrays.stream(AttributeType.values()).filter(EntityTypeUtils::isReferenceType)
				.map(AttributeType::getValueString).collect(Collectors.joining("|")) + ")$/";

		return "$('" + REF_ENTITY_TYPE + "').isNull().and($('" + TYPE + "').matches(" + regex + ").not()).or(" + "$('"
				+ REF_ENTITY_TYPE + "').isNull().not().and($('" + TYPE + "').matches(" + regex + "))).value()";
	}
}
