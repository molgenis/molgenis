package org.molgenis.data.meta.model;

import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.support.EntityTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.support.AttributeUtils.getValidIdAttributeTypes;

@Component
public class AttributeMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Attribute";
	public static final String ATTRIBUTE_META_DATA = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String ENTITY = "entity";
	public static final String SEQUENCE_NR = "sequenceNr";
	public static final String TYPE = "type";
	public static final String IS_ID_ATTRIBUTE = "isIdAttribute";
	public static final String IS_LABEL_ATTRIBUTE = "isLabelAttribute";
	public static final String LOOKUP_ATTRIBUTE_INDEX = "lookupAttributeIndex";
	public static final String REF_ENTITY_TYPE = "refEntityType";
	/**
	 * Deleting an entity also deletes the referenced entity when cascading delete is enabled
	 */
	public static final String IS_CASCADE_DELETE = "isCascadeDelete";

	/**
	 * For attributes with data type ONE_TO_MANY defines the attribute in the referenced entity that owns the relationship.
	 */
	public static final String MAPPED_BY = "mappedBy";

	/**
	 * For attributes with data type ONE_TO_MANY defines how to sort the entity collection.
	 * Syntax: attribute_name,[ASC | DESC] [;attribute_name,[ASC | DESC]]*
	 * - If ASC or DESC is not specified, ASC (ascending order) is assumed.
	 * - If the ordering element is not specified, ordering by the id attribute of the associated entity is assumed.
	 */
	public static final String ORDER_BY = "orderBy";

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
	public static final String PARENT = "parent";
	public static final String CHILDREN = "children";
	public static final String TAGS = "tags";
	public static final String NULLABLE_EXPRESSION = "nullableExpression";
	public static final String VISIBLE_EXPRESSION = "visibleExpression";
	public static final String VALIDATION_EXPRESSION = "validationExpression";
	public static final String DEFAULT_VALUE = "defaultValue";

	private TagMetadata tagMetadata;
	private EntityTypeMetadata entityTypeMeta;

	public AttributeMetadata()
	{
		super(SIMPLE_NAME, PACKAGE_META);
	}

	public void init()
	{
		setId(ATTRIBUTE_META_DATA);
		setLabel("Attribute");
		setDescription("Meta data for attributes");

		addAttribute(ID, ROLE_ID).setVisible(false).setAuto(true).setLabel("Identifier");
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setReadOnly(true).setLabel("Name");
		addAttribute(ENTITY).setDataType(XREF)
							.setRefEntity(entityTypeMeta)
							.setLabel("Entity")
							.setNillable(false)
							.setReadOnly(true);
		addAttribute(SEQUENCE_NR).setDataType(INT)
								 .setLabel("Sequence number")
								 .setDescription("Number that defines order of attributes in a entity")
								 .setNillable(false);
		addAttribute(TYPE).setDataType(ENUM)
						  .setEnumOptions(AttributeType.getOptionsLowercase())
						  .setNillable(false)
						  .setLabel("Data type");
		addAttribute(IS_ID_ATTRIBUTE).setDataType(BOOL)
									 .setLabel("ID attribute")
									 .setValidationExpression(getIdAttributeValidationExpression());
		addAttribute(IS_LABEL_ATTRIBUTE).setDataType(BOOL).setLabel("Label attribute");
		addAttribute(LOOKUP_ATTRIBUTE_INDEX).setDataType(INT)
											.setLabel("Lookup attribute index")
											.setValidationExpression(getLookupAttributeValidationExpression());
		Attribute parentAttr = addAttribute(PARENT).setDataType(XREF).setRefEntity(this).setLabel("Attribute parent");
		addAttribute(CHILDREN).setDataType(ONE_TO_MANY)
							  .setRefEntity(this)
							  .setMappedBy(parentAttr)
							  .setOrderBy(new Sort(SEQUENCE_NR))
							  .setLabel("Attribute parts")
							  .setCascadeDelete(true);
		addAttribute(REF_ENTITY_TYPE).setDataType(XREF)
									 .setRefEntity(entityTypeMeta)
									 .setLabel("Referenced entity")
									 .setValidationExpression(getRefEntityValidationExpression());
		addAttribute(IS_CASCADE_DELETE).setDataType(BOOL)
									   .setLabel("Cascade delete")
									   .setDescription("Delete corresponding referenced entities on delete")
									   .setValidationExpression(getCascadeDeleteValidationExpression());
		addAttribute(MAPPED_BY).setDataType(XREF)
							   .setRefEntity(this)
							   .setLabel("Mapped by")
							   .setDescription(
									   "Attribute in the referenced entity that owns the relationship of a onetomany attribute")
							   .setValidationExpression(getMappedByValidationExpression())
							   .setReadOnly(true);
		addAttribute(ORDER_BY).setLabel("Order by")
							  .setDescription(
									  "Order expression that defines entity collection order of a onetomany attribute (e.g. \"attr0\", \"attr0,ASC\", \"attr0,DESC\" or \"attr0,ASC;attr1,DESC\"")
							  .setValidationExpression(getOrderByValidationExpression());
		addAttribute(EXPRESSION).setNillable(true)
								.setLabel("Expression")
								.setDescription("Computed value expression in Magma JavaScript");
		addAttribute(IS_NULLABLE).setDataType(BOOL)
								 .setNillable(false)
								 .setLabel("Nillable")
								 .setDefaultValue("true")
								 .setValidationExpression(getNullableValidationExpression());
		addAttribute(IS_AUTO).setDataType(BOOL)
							 .setNillable(false)
							 .setLabel("Auto")
							 .setDescription("Auto generated values")
							 .setValidationExpression(getAutoValidationExpression());
		addAttribute(IS_VISIBLE).setDataType(BOOL).setNillable(false).setLabel("Visible");
		addAttribute(LABEL, ROLE_LOOKUP).setLabel("Label");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		addAttribute(IS_AGGREGATABLE).setDataType(BOOL)
									 .setNillable(false)
									 .setLabel("Aggregatable")
									 .setValidationExpression(getAggregatableExpression());
		addAttribute(ENUM_OPTIONS).setDataType(TEXT)
								  .setLabel("Enum values")
								  .setDescription("For data type ENUM")
								  .setValidationExpression(getEnumOptionsValidationExpression());
		addAttribute(RANGE_MIN).setDataType(LONG)
							   .setLabel("Range min")
							   .setValidationExpression(getRangeValidationExpression(RANGE_MIN));
		addAttribute(RANGE_MAX).setDataType(LONG)
							   .setLabel("Range max")
							   .setValidationExpression(getRangeValidationExpression(RANGE_MAX));
		addAttribute(IS_READ_ONLY).setDataType(BOOL).setNillable(false).setLabel("Read-only");
		addAttribute(IS_UNIQUE).setDataType(BOOL).setNillable(false).setLabel("Unique");
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetadata).setLabel("Tags");
		addAttribute(NULLABLE_EXPRESSION).setDataType(SCRIPT).setNillable(true).setLabel("Nullable expression");
		addAttribute(VISIBLE_EXPRESSION).setDataType(SCRIPT).setNillable(true).setLabel("Visible expression");
		addAttribute(VALIDATION_EXPRESSION).setDataType(SCRIPT).setNillable(true).setLabel("Validation expression");
		addAttribute(DEFAULT_VALUE).setDataType(TEXT).setNillable(true).setLabel("Default value");
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setTagMetadata(TagMetadata tagMetadata)
	{
		this.tagMetadata = requireNonNull(tagMetadata);
	}

	@Autowired
	public void setEntityTypeMetadata(EntityTypeMetadata entityTypeMeta)
	{
		this.entityTypeMeta = requireNonNull(entityTypeMeta);
	}

	private static String getMappedByValidationExpression()
	{
		return "$('" + MAPPED_BY + "').isNull().and($('" + TYPE + "').eq('" + getValueString(ONE_TO_MANY)
				+ "').not()).or(" + "$('" + MAPPED_BY + "').isNull().not().and($('" + TYPE + "').eq('" + getValueString(
				ONE_TO_MANY) + "'))).value()";
	}

	private static String getOrderByValidationExpression()
	{
		String regex = "/^\\w+(,(ASC|DESC))?(;\\w+(,(ASC|DESC))?)*$/";
		return "$('" + ORDER_BY + "').isNull().or(" + "$('" + ORDER_BY + "').matches(" + regex + ").and($('" + TYPE
				+ "').eq('" + getValueString(ONE_TO_MANY) + "'))).value()";
	}

	private static String getNullableValidationExpression()
	{
		return "$('" + IS_NULLABLE + "').eq(true).or(" + "$('" + NULLABLE_EXPRESSION + "').isNull()).value()";
	}

	private static String getEnumOptionsValidationExpression()
	{
		return "$('" + ENUM_OPTIONS + "').isNull().and($('" + TYPE + "').eq('" + getValueString(ENUM) + "').not()).or("
				+ "$('" + ENUM_OPTIONS + "').isNull().not().and($('" + TYPE + "').eq('" + getValueString(ENUM)
				+ "'))).value()";
	}

	private static String getRefEntityValidationExpression()
	{
		String regex = "/^(" + Arrays.stream(AttributeType.values())
									 .filter(EntityTypeUtils::isReferenceType)
									 .map(AttributeType::getValueString)
									 .collect(Collectors.joining("|")) + ")$/";

		return "$('" + REF_ENTITY_TYPE + "').isNull().and($('" + TYPE + "').matches(" + regex + ").not()).or(" + "$('"
				+ REF_ENTITY_TYPE + "').isNull().not().and($('" + TYPE + "').matches(" + regex + "))).value()";
	}

	private static String getCascadeDeleteValidationExpression()
	{
		return "$('" + IS_CASCADE_DELETE + "').isNull().or(" + "$('" + REF_ENTITY_TYPE + "').isNull().not()).value()";
	}

	private static String getAutoValidationExpression()
	{
		String dateTypeRegex = "/^(" + Arrays.stream(AttributeType.values())
											 .filter(EntityTypeUtils::isDateType)
											 .map(AttributeType::getValueString)
											 .collect(Collectors.joining("|")) + ")$/";

		String autoIsTrue = "$('" + IS_AUTO + "').eq(true)";
		String autoIsFalse = "$('" + IS_AUTO + "').eq(false)";
		String autoIsTrueAndIsIdIsTrueAndTypeIsStringOrNull =
				autoIsTrue + ".and($('" + IS_ID_ATTRIBUTE + "').eq(true).and($('" + TYPE + "').eq('" + getValueString(
						STRING) + "').or($('" + TYPE + "').isNull())))";
		String autoIsTrueAndIsIdIsFalseOrNullAndTypeIsDateType =
				autoIsTrue + ".and($('" + IS_ID_ATTRIBUTE + "').eq(false).or($('" + IS_ID_ATTRIBUTE
						+ "').isNull())).and($('" + TYPE + "').matches(" + dateTypeRegex + "))";

		return autoIsFalse + ".or(" + autoIsTrueAndIsIdIsTrueAndTypeIsStringOrNull + ").or("
				+ autoIsTrueAndIsIdIsFalseOrNullAndTypeIsDateType + ").value()";
	}

	private static String getRangeValidationExpression(String attribute)
	{
		String regex = "/^(" + Arrays.stream(AttributeType.values())
									 .filter(EntityTypeUtils::isIntegerType)
									 .map(AttributeType::getValueString)
									 .collect(Collectors.joining("|")) + ")$/";
		String rangeIsNull = "$('" + attribute + "').isNull()";

		return rangeIsNull + ".or(" + rangeIsNull + ".not().and($('" + TYPE + "').matches(" + regex + "))).value()";
	}

	// Package private for testability
	static String getIdAttributeValidationExpression()
	{
		String isIdIsTrue = "$('" + IS_ID_ATTRIBUTE + "').eq(true)";
		String isIdIsFalseOrNull = "$('" + IS_ID_ATTRIBUTE + "').eq(false).or($('" + IS_ID_ATTRIBUTE + "').isNull())";

		// Use the valid ID attribute types to constuct the validation expression
		List<String> typeExpressions = getValidIdAttributeTypes().stream()
																 .map(attributeType -> "$('" + TYPE + "').eq('"
																		 + getValueString(attributeType) + "')")
																 .collect(Collectors.toList());

		boolean first = true;
		StringBuilder typeIsNullOrStringOrIntOrLong = new StringBuilder();
		for (String expression : typeExpressions)
		{
			if (first)
			{
				typeIsNullOrStringOrIntOrLong.append(expression);
				first = false;
				continue;
			}
			typeIsNullOrStringOrIntOrLong.append(".or(").append(expression).append(")");
		}
		typeIsNullOrStringOrIntOrLong.append(".or($('" + TYPE + "').isNull())");

		String nullableIsFalse = "$('" + IS_NULLABLE + "').eq(false)";

		return isIdIsFalseOrNull + ".or(" + isIdIsTrue + ".and(" + typeIsNullOrStringOrIntOrLong + ").and("
				+ nullableIsFalse + ")).value()";
	}

	private static String getLookupAttributeValidationExpression()
	{
		String regex = "/^(" + Arrays.stream(AttributeType.values())
									 .filter(EntityTypeUtils::isReferenceType)
									 .map(AttributeType::getValueString)
									 .collect(Collectors.joining("|")) + ")$/";

		return "$('" + LOOKUP_ATTRIBUTE_INDEX + "').isNull().or(" + "$('" + LOOKUP_ATTRIBUTE_INDEX
				+ "').isNull().not().and($('" + TYPE + "').matches(" + regex + ").not())).value()";
	}

	private static String getAggregatableExpression()
	{
		String aggregatableIsNullOrFalse =
				"$('" + IS_AGGREGATABLE + "').isNull().or($('" + IS_AGGREGATABLE + "').eq(false))";
		String regex = "/^(" + Arrays.stream(AttributeType.values())
									 .filter(EntityTypeUtils::isReferenceType)
									 .map(AttributeType::getValueString)
									 .collect(Collectors.joining("|")) + ")$/";
		return aggregatableIsNullOrFalse + ".or(" + "$('" + TYPE + "')" + ".matches(" + regex + ")" + ".and(" + "$('"
				+ IS_NULLABLE + "')" + ".eq(false)" + ")" + ")" + ".or(" + "$('" + TYPE + "')" + ".matches(" + regex
				+ ").not()).value()";
	}
}
