package org.molgenis.data;

import org.molgenis.data.support.DefaultAttributeMetaData;

/**
 * EditableEntityMetaData defines the editable structure and attributes of an Entity.
 */
public interface EditableEntityMetaData extends EntityMetaData
{
	/**
	 * set label
	 * 
	 * @param string
	 */
	EditableEntityMetaData setLabel(String string);

	/**
	 * Set label for language
	 * 
	 * @param languageCode
	 * @param label
	 * @return
	 */
	EditableEntityMetaData setLabel(String languageCode, String label);

	/**
	 * Set description
	 * 
	 * @param string
	 */
	EditableEntityMetaData setDescription(String string);

	/**
	 * Set description for language
	 * 
	 * @param languageCode
	 * @param description
	 * @return
	 */
	EditableEntityMetaData setDescription(String languageCode, String description);

	/**
	 * set extends entity metadata
	 * 
	 * @param extendsEntityMeta
	 */
	EditableEntityMetaData setExtends(EntityMetaData extendsEntityMeta);

	/**
	 * set packege
	 * 
	 * @param packageImpl
	 */
	EditableEntityMetaData setPackage(Package packageImpl);

	/**
	 * set abstract
	 * 
	 * @param boolean1
	 */
	EditableEntityMetaData setAbstract(boolean boolean1);

	EditableEntityMetaData setBackend(String backend);

	/**
	 * 
	 * @param defaultAttributeMetaData
	 */
	void addAttributeMetaData(AttributeMetaData attributeMetaData);

	/**
	 * Add attributes to this entity
	 * 
	 * @param attributeMetaData
	 */
	void addAllAttributeMetaData(Iterable<AttributeMetaData> attributeMetaData);

	/**
	 * Remove the given attribute from this entity
	 * 
	 * @param attributeMetaData
	 */
	void removeAttributeMetaData(AttributeMetaData attributeMetaData);

	/**
	 * Set id attribute name
	 * 
	 * @param string
	 */
	void setIdAttribute(String idAttrName);

	/**
	 * Set label attribute name
	 * 
	 * @param string
	 */
	void setLabelAttribute(String labelAttrName);

	/**
	 * add attribute
	 * 
	 * @param string
	 * @return
	 */
	DefaultAttributeMetaData addAttribute(String string);
}
