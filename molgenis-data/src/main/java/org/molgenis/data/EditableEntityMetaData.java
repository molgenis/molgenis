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
	 * Set description
	 * 
	 * @param string
	 */
	EditableEntityMetaData setDescription(String string);

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

	/**
	 * 
	 * @param defaultAttributeMetaData
	 */
	void addAttributeMetaData(AttributeMetaData attributeMetaData);

	/**
	 * Set id attribute
	 * 
	 * @param string
	 */
	void setIdAttribute(String string);

	/**
	 * add attribute
	 * 
	 * @param string
	 * @return
	 */
	DefaultAttributeMetaData addAttribute(String string);
}
