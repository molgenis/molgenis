package org.molgenis.data;

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
	 * Default value
	 */
	Object getDefaultValue();

	/**
	 * Whether attribute is primary key
	 */
	boolean isIdAtrribute();

	/**
	 * Whether attribute is human readable key
	 */
	boolean isLabelAttribute();

	/**
	 * When getDataType=xref/mref, get other end of xref
	 */
	EntityMetaData getRefEntity();

}
