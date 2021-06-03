package org.molgenis.data.meta.model;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AttributeMetadata extends SystemEntityType {
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
  /** Deleting an entity also deletes the referenced entity when cascading delete is enabled */
  public static final String IS_CASCADE_DELETE = "isCascadeDelete";

  /**
   * For attributes with data type ONE_TO_MANY defines the attribute in the referenced entity that
   * owns the relationship.
   */
  public static final String MAPPED_BY = "mappedBy";

  /**
   * For attributes with data type ONE_TO_MANY defines how to sort the entity collection. Syntax:
   * attribute_name,[ASC | DESC] [;attribute_name,[ASC | DESC]]* - If ASC or DESC is not specified,
   * ASC (ascending order) is assumed. - If the ordering element is not specified, ordering by the
   * id attribute of the associated entity is assumed.
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
  public static final String MAX_LENGTH = "maxLength";

  private TagMetadata tagMetadata;
  private EntityTypeMetadata entityTypeMeta;

  public AttributeMetadata() {
    super(SIMPLE_NAME, PACKAGE_META);
  }

  public void init() {
    setId(ATTRIBUTE_META_DATA);
    setLabel(SIMPLE_NAME);
    setDescription("Meta data for attributes");

    addAttribute(ID, ROLE_ID).setVisible(false).setAuto(true).setLabel("Identifier");
    addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP)
        .setNillable(false)
        .setReadOnly(true)
        .setLabel("Name");
    addAttribute(ENTITY)
        .setDataType(XREF)
        .setRefEntity(entityTypeMeta)
        .setLabel("Entity")
        .setNillable(false)
        .setReadOnly(true);
    addAttribute(SEQUENCE_NR)
        .setDataType(INT)
        .setLabel("Sequence number")
        .setDescription("Number that defines order of attributes in a entity")
        .setNillable(false);
    addAttribute(TYPE)
        .setDataType(ENUM)
        .setEnumOptions(AttributeType.getOptionsLowercase())
        .setNillable(false)
        .setLabel("Data type");
    addAttribute(IS_ID_ATTRIBUTE)
        .setDataType(BOOL)
        .setLabel("ID attribute")
        .setValidationExpression(
            "!{isIdAttribute} or "
                + "{type} anyof ['email','hyperlink','int','long','string'] and !{isNullable}");
    addAttribute(IS_LABEL_ATTRIBUTE).setDataType(BOOL).setLabel("Label attribute");
    addAttribute(LOOKUP_ATTRIBUTE_INDEX)
        .setDataType(INT)
        .setLabel("Lookup attribute index")
        .setValidationExpression(
            "{lookupAttributeIndex} empty or "
                + "['categorical','categoricalmref','file','mref','onetomany','xref'] notcontains {type}");
    Attribute parentAttr =
        addAttribute(PARENT).setDataType(XREF).setRefEntity(this).setLabel("Attribute parent");
    addAttribute(CHILDREN)
        .setDataType(ONE_TO_MANY)
        .setRefEntity(this)
        .setMappedBy(parentAttr)
        .setOrderBy(new Sort(SEQUENCE_NR))
        .setLabel("Attribute parts")
        .setCascadeDelete(true);
    addAttribute(REF_ENTITY_TYPE)
        .setDataType(XREF)
        .setRefEntity(entityTypeMeta)
        .setLabel("Referenced entity")
        .setValidationExpression(
            "{refEntityType} empty != "
                + "['categorical','categoricalmref','file','mref','onetomany','xref'] contains {type}");
    addAttribute(IS_CASCADE_DELETE)
        .setDataType(BOOL)
        .setLabel("Cascade delete")
        .setDescription("Delete corresponding referenced entities on delete")
        .setValidationExpression("!{isCascadeDelete} or {refEntityType} notempty");
    addAttribute(MAPPED_BY)
        .setDataType(XREF)
        .setRefEntity(this)
        .setLabel("Mapped by")
        .setDescription(
            "Attribute in the referenced entity that owns the relationship of a onetomany attribute")
        .setValidationExpression("{mappedBy} notempty = ({type} = 'onetomany')")
        .setReadOnly(true);
    addAttribute(ORDER_BY)
        .setLabel("Order by")
        .setDescription(
            "Order expression that defines entity collection order of a onetomany attribute (e.g. \"attr0\", \"attr0,ASC\", \"attr0,DESC\" or \"attr0,ASC;attr1,DESC\"")
        .setValidationExpression(
            "{orderBy} empty or "
                + "{type} = 'onetomany' and "
                + "regex('^\\\\w+(,(ASC|DESC))?(;\\\\w+(,(ASC|DESC))?)*$', {orderBy})");
    addAttribute(EXPRESSION)
        .setNillable(true)
        .setLabel("Expression")
        .setDescription("Computed value expression in Magma JavaScript");
    addAttribute(IS_NULLABLE)
        .setDataType(BOOL)
        .setNillable(false)
        .setLabel("Nillable")
        .setDefaultValue("true")
        .setValidationExpression("{isNullable} or {nullableExpression} empty");
    addAttribute(IS_AUTO)
        .setDataType(BOOL)
        .setNillable(false)
        .setLabel("Auto")
        .setDescription("Auto generated values")
        .setValidationExpression(
            "!{isAuto} or "
                + "!!{isIdAttribute} and {type}='string' or "
                + "!{isIdAttribute} and {type} anyof ['date','datetime']");
    addAttribute(IS_VISIBLE).setDataType(BOOL).setNillable(false).setLabel("Visible");
    addAttribute(LABEL, ROLE_LOOKUP).setLabel("Label");
    addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
    addAttribute(IS_AGGREGATABLE)
        .setDataType(BOOL)
        .setNillable(false)
        .setLabel("Aggregatable")
        .setValidationExpression(
            "!{isAggregatable} or "
                + "!{isNullable} or "
                + "!({type} anyof ['categorical','categoricalmref','file','mref','onetomany','xref'])");
    addAttribute(ENUM_OPTIONS)
        .setDataType(TEXT)
        .setLabel("Enum values")
        .setDescription("For data type ENUM")
        .setNullableExpression("{type} != 'enum'")
        .setValidationExpression("{enumOptions} notempty = ({type} = 'enum')");
    addAttribute(RANGE_MIN)
        .setDataType(LONG)
        .setLabel("Range min")
        .setValidationExpression("{rangeMin} empty or {type} anyof ['int','long']");
    addAttribute(RANGE_MAX)
        .setDataType(LONG)
        .setLabel("Range max")
        .setValidationExpression("{rangeMax} empty or {type} anyof ['int','long']");
    addAttribute(MAX_LENGTH)
        .setDataType(INT)
        .setLabel("Max length")
        .setDescription(
            "Maximum length for string attributes. If not set, falls back to the default for the attribute type. "
                + "N.B. If you lower this value, existing data will be truncated!")
        .setValidationExpression(
            "{maxLength} empty or {type} anyof ['email','html','hyperlink','script','string','text']");
    addAttribute(IS_READ_ONLY).setDataType(BOOL).setNillable(false).setLabel("Read-only");
    addAttribute(IS_UNIQUE).setDataType(BOOL).setNillable(false).setLabel("Unique");
    addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetadata).setLabel("Tags");
    addAttribute(NULLABLE_EXPRESSION).setDataType(SCRIPT).setLabel("Nullable expression");
    addAttribute(VISIBLE_EXPRESSION).setDataType(SCRIPT).setLabel("Visible expression");
    addAttribute(VALIDATION_EXPRESSION).setDataType(SCRIPT).setLabel("Validation expression");
    addAttribute(DEFAULT_VALUE).setDataType(TEXT).setNillable(true).setLabel("Default value");
  }

  // setter injection instead of constructor injection to avoid unresolvable circular dependencies
  @Autowired
  public void setTagMetadata(TagMetadata tagMetadata) {
    this.tagMetadata = requireNonNull(tagMetadata);
  }

  @Autowired
  public void setEntityTypeMetadata(EntityTypeMetadata entityTypeMeta) {
    this.entityTypeMeta = requireNonNull(entityTypeMeta);
  }
}
