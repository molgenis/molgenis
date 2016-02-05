package org.molgenis.data;

import java.util.stream.Stream;

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
	 * @param attributeMetaData
	 * @param attributeTypes
	 *            whether this attribute is a id and/or label and/or lookup attribute
	 */
	void addAttributeMetaData(AttributeMetaData attributeMetaData, AttributeRole... attributeTypes);

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
	 * Set id attribute
	 * 
	 * @param attr
	 */
	void setIdAttribute(AttributeMetaData attr);

	/**
	 * Set label attribute
	 * 
	 * @param attr
	 */
	void setLabelAttribute(AttributeMetaData attr);

	/**
	 * Add a lookup attribute
	 * 
	 * @param attr
	 */
	void addLookupAttribute(AttributeMetaData attr);

	/**
	 * Set lookup attributes
	 * 
	 * @param lookupAttrs
	 */
	void setLookupAttributes(Stream<AttributeMetaData> lookupAttrs);

	/**
	 * add attribute
	 * 
	 * @param string
	 * @param attributeTypes
	 *            whether this attribute is a id and/or label and/or lookup attribute
	 * @return
	 */
	DefaultAttributeMetaData addAttribute(String string, AttributeRole... attributeTypes);
}
