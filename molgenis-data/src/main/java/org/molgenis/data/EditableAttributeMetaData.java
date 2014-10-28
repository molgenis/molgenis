package org.molgenis.data;

import org.molgenis.fieldtypes.FieldType;

/**
 * Attribute defines the properties of an entity. Synonyms: feature, column, data item.
 */
public interface EditableAttributeMetaData
{
	/**
	 * set data Type
	 * 
	 * @param bool
	 * @return
	 */
	EditableAttributeMetaData setDataType(FieldType bool);

	/**
	 * set nillable
	 * 
	 * @param b
	 * @return
	 */
	EditableAttributeMetaData setNillable(boolean b);

	/**
	 * set ref entity
	 * 
	 * @param refEntity
	 * @return
	 */
	EditableAttributeMetaData setRefEntity(EntityMetaData refEntity);

	/**
	 * set auto
	 * 
	 * @param b
	 * @return
	 */
	EditableAttributeMetaData setAuto(boolean b);

	/**
	 * set default value
	 * 
	 * @param i
	 */
	void setDefaultValue(Object o);

	EditableAttributeMetaData setIdAttribute(boolean b);

	EditableAttributeMetaData setLabelAttribute(boolean b);

	EditableAttributeMetaData setUnique(boolean b);
}
