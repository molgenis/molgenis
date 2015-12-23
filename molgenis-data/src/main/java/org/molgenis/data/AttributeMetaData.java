package org.molgenis.data;

import java.util.List;
import java.util.Set;

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
	 * Label of the attribute in the default language if set else returns name
	 */
	String getLabel();

	/**
	 * Label of the attribute in the requested language if set else returns name
	 */
	String getLabel(String languageCode);

	/**
	 * Get available label language codes
	 */
	Set<String> getLabelLanguageCodes();

	/**
	 * Description of the attribute
	 */
	String getDescription();

	/**
	 * Description of the attribute in the requested languages
	 */
	String getDescription(String languageCode);

	/**
	 * Get available description language codes
	 */
	Set<String> getDescriptionLanguageCodes();

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
	 * Get attribute part by name (case insensitive), returns null if not found
	 * 
	 * @param attrName
	 *            attribute name (case insensitive)
	 * @return attribute or null
	 */
	AttributeMetaData getAttributePart(String attrName);

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

	Iterable<AttributeChangeListener> getChangeListeners();

	/**
	 * Add listener that listens to attribute property changes
	 * 
	 * @param changeListener
	 */
	void addChangeListener(AttributeChangeListener changeListener);

	/**
	 * Add listeners that listens to attribute property changes
	 * 
	 * @param changeListeners
	 */
	void addChangeListeners(Iterable<AttributeChangeListener> changeListeners);

	/**
	 * Remove listener with the given listener id
	 * 
	 * @param changeListenerId
	 */
	void removeChangeListener(String changeListenerId);

	/**
	 * Remove all attribute listeners
	 */
	void removeChangeListeners();
}
