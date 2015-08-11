package org.molgenis.data;

import java.util.List;

import org.molgenis.fieldtypes.FieldType;

/**
 * Attribute defines the properties of an entity. Synonyms: feature, column, data item.
 */
public interface AttributeMetaData
{
	/**
	 * Name of the attribute
	 */
	String getName();

	/**
	 * Label of the attribute if set else returns name
	 */
	String getLabel();

	/**
	 * Description of the attribute
	 */
	String getDescription();

	/**
	 * DataType of the attribute
	 */
	FieldType getDataType();

	/**
	 * Whether attribute has not null constraint
	 */
	boolean isNillable();

	/**
	 * Whether attribute is readonly
	 */
	boolean isReadonly();

	/**
	 * Wether attribute should have an unique value for each entity
	 */
	boolean isUnique();

	/**
	 * Should this attribute be visible to the user?
	 */
	boolean isVisible();

	/**
	 * Default value expression
	 */
	String getDefaultValue();

	/**
	 * Whether attribute is primary key
	 */
	boolean isIdAtrribute();

	/**
	 * Whether attribute is human readable key
	 */
	boolean isLabelAttribute();

	/**
	 * Wether this attribute must be searched in case of xref/mref search
	 */
	boolean isLookupAttribute();

	/**
	 * When true the attribute is automatically assigned a value when persisted (for example the current date)
	 */
	boolean isAuto();

	/**
	 * When getDataType=xref/mref, get other end of xref
	 */
	EntityMetaData getRefEntity();

	/**
	 * Expression used to compute this attribute.
	 * 
	 * @return String representation of expression, in JSON format
	 */
	String getExpression();

	/**
	 * When getDataType=compound, get compound attribute parts
	 * 
	 * @return Iterable of attributes or empty Iterable if no attribute parts exist
	 */
	Iterable<AttributeMetaData> getAttributeParts();

	/**
	 * Wether this attribute can be used to aggregate on. Default only attributes of type 'BOOL', 'XREF' and
	 * 'CATEGORICAL' are aggregateable.
	 */
	boolean isAggregateable();

	/**
	 * For int and long fields, the value must be between min and max (included) of the range
	 **/
	Range getRange();

	/**
	 * For enum fields returns the posible enum values
	 */
	List<String> getEnumOptions();

	/**
	 * Javascript expression to determine at runtime if the attribute must be visible or not in the form
	 */
	String getVisibleExpression();

	/**
	 * Javascript expression to validate the value of the attribute
	 */
	String getValidationExpression();

	boolean isSameAs(AttributeMetaData attributeMetaData);
}
